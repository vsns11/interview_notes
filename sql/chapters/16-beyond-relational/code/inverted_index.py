"""16.11 A search engine in miniature: analyzer -> inverted index -> boolean match -> BM25 ranking,
plus hash-sharding the documents and merging per-shard top hits (what Elasticsearch/OpenSearch do at scale).
Run: python inverted_index.py"""
import math
import re
from collections import Counter, defaultdict

DOCS = {
    1: "Running shoes for trail running, lightweight",
    2: "Waterproof hiking boots for the trail",
    3: "Road running shoes with extra cushioning",
    4: "Trail mix snack: nuts and raisins",
    5: "Lightweight rain jacket for hiking and running",
}
STOP = {"for", "the", "and", "with", "a", "of"}


def analyze(text):
    """Analyzer = char filter + tokenizer + token filters (lowercase, stop words, naive stemming)."""
    tokens = re.findall(r"[a-z0-9]+", text.lower())                     # tokenizer + lowercase filter
    tokens = [t for t in tokens if t not in STOP]                        # stop-word filter
    return [re.sub(r"(ning|ing|s)$", "", t) if len(t) > 4 else t for t in tokens]  # toy stemmer: running->run


class Shard:
    def __init__(self):
        self.postings = defaultdict(dict)   # term -> {doc_id: term frequency}
        self.doc_len = {}

    def index(self, doc_id, text):
        terms = analyze(text)
        self.doc_len[doc_id] = len(terms)
        for term, tf in Counter(terms).items():
            self.postings[term][doc_id] = tf


def bm25(shards, query, k1=1.2, b=0.75, top=3):
    """Score with global statistics (like Elasticsearch's dfs_query_then_fetch), then merge shard results."""
    n_docs = sum(len(s.doc_len) for s in shards)
    avg_len = sum(sum(s.doc_len.values()) for s in shards) / n_docs
    terms = analyze(query)
    df = {t: sum(len(s.postings.get(t, {})) for s in shards) for t in terms}
    per_shard = []
    for s in shards:                                                     # "query phase": each shard scores locally
        scores = Counter()
        for t in terms:
            idf = math.log(1 + (n_docs - df[t] + 0.5) / (df[t] + 0.5))
            for doc, tf in s.postings.get(t, {}).items():
                norm = tf * (k1 + 1) / (tf + k1 * (1 - b + b * s.doc_len[doc] / avg_len))
                scores[doc] += idf * norm
        per_shard.append(scores.most_common(top))
    merged = sorted((hit for hits in per_shard for hit in hits), key=lambda h: -h[1])[:top]  # coordinator merge
    return terms, per_shard, merged


if __name__ == "__main__":
    print("analyze('Running shoes for the TRAIL') ->", analyze("Running shoes for the TRAIL"))

    shards = [Shard(), Shard()]
    for doc_id, text in DOCS.items():
        shards[hash(doc_id) % len(shards)].index(doc_id, text)          # routing: shard = hash(_id) % shards
    for i, s in enumerate(shards):
        print(f"shard {i}: docs {sorted(s.doc_len)}")

    everything = defaultdict(dict)
    for s in shards:
        for term, plist in s.postings.items():
            everything[term].update(plist)
    for term in ["run", "trail", "shoe", "hik", "lightweight"]:
        print(f"postings[{term!r:14}] -> {dict(sorted(everything[term].items()))}")

    both = set(everything["run"]) & set(everything["trail"])            # boolean AND = intersect posting lists
    print("docs matching run AND trail:", sorted(both))
    assert both == {1}, both

    terms, per_shard, merged = bm25(shards, "trail running shoes")
    print("query terms:", terms)
    for i, hits in enumerate(per_shard):
        print(f"shard {i} top hits:", [(d, round(sc, 3)) for d, sc in hits])
    print("merged top 3:", [(d, round(sc, 3)) for d, sc in merged])
    for d, _ in merged:
        print(f"  doc {d}: {DOCS[d]}")
    assert merged[0][0] == 1, "doc 1 mentions trail, running (twice) and shoes"
    print("all assertions passed")
