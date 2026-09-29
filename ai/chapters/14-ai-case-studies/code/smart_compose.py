"""Email sentence completion (smart-compose style) with a tiny local model.

A trigram language model with backoff stands in for the small neural LM.
Beam search proposes the rest of the sentence; a confidence threshold decides
whether to show it; a blocklist suppresses risky words; a per-user model is
interpolated with the global one. We replay held-out emails word by word and
measure trigger rate, acceptance rate and characters saved.
All data is synthetic.
"""
from __future__ import annotations

import math
import random
from collections import Counter, defaultdict

random.seed(7)
END = "</s>"

OPENERS = ["thanks for", "thank you for", "i hope you", "please let me", "let me know",
           "looking forward to", "can you ask", "could you send"]
TAILS = {
    "thanks for": ["your help", "the update", "getting back to me", "your quick reply", "sharing this"],
    "thank you for": ["your help", "the update", "your time", "the invite"],
    "i hope you": ["are doing well", "had a great weekend", "are well", "enjoyed the trip"],
    "please let me": ["know if you have any questions", "know what you think", "know by friday"],
    "let me know": ["if you have any questions", "what you think", "if that works for you", "when you are free"],
    "looking forward to": ["hearing from you", "our meeting", "working with you", "the demo"],
    "can you ask": ["her to join the call", "him to send the file", "them to review the draft"],
    "could you send": ["me the slides", "the invoice", "the latest version", "it again"],
}
FREE = "the a report draft file meeting call budget slides team plan is was will be on at by today tomorrow friday monday next week new old".split()


def sentence(user: str) -> list[str]:
    r = random.random()
    if r < 0.7:
        o = random.choice(OPENERS)
        w = TAILS[o]
        t = w[0] if random.random() < 0.45 else random.choice(w)   # skewed, like real phrasing
        if user == "sam" and o == "thanks for":
            t = "the heads up"                                     # sam's personal habit
        s = f"{o} {t}"
    else:                                                          # free-form text: hard to predict
        s = " ".join(random.choices(FREE, k=random.randint(3, 7)))
    return s.split() + [END]


def corpus(n: int, users: list[str]) -> list[tuple[str, list[str]]]:
    return [(u, sentence(u)) for u in random.choices(users, k=n)]


class Trigram:
    def __init__(self, sents):
        self.c3, self.c2, self.c1 = defaultdict(Counter), defaultdict(Counter), Counter()
        for s in sents:
            toks = ["<s>", "<s>"] + s
            for a, b, c in zip(toks, toks[1:], toks[2:]):
                self.c3[(a, b)][c] += 1
                self.c2[b][c] += 1
                self.c1[c] += 1
        self.n1 = sum(self.c1.values())

    def dist(self, a: str, b: str) -> dict[str, float]:
        """Next-word distribution with simple backoff (trigram -> bigram -> unigram)."""
        for table in (self.c3.get((a, b)), self.c2.get(b)):
            if table:
                tot = sum(table.values())
                return {w: c / tot for w, c in table.items()}
        return {w: c / self.n1 for w, c in self.c1.items()}


def mix(p: dict, q: dict | None, lam: float) -> dict:
    if not q:
        return p
    keys = set(p) | set(q)
    return {k: (1 - lam) * p.get(k, 0) + lam * q.get(k, 0) for k in keys}


BLOCK = {"he", "she", "him", "her"}   # never guess a person's gender for the writer


def suggest(prefix, g: Trigram, u: Trigram | None, beam=3, max_words=6, lam=0.6, block=True):
    """Beam search from the words typed so far. Returns (words, probability)."""
    beams = [(0.0, list(prefix), [])]
    done = []
    for _ in range(max_words):
        nxt = []
        for lp, ctx, out in beams:
            a, b = (["<s>", "<s>"] + ctx)[-2:]
            d = mix(g.dist(a, b), u.dist(a, b) if u else None, lam)
            for w, p in sorted(d.items(), key=lambda kv: -kv[1])[:beam]:
                if p <= 0:
                    continue
                cand = (lp + math.log(p), ctx + [w], out + [w])
                (done if w == END else nxt).append(cand)
        beams = sorted(nxt, key=lambda x: -x[0])[:beam]
        if not beams:
            break
    if not done:
        return None, 0.0
    lp, _, out = max(done, key=lambda x: x[0])
    words = out[:-1]
    if not words or (block and BLOCK & set(words)):
        return None, 0.0
    return words, math.exp(lp)


def replay(test, g, users_models, thr, block=True):
    shown = accepted = chars_saved = chars_total = positions = blocked = 0
    for user, s in test:
        words = s[:-1]
        chars_total += len(" ".join(words))
        i = 1                                         # the user always types the first word
        while i < len(words):
            positions += 1
            sug, p = suggest(words[:i], g, users_models.get(user), block=block)
            if sug is None and block:
                raw, _ = suggest(words[:i], g, users_models.get(user), block=False)
                blocked += raw is not None and bool(BLOCK & set(raw))
            if sug is not None and p >= thr:
                shown += 1
                if words[i:i + len(sug)] == sug and i + len(sug) == len(words):
                    accepted += 1                     # Tab: the rest of the sentence is taken
                    chars_saved += len(" ".join(sug))
                    break
            i += 1
    return dict(trigger=shown / positions, accept=accepted / max(shown, 1),
                saved=chars_saved / chars_total, blocked=blocked)


def main() -> None:
    users = ["sam"] + [f"u{i}" for i in range(19)]
    train = corpus(6000, users)
    test = corpus(800, users)
    g = Trigram([s for _, s in train])
    personal = {"sam": Trigram([s for u, s in train if u == "sam"])}

    print("1) Threshold sweep (global model only) - show a suggestion only if P(suggestion) >= threshold")
    print("   threshold | trigger rate | acceptance | chars saved | blocked by pronoun filter")
    for thr in (0.0, 0.05, 0.15, 0.3, 0.5):
        m = replay(test, g, {}, thr)
        print(f"   {thr:9.2f} | {m['trigger']:12.1%} | {m['accept']:10.1%} | {m['saved']:11.1%} | {m['blocked']}")

    print("\n2) Personalisation for user 'sam' (always writes 'thanks for the heads up'), threshold 0.3")
    sam_test = [(u, s) for u, s in test if u == "sam"]
    for label, models in (("global only", {}), ("global + personal", personal)):
        m = replay(sam_test, g, models, 0.3)
        print(f"   {label:18s} trigger {m['trigger']:5.1%}  acceptance {m['accept']:5.1%}  chars saved {m['saved']:5.1%}")
    print("   prefix ['thanks','for'] -> global:", suggest(["thanks", "for"], g, None)[0],
          "| sam's blend:", suggest(["thanks", "for"], g, personal["sam"])[0])

    print("\n3) Sensitive-word filter (never guess the pronoun of someone the writer mentions)")
    print("   prefix ['can','you','ask'] unfiltered ->", suggest(["can", "you", "ask"], g, None, block=False)[0],
          "| filtered -> (nothing shown)" if suggest(["can", "you", "ask"], g, None)[0] is None else "?")


if __name__ == "__main__":
    main()
