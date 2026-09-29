"""Legal research assistant, in miniature: jurisdiction-filtered hybrid
retrieval (BM25 + toy embeddings fused with reciprocal rank fusion), then a
synchronous verifier that checks EVERY claim before anything is shown.

All cases are fictional. The drafting "LLM" is a scripted fake that makes
the kinds of mistakes real models make (an invented citation, a misquote,
an overruled authority) so the verifier has something to catch.
"""
from __future__ import annotations

import math
import re
from collections import Counter
from dataclasses import dataclass

STOP = set("a an the of to and or in on for is are be by with that this it as at from".split())


def toks(text: str) -> list[str]:
    return [w for w in re.findall(r"[a-z0-9]+", text.lower()) if w not in STOP]


@dataclass(frozen=True)
class Authority:
    cite: str            # the citation string an attorney would use
    jurisdiction: str
    court: str
    year: int
    treatment: str       # "good law" | "overruled" | "distinguished"
    passage: str


CORPUS = [
    Authority("Avery v. Brook Logistics, 212 F.3d 101 (2d Cir. 2004)", "US-federal", "appeals", 2004,
              "good law", "An employee is constructively dismissed when the employer makes working "
              "conditions so intolerable that a reasonable person would feel compelled to resign."),
    Authority("Hale v. Northwind Foods, 88 Cal.App.4th 55 (2001)", "US-CA", "appellate", 2001,
              "good law", "Under California law, a constructive discharge claim requires aggravated "
              "or continuous intolerable conditions known to the employer."),
    Authority("Ortiz v. Pell Mfg., 51 Cal.3d 900 (1994)", "US-CA", "supreme", 1994,
              "overruled", "A single isolated incident is sufficient to establish constructive discharge."),
    Authority("Marsh v. Delta Freight, 640 A.2d 12 (Del. 1996)", "US-DE", "supreme", 1996,
              "good law", "Delaware courts require the employee to show the employer deliberately "
              "created intolerable conditions to force a resignation."),
    Authority("Cal. Lab. Code § 2802", "US-CA", "statute", 2016,
              "good law", "An employer shall indemnify an employee for all necessary expenditures "
              "incurred in direct consequence of the discharge of duties."),
]


# ------------------------------------------------------------ retrieval
def bm25_scores(query: str, docs: list[Authority], k1: float = 1.5, b: float = 0.75) -> list[float]:
    tokenised = [toks(d.cite + " " + d.passage) for d in docs]
    avgdl = sum(map(len, tokenised)) / len(tokenised)
    n = len(docs)
    df = Counter(w for t in tokenised for w in set(t))
    out = []
    for t in tokenised:
        tf = Counter(t)
        s = 0.0
        for w in toks(query):
            if w not in tf:
                continue
            idf = math.log(1 + (n - df[w] + 0.5) / (df[w] + 0.5))
            s += idf * tf[w] * (k1 + 1) / (tf[w] + k1 * (1 - b + b * len(t) / avgdl))
        out.append(s)
    return out


def embed_scores(query: str, docs: list[Authority]) -> list[float]:
    """Toy 'semantic' score: overlap of 4-letter word stems (a real system uses an embedding model)."""
    stem = lambda ws: {w[:4] for w in ws}
    q = stem(toks(query))
    return [len(q & stem(toks(d.passage))) / math.sqrt(len(q) * len(stem(toks(d.passage))) or 1)
            for d in docs]


def rrf(*rankings: list[int], k: int = 60) -> dict[int, float]:
    """Reciprocal rank fusion: score(d) = sum over lists of 1 / (k + rank)."""
    fused: dict[int, float] = {}
    for ranking in rankings:
        for rank, idx in enumerate(ranking, start=1):
            fused[idx] = fused.get(idx, 0.0) + 1 / (k + rank)
    return fused


def retrieve(query: str, jurisdictions: set[str], k: int = 3) -> list[Authority]:
    docs = [d for d in CORPUS if d.jurisdiction in jurisdictions | {"US-federal"}]   # metadata filter first
    b = bm25_scores(query, docs)
    e = embed_scores(query, docs)
    rank_b = sorted(range(len(docs)), key=lambda i: -b[i])
    rank_e = sorted(range(len(docs)), key=lambda i: -e[i])
    fused = rrf(rank_b, rank_e)
    return [docs[i] for i in sorted(fused, key=lambda i: -fused[i])[:k]]


