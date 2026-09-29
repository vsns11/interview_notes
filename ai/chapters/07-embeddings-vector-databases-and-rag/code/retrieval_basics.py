"""Lexical vs semantic retrieval, and fusing them with Reciprocal Rank Fusion.

- TF-IDF + cosine (scikit-learn) and BM25 (from scratch): exact-word matching.
- FakeEmbedder: a stand-in for a trained embedding model. It maps words to
  hand-made "concepts" so paraphrases land close together. A real model learns
  this from data; the retrieval code around it is identical.
- rrf(): merges ranked lists using ranks only (no score normalisation).
"""
import math
import re
from collections import Counter

import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

STOP = set("a an the of to for in on is are my i im can cant can't do does what how "
           "into with and or it its be by at from this that".split())


def tokenize(text):
    return [t for t in re.findall(r"[a-z0-9$]+(?:'[a-z]+)?", text.lower()) if t not in STOP]


# ------------------------------------------------------------------ BM25
class BM25:
    def __init__(self, docs, k1=1.5, b=0.75):
        self.docs = [tokenize(d) for d in docs]
        self.N = len(docs)
        self.avgdl = sum(len(d) for d in self.docs) / self.N
        self.df = Counter(t for d in self.docs for t in set(d))   # document frequency
        self.k1, self.b = k1, b

    def idf(self, term):
        n = self.df.get(term, 0)
        return math.log((self.N - n + 0.5) / (n + 0.5) + 1)       # always >= 0

    def score(self, query, i):
        doc, tf = self.docs[i], Counter(self.docs[i])
        s = 0.0
        for t in tokenize(query):
            if t in tf:
                f = tf[t]
                s += self.idf(t) * f * (self.k1 + 1) / (
                    f + self.k1 * (1 - self.b + self.b * len(doc) / self.avgdl))
        return s

    def search(self, query, k=5, allowed=None):
        ids = [i for i in range(self.N) if allowed is None or i in allowed]
        scored = sorted(((self.score(query, i), i) for i in ids), reverse=True)
        return [(i, s) for s, i in scored[:k] if s > 0]


# ------------------------------------------------------------------ fake embeddings
CONCEPTS = {
    "ACCESS": "log login logged sign signin sign-in password locked lockout unlock "
              "forgot reset credentials access",
    "ACCOUNT": "account profile user",
    "PAYMENT": "card payment declined bank issuer billing charge expired expiry",
    "ERRORCODE": "error code",
    "REFUND": "refund refunds money back return reimbursed",
    "EXPENSE": "expense expenses meals receipts travel travelling claim reimburse",
    "LEAVE": "leave parental maternity paternity baby birth due",
    "BENEFIT": "dental plan covers covered insurance spouse spouses children orthodontics",
}
WORD2CONCEPT = {w: c for c, ws in CONCEPTS.items() for w in ws.split()}
DIMS = list(CONCEPTS) + [f"h{i}" for i in range(16)]            # concepts + hash buckets


class FakeEmbedder:
    """Text -> unit vector. Similar meaning -> nearby vectors (by construction)."""

    def embed(self, text):
        v = np.zeros(len(DIMS))
        for t in tokenize(text):
            if re.fullmatch(r"e\d{4}", t):                 # knows it's an error code...
                v[DIMS.index("ERRORCODE")] += 1.0          # ...but not WHICH code
            elif t in WORD2CONCEPT:
                v[DIMS.index(WORD2CONCEPT[t])] += 1.0
            else:                                           # unknown words: weak hashed signal
                v[len(CONCEPTS) + sum(map(ord, t)) % 16] += 0.3
        n = np.linalg.norm(v)
        return v / n if n else v


class VectorIndex:
    """Exact (brute-force) nearest-neighbour search with cosine similarity."""

    def __init__(self, texts, embedder):
        self.embedder = embedder
        self.M = np.stack([embedder.embed(t) for t in texts])

    def search(self, query, k=5, allowed=None):
        sims = self.M @ self.embedder.embed(query)          # unit vectors: dot = cosine
        order = [i for i in np.argsort(-sims) if allowed is None or i in allowed]
        return [(int(i), float(sims[i])) for i in order[:k] if sims[i] > 0]


# ------------------------------------------------------------------ fusion
def rrf(ranked_lists, k=60):
    """score(d) = sum over lists of 1 / (k + rank_in_list); rank starts at 1."""
    scores = Counter()
    for lst in ranked_lists:
        for rank, (doc_id, _) in enumerate(lst, start=1):
            scores[doc_id] += 1 / (k + rank)
    return scores.most_common()


DOCS = [
    ("Password reset", "To reset a forgotten password, open the sign-in page and choose "
     "Forgot password. A reset link is emailed and expires after 30 minutes."),
    ("Account lockout", "After five failed sign-in attempts the account is locked for "
     "15 minutes. An administrator can unlock it sooner from the admin console."),
    ("Error E4022", "Error E4022 means the payment card was declined by the issuing bank. "
     "Ask the customer to use another card or contact the bank."),
    ("Error E4021", "Error E4021 means the card has expired. Update the expiry date in "
     "billing settings."),
    ("Refund policy", "Customers can request a refund within 30 days of purchase. Refunds go "
     "back to the original payment method within 5 to 10 business days."),
    ("Travel expenses", "Employees may expense meals up to $50 per day while travelling. "
     "Keep receipts for every claim."),
    ("Parental leave", "Full-time employees receive 16 weeks of paid parental leave. Leave "
     "can start up to two weeks before the due date."),
    ("Dental plan", "The dental plan covers employees, spouses and children. Orthodontics "
     "is covered at 50 percent."),
]

if __name__ == "__main__":
    texts = [f"{t}. {b}" for t, b in DOCS]
    names = [t for t, _ in DOCS]
    bm25, vec = BM25(texts), VectorIndex(texts, FakeEmbedder())
    tfidf = TfidfVectorizer(tokenizer=tokenize, token_pattern=None).fit(texts)
    D = tfidf.transform(texts)

    print("BM25 worked example: idf('e4022') =",
          f"{bm25.idf('e4022'):.3f} (in 1 of 8 docs), idf('employees') =",
          f"{bm25.idf('employees'):.3f} (in 3 of 8 docs)\n")

    queries = ["what does error E4022 mean",
               "I can't log into my profile",
               "can I get my money back",
               "card declined E4022 what to do"]
    fmt = lambda res: ", ".join(f"{names[i]} ({s:.2f})" for i, s in res[:3]) or "(nothing)"
    for q in queries:
        tf_sims = cosine_similarity(tfidf.transform([q]), D)[0]
        tf_res = [(int(i), tf_sims[i]) for i in np.argsort(-tf_sims) if tf_sims[i] > 0]
        b, v = bm25.search(q, k=5), vec.search(q, k=5)
        fused = rrf([b, v])
        print(f"Q: {q!r}")
        print(f"  TF-IDF cosine : {fmt(tf_res)}")
        print(f"  BM25          : {fmt(b)}")
        print(f"  embeddings    : {fmt(v)}")
        print(f"  RRF (k=60)    : " + ", ".join(f"{names[i]} ({s:.4f})" for i, s in fused[:3]))
        print()
