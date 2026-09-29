"""Byte-Pair Encoding (BPE) from scratch on a tiny corpus.

Learns merge rules from word counts, then tokenizes unseen words.
"""
from collections import Counter


def get_pair_counts(vocab: dict[tuple[str, ...], int]) -> Counter:
    """Count adjacent symbol pairs, weighted by how often each word occurs."""
    pairs: Counter = Counter()
    for symbols, freq in vocab.items():
        for a, b in zip(symbols, symbols[1:]):
            pairs[(a, b)] += freq
    return pairs


def merge_pair(pair: tuple[str, str], vocab: dict) -> dict:
    """Replace every occurrence of `pair` with one merged symbol."""
    merged = {}
    for symbols, freq in vocab.items():
        out, i = [], 0
        while i < len(symbols):
            if i < len(symbols) - 1 and (symbols[i], symbols[i + 1]) == pair:
                out.append(symbols[i] + symbols[i + 1])
                i += 2
            else:
                out.append(symbols[i])
                i += 1
        merged[tuple(out)] = freq
    return merged


def train_bpe(word_counts: dict[str, int], num_merges: int):
    # Start: every word is a sequence of characters plus an end-of-word marker.
    vocab = {tuple(w) + ("</w>",): c for w, c in word_counts.items()}
    merges = []
    for step in range(1, num_merges + 1):
        pairs = get_pair_counts(vocab)
        if not pairs:
            break
        best, count = pairs.most_common(1)[0]
        vocab = merge_pair(best, vocab)
        merges.append(best)
        print(f"merge {step:2d}: {best[0]!r:8} + {best[1]!r:8} (seen {count:2d}x)")
    return merges, vocab


def tokenize(word: str, merges: list[tuple[str, str]]) -> list[str]:
    """Apply learned merges, in the order they were learned."""
    symbols = list(word) + ["</w>"]
    for pair in merges:
        i, out = 0, []
        while i < len(symbols):
            if i < len(symbols) - 1 and (symbols[i], symbols[i + 1]) == pair:
                out.append(symbols[i] + symbols[i + 1])
                i += 2
            else:
                out.append(symbols[i])
                i += 1
        symbols = out
    return symbols


if __name__ == "__main__":
    corpus = {"low": 5, "lower": 2, "newest": 6, "widest": 3}
    print("corpus word counts:", corpus)
    merges, vocab = train_bpe(corpus, num_merges=10)
    print("\nwords after 10 merges:")
    for symbols, freq in vocab.items():
        print(f"  {freq}x  {' '.join(symbols)}")
    print("\ntokenizing words (some never seen in training):")
    for w in ["lowest", "newer", "wider", "slow"]:
        print(f"  {w:7} -> {tokenize(w, merges)}")
