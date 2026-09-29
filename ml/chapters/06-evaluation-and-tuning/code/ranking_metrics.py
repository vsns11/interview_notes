"""Ranking metrics from scratch: precision@k, recall@k, AP/MAP, MRR, NDCG (checked against sklearn.ndcg_score)."""
import numpy as np
from sklearn.metrics import ndcg_score


def precision_at_k(rels, k):
    return np.mean(np.asarray(rels[:k]) > 0)


def recall_at_k(rels, k, n_relevant):
    return np.sum(np.asarray(rels[:k]) > 0) / n_relevant


def average_precision(rels, n_relevant):
    """Mean of precision@i at every rank i that holds a relevant item (missing relevant items count as 0)."""
    hits, total = 0, 0.0
    for i, r in enumerate(rels, start=1):
        if r > 0:
            hits += 1
            total += hits / i
    return total / n_relevant


def reciprocal_rank(rels):
    for i, r in enumerate(rels, start=1):
        if r > 0:
            return 1 / i
    return 0.0


def dcg(rels, k):
    rels = np.asarray(rels[:k], dtype=float)
    return np.sum(rels / np.log2(np.arange(2, len(rels) + 2)))     # position i is discounted by log2(i+1)


def ndcg(rels, k, all_rels=None):
    ideal = sorted(all_rels if all_rels is not None else rels, reverse=True)
    return dcg(rels, k) / dcg(ideal, k)


def main() -> None:
    # Query A: the system returned 6 results. Graded relevance: 3 = perfect, 2 = good, 1 = fair, 0 = bad.
    # The collection holds 4 relevant documents for A; one of them (grade 2) was NOT returned.
    rels_a = [3, 0, 2, 1, 0, 0]
    all_a = [3, 2, 2, 1]
    n_rel_a = 4
    print("query A graded list:", rels_a, "| relevant in collection:", n_rel_a)
    for k in (3, 5):
        print(f"  precision@{k} = {precision_at_k(rels_a, k):.3f}   recall@{k} = {recall_at_k(rels_a, k, n_rel_a):.3f}")
    ap_a = average_precision(rels_a, n_rel_a)
    print(f"  AP  = (1/1 + 2/3 + 3/4) / 4 = {ap_a:.3f}")
    print(f"  RR  = 1/rank of first relevant = {reciprocal_rank(rels_a):.3f}")
    print(f"  DCG@5 = {dcg(rels_a, 5):.3f}, ideal DCG@5 = {dcg(sorted(all_a, reverse=True), 5):.3f}, "
          f"NDCG@5 = {ndcg(rels_a, 5, all_a):.3f}")

    # Query B: first relevant item only at rank 3
    rels_b = [0, 0, 1, 1, 0, 0]
    n_rel_b = 2
    ap_b = average_precision(rels_b, n_rel_b)
    print("\nquery B list:", rels_b, f"AP = {ap_b:.3f}  RR = {reciprocal_rank(rels_b):.3f}")
    print(f"MAP over A and B = {(ap_a + ap_b) / 2:.3f}   MRR = {(reciprocal_rank(rels_a) + reciprocal_rank(rels_b)) / 2:.3f}")

    # Cross-check NDCG with sklearn (true relevance per returned item, scores = descending ranks)
    scores = np.arange(len(rels_a), 0, -1)[None, :]
    print(f"\nsklearn ndcg_score@5 on the returned list only: {ndcg_score([rels_a], scores, k=5):.3f}  "
          f"(ours, same ideal): {ndcg(rels_a, 5):.3f}")
    print("note: sklearn builds the ideal ordering from the returned items only; with the unreturned grade-2")
    print("document included in the ideal list, NDCG@5 drops to", round(ndcg(rels_a, 5, all_a), 3))


if __name__ == "__main__":
    main()