# ---------------------------------------------------------- drafting (fake)
@dataclass
class Claim:
    text: str
    cite: str
    quote: str


def fake_draft(_query: str, _context: list[Authority]) -> list[Claim]:
    return [
        Claim("In California, constructive discharge requires aggravated or continuous intolerable conditions.",
              "Hale v. Northwind Foods, 88 Cal.App.4th 55 (2001)",
              "a constructive discharge claim requires aggravated or continuous intolerable conditions"),
        Claim("A single isolated incident is enough for constructive discharge in California.",
              "Ortiz v. Pell Mfg., 51 Cal.3d 900 (1994)",
              "A single isolated incident is sufficient to establish constructive discharge"),
        Claim("California courts presume constructive discharge after any pay cut.",
              "Reyes v. Summit Bank, 77 Cal.4th 300 (2019)",           # does not exist
              "any reduction in pay creates a presumption of constructive discharge"),
        Claim("Delaware requires proof that the employer deliberately created intolerable conditions.",
              "Marsh v. Delta Freight, 640 A.2d 12 (Del. 1996)",
              "the employer knowingly permitted difficult conditions"),  # misquote
        Claim("The federal standard asks whether a reasonable person would feel compelled to resign.",
              "Avery v. Brook Logistics, 212 F.3d 101 (2d Cir. 2004)",
              "a reasonable person would feel compelled to resign"),
    ]


# -------------------------------------------------------------- verifier
def verify(claim: Claim, ctx: list[Authority], available: bool = True) -> tuple[bool, str]:
    if not available:
        raise RuntimeError("verification service unavailable")
    auth = next((a for a in CORPUS if a.cite == claim.cite), None)
    if auth is None:
        return False, "citation does not resolve in corpus (cannot render)"
    if auth not in ctx:          # debugging-matrix question: generated from memory, not retrieval?
        return False, f"not in retrieved context (drafted from model memory; {auth.treatment})"
    if claim.quote.lower() not in auth.passage.lower():
        return False, "quoted text not found in cited source"
    support = len(set(toks(claim.text)) & set(toks(claim.quote))) / max(1, len(set(toks(claim.text))))
    if support < 0.4:
        return False, f"entailment proxy too weak ({support:.2f})"
    if auth.treatment == "overruled":
        return False, "authority has been overruled (treatment re-check)"
    return True, f"verified (support {support:.2f}, {auth.treatment})"


def answer(query: str, jurisdictions: set[str], verifier_up: bool = True) -> None:
    ctx = retrieve(query, jurisdictions)
    print("retrieved:", [a.cite.split(",")[0] for a in ctx])
    try:
        results = [(c, *verify(c, ctx, verifier_up)) for c in fake_draft(query, ctx)]
    except RuntimeError as e:
        print(f"FAIL CLOSED: {e} -> research pointers only:")
        for a in ctx:
            print(f"   - {a.cite} [{a.treatment}]")
        return
    kept = [c for c, ok, _ in results if ok]
    for c, ok, why in results:
        print(f"  {'KEEP ' if ok else 'STRIP'} {c.cite.split(',')[0]:<28} {why}")
    if not kept:
        print("no verified claims -> research pointers mode")
        return
    print("\nverified answer (each claim carries a pinpoint quote):")
    for c in kept:
        print(f"  * {c.text}\n      [{c.cite}] \"{c.quote}\"")
    pointers = [a for a in ctx if a.cite not in {c.cite for c in kept}]
    for a in pointers:
        print(f"  research pointer (no verified claim): {a.cite} [{a.treatment}]")
    print("  (claims were removed: re-read what remains so it cannot mislead)")


if __name__ == "__main__":
    q = "constructive dismissal standard for an employee in California, company incorporated in Delaware"
    print("jurisdictions extracted: US-CA, US-DE")
    answer(q, {"US-CA", "US-DE"})
    print("\n--- same query while the verification service is down ---")
    answer(q, {"US-CA", "US-DE"}, verifier_up=False)
