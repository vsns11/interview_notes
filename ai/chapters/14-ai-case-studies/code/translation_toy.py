"""Machine translation service: three pieces you can run without a model.

1) Language identification from character trigrams (the first hop of the request path).
2) BLEU from scratch - and why a correct paraphrase can score badly.
3) A segment cache: how much traffic an LRU cache of whole sentences absorbs
   when requests follow a Zipf (heavy-head) distribution.
All sentences are written for this demo; numbers are illustrative.
"""
from __future__ import annotations

import math
from collections import Counter, OrderedDict

import numpy as np

TRAIN = {
    "en": "the weather is nice today and we are going to the park with the children. "
          "please send me the report before the meeting on friday. where is the station",
    "es": "el tiempo es bueno hoy y vamos al parque con los niños. "
          "por favor envíame el informe antes de la reunión del viernes. dónde está la estación",
    "fr": "le temps est beau aujourd'hui et nous allons au parc avec les enfants. "
          "merci de m'envoyer le rapport avant la réunion de vendredi. où est la gare",
    "de": "das wetter ist heute schön und wir gehen mit den kindern in den park. "
          "bitte schick mir den bericht vor dem treffen am freitag. wo ist der bahnhof",
}


def trigrams(text: str) -> Counter:
    t = f"  {text.lower()}  "
    return Counter(t[i:i + 3] for i in range(len(t) - 2))


PROFILES = {lang: trigrams(txt) for lang, txt in TRAIN.items()}


def detect(text: str) -> tuple[str, float]:
    q = trigrams(text)
    scores = {}
    for lang, prof in PROFILES.items():
        tot, v = sum(prof.values()), len(prof) + 1
        scores[lang] = sum(c * math.log((prof[g] + 0.5) / (tot + 0.5 * v)) for g, c in q.items())
    best = sorted(scores.items(), key=lambda kv: -kv[1])
    margin = (best[0][1] - best[1][1]) / max(1, sum(q.values()))   # per-trigram log-odds margin
    return best[0][0], margin


def ngrams(toks, n):
    return Counter(tuple(toks[i:i + n]) for i in range(len(toks) - n + 1))


def bleu(cand: str, refs: list[str], max_n: int = 4) -> float:
    """Sentence BLEU (Papineni et al., 2002) with add-one smoothing for n > 1."""
    c = cand.lower().split()
    rs = [r.lower().split() for r in refs]
    logp = 0.0
    for n in range(1, max_n + 1):
        cn = ngrams(c, n)
        maxref = Counter()
        for r in rs:
            maxref |= ngrams(r, n)                    # clip by the max count in any reference
        match = sum(min(k, maxref[g]) for g, k in cn.items())
        total = max(1, sum(cn.values()))
        p = (match + (n > 1)) / (total + (n > 1))
        logp += math.log(max(p, 1e-9)) / max_n
    ref_len = min((len(r) for r in rs), key=lambda L: (abs(L - len(c)), L))
    bp = 1.0 if len(c) > ref_len else math.exp(1 - ref_len / max(1, len(c)))
    return bp * math.exp(logp)


def cache_hit_rate(capacity: int, n_requests: int = 200_000, n_segments: int = 500_000,
                   s: float = 1.1, seed: int = 3) -> float:
    rng = np.random.default_rng(seed)
    ranks = np.arange(1, n_segments + 1)
    p = ranks ** -s
    p /= p.sum()
    reqs = rng.choice(n_segments, size=n_requests, p=p)
    cache: OrderedDict[int, None] = OrderedDict()
    hits = 0
    for r in reqs:
        if r in cache:
            hits += 1
            cache.move_to_end(r)
        else:
            cache[r] = None
            if len(cache) > capacity:
                cache.popitem(last=False)
    return hits / n_requests


def main() -> None:
    print("1) Language ID (character trigrams)")
    for text in ["we will send the report on friday", "enviaré el informe el viernes",
                 "nous allons à la gare", "wir gehen in den park", "hotel", "ok"]:
        lang, margin = detect(text)
        flag = "" if margin > 0.5 else "  <- low margin: ask the user / use the UI language"
        print(f"   {text!r:40} -> {lang} (margin {margin:.2f}){flag}")

    print("\n2) BLEU against one reference")
    ref = ["please send me the report before friday"]
    cands = {
        "exact": "please send me the report before friday",
        "correct paraphrase": "could you email me the report by friday",
        "wrong meaning, same words": "please send the report to me after friday",
        "word salad": "report the friday me send before please",
    }
    for k, v in cands.items():
        print(f"   {k:27} BLEU {bleu(v, ref):.3f}   {v!r}")
    print("   add a 2nd human reference 'could you email the report to me by friday':",
          f"paraphrase BLEU {bleu(cands['correct paraphrase'], ref + ['could you email the report to me by friday']):.3f}")

    print("\n3) Sentence cache under Zipf traffic (assumed Zipf s=1.1; 200k requests over 500k distinct segments)")
    for cap in (1_000, 10_000, 50_000):
        print(f"   LRU capacity {cap:>6,} segments -> hit rate {cache_hit_rate(cap):.1%}")


if __name__ == "__main__":
    main()
