"""Synthetic MovieLens-like data, generated in code (no downloads).

Each user has hidden tastes over 6 genres; each movie belongs to 1-2 genres and has a
hidden "quality". Popular movies get seen far more often (long-tail popularity), like
real catalogues. Ratings are 1-5 stars and only a small fraction of (user, movie) pairs
are observed -- the matrix is sparse, exactly like real rating data.
"""
from pathlib import Path

import numpy as np

GENRES = ["Action", "Comedy", "Drama", "Horror", "Romance", "SciFi"]
IMG = Path(__file__).resolve().parent.parent / "img"


def make_movielens_like(n_users=600, n_items=400, seed=0):
    rng = np.random.default_rng(seed)
    g = len(GENRES)
    # item genres: one main genre, 30 % chance of a second one
    item_genres = np.zeros((n_items, g))
    main = rng.integers(0, g, n_items)
    item_genres[np.arange(n_items), main] = 1
    second = rng.integers(0, g, n_items)
    has2 = rng.random(n_items) < 0.3
    item_genres[np.arange(n_items)[has2], second[has2]] = 1
    quality = rng.normal(0, 0.5, n_items)

    # user tastes: likes 1-2 genres strongly, mild opinions on the rest
    taste = rng.normal(0, 0.4, (n_users, g))
    fav = rng.integers(0, g, n_users)
    taste[np.arange(n_users), fav] += 1.5
    fav2 = rng.integers(0, g, n_users)
    taste[np.arange(n_users), fav2] += 0.8

    # true affinity -> star rating
    affinity = taste @ item_genres.T / item_genres.sum(1) + quality  # (users, items)
    stars = np.clip(np.round(3 + 1.2 * affinity + rng.normal(0, 0.5, affinity.shape)), 1, 5)

    # which pairs get observed: popular items (Zipf-like) and liked items are seen more
    pop = 1.0 / np.arange(1, n_items + 1) ** 0.6
    pop = pop[rng.permutation(n_items)]
    p_seen = pop[None, :] * np.exp(1.5 * affinity)
    p_seen = p_seen / p_seen.sum(1, keepdims=True) * 40  # ~40 ratings per user on average
    observed = rng.random(p_seen.shape) < np.clip(p_seen, 0, 1)

    users, items = np.nonzero(observed)
    ratings = stars[users, items]
    return {
        "users": users, "items": items, "ratings": ratings,
        "n_users": n_users, "n_items": n_items,
        "item_genres": item_genres, "true_stars": stars,
    }


def train_test_split_per_user(users, items, ratings, test_frac=0.2, seed=0):
    """Hold out ~20 % of each user's ratings (so every user appears in train)."""
    rng = np.random.default_rng(seed)
    test_mask = np.zeros(len(users), dtype=bool)
    for u in np.unique(users):
        idx = np.flatnonzero(users == u)
        k = int(len(idx) * test_frac)
        if k:
            test_mask[rng.choice(idx, k, replace=False)] = True
    tr, te = ~test_mask, test_mask
    return (users[tr], items[tr], ratings[tr]), (users[te], items[te], ratings[te])


if __name__ == "__main__":
    d = make_movielens_like()
    n_obs = len(d["ratings"])
    print(f"users={d['n_users']} items={d['n_items']} ratings={n_obs}")
    print(f"density = {n_obs / (d['n_users'] * d['n_items']):.3%}")
    counts = np.bincount(d["items"], minlength=d["n_items"])
    top10 = np.sort(counts)[::-1][: d["n_items"] // 10].sum() / counts.sum()
    print(f"top 10% of items receive {top10:.0%} of all ratings")
    print("star histogram:", {int(s): int((d['ratings'] == s).sum()) for s in range(1, 6)})
