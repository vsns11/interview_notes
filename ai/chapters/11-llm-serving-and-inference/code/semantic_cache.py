"""Exact + semantic response cache in front of a (fake) LLM.

- FakeEmbedder: a deterministic local stand-in for an embedding model
  (normalise words, map synonyms, hash words + character trigrams into a vector).
- ResponseCache: layer 1 = exact match on a normalised key, layer 2 = cosine
  similarity >= threshold. Entries are scoped by tenant and expire after a TTL.
- FakeLLM: pretends to be slow and expensive so we can count saved calls.
"""
import hashlib
import re
import time
import numpy as np

SYNONYMS = {"reset": "change", "modify": "change", "password": "password",
            "pwd": "password", "refund": "refund", "money": "refund",
            "cancel": "cancel", "terminate": "cancel", "sub": "subscription"}
STOP = {"how", "do", "i", "can", "my", "the", "a", "to", "is", "please", "what",
        "you", "me", "get", "back", "want", "tell"}


class FakeEmbedder:
    dim = 256

    def _hash(self, s):
        return int(hashlib.md5(s.encode()).hexdigest(), 16) % self.dim

    def embed(self, text):
        words = [SYNONYMS.get(w, w) for w in re.findall(r"[a-z]+", text.lower())]
        words = [w for w in words if w not in STOP]
        v = np.zeros(self.dim)
        for w in words:
            v[self._hash("w:" + w)] += 1.0
            for i in range(len(w) - 2):                    # char trigrams: typo tolerance
                v[self._hash("c:" + w[i:i + 3])] += 0.3
        n = np.linalg.norm(v)
        return v / n if n else v


class FakeLLM:
    calls = 0

    def answer(self, question):
        FakeLLM.calls += 1
        return f"<answer to: {question}>"


class ResponseCache:
    def __init__(self, embedder, threshold=0.85, ttl_s=3600):
        self.emb, self.threshold, self.ttl = embedder, threshold, ttl_s
        self.exact = {}                  # (tenant, normalised text) -> (answer, t)
        self.entries = []                # (tenant, vector, answer, t, original)

    @staticmethod
    def _norm(text):
        return " ".join(re.findall(r"[a-z0-9]+", text.lower()))

    def get(self, tenant, text, now):
        k = (tenant, self._norm(text))
        if k in self.exact and now - self.exact[k][1] < self.ttl:
            return self.exact[k][0], "exact", 1.0
        v = self.emb.embed(text)
        best, best_sim = None, -1.0
        for t, ev, ans, ts, orig in self.entries:
            if t != tenant or now - ts >= self.ttl:        # scope + freshness first
                continue
            sim = float(v @ ev)
            if sim > best_sim:
                best, best_sim = (ans, orig), sim
        if best and best_sim >= self.threshold:
            return best[0], f"semantic (matched '{best[1]}')", best_sim
        return None, "miss", best_sim

    def put(self, tenant, text, answer, now):
        self.exact[(tenant, self._norm(text))] = (answer, now)
        self.entries.append((tenant, self.emb.embed(text), answer, now, text))


def serve(cache, llm, tenant, q, now):
    ans, how, sim = cache.get(tenant, q, now)
    if ans is None:
        ans = llm.answer(q)
        cache.put(tenant, q, ans, now)
    print(f"[{tenant}] {q!r:<44} -> {how:<48} sim={sim:.2f}")
    return ans


if __name__ == "__main__":
    cache, llm = ResponseCache(FakeEmbedder(), threshold=0.85, ttl_s=3600), FakeLLM()
    now = time.time()
    traffic = [
        ("acme", "How do I reset my password?"),
        ("acme", "how do i reset my password"),          # exact after normalising
        ("acme", "How can I change my pwd?"),            # same meaning, new words
        ("acme", "Please tell me how to modify my password"),
        ("acme", "how do I reset my pasword"),           # typo: partial overlap only
        ("acme", "How do I cancel my subscription?"),
        ("acme", "How do I terminate my sub?"),
        ("acme", "How do I cancel my order?"),           # DIFFERENT meaning - risky
        ("globex", "How do I reset my password?"),       # other tenant: never shared
        ("acme", "Can I get my money back?"),
    ]
    for tenant, q in traffic:
        serve(cache, llm, tenant, q, now)
    print(f"\nLLM calls: {FakeLLM.calls} of {len(traffic)} requests")

    # threshold is a precision knob: check the risky pair explicitly
    e = FakeEmbedder()
    s = e.embed("How do I cancel my subscription?") @ e.embed("How do I cancel my order?")
    print(f"similarity 'cancel subscription' vs 'cancel order' = {s:.2f} "
          f"(threshold 0.85 keeps them apart; 0.40 would wrongly merge them)")
    # TTL: an hour later the entry has expired
    _, how, _ = cache.get("acme", "How do I reset my password?", now + 7200)
    print(f"same question 2 h later: {how} (TTL expired)")
