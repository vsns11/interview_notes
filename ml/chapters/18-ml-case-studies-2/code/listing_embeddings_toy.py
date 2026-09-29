"""Toy similar listings: listing embeddings learned from click sessions (skip-gram with negative sampling).

Synthetic rental marketplace: 20 cities (markets) x 100 listings. Each listing has hidden attributes
(price tier, style, size). A browsing session stays in ONE city and clicks listings close to the guest's
taste; about 30% of sessions end in a booking.
Three training variants (same skip-gram, window 2):
  a) negatives drawn from the whole catalogue
  b) + half the negatives drawn from the SAME city (otherwise "different city" is an easy shortcut)
  c) + the booked listing as a global context for every click of a booked session
Evaluation on held-out sessions: are the 10 most similar listings in the same city (before filtering)?
After the serving-time city filter: is the next click in the top 10? Is the eventual booking?
Finally, cold start: new listings with no clicks get the mean vector of 3 similar listings nearby.
"""
import numpy as np
import torch

torch.manual_seed(0)
torch.set_num_threads(1)
rng = np.random.default_rng(0)
CITIES, PER, DIM = 20, 100, 16
N = CITIES * PER
city = np.repeat(np.arange(CITIES), PER)
attr = rng.normal(size=(N, 3))                                   # price tier, style, size (hidden)


def session(c):
    taste = rng.normal(size=3)
    pool = np.where(city == c)[0]
    w = np.exp(-((attr[pool] - taste) ** 2).sum(1) / 0.8)
    clicks = list(rng.choice(pool, 8, replace=False, p=w / w.sum()))
    booked = clicks[rng.integers(8)] if rng.random() < 0.3 else None   # booked = one of the viewed listings
    return clicks, booked


def make(n):
    return [session(rng.integers(CITIES)) for _ in range(n)]


def train(sessions, same_city_neg, booking_ctx, epochs=2, neg=5):
    pairs = []
    for clicks, booked in sessions:
        for i, a in enumerate(clicks):
            for j in range(max(0, i - 2), min(len(clicks), i + 3)):
                if j != i:
                    pairs.append((a, clicks[j]))
            if booking_ctx and booked is not None and a != booked:
                pairs.append((a, booked))
    pairs = torch.tensor(pairs)
    emb_in = torch.nn.Embedding(N, DIM); emb_out = torch.nn.Embedding(N, DIM)
    torch.nn.init.normal_(emb_in.weight, std=0.1); torch.nn.init.zeros_(emb_out.weight)
    opt = torch.optim.Adam(list(emb_in.parameters()) + list(emb_out.parameters()), lr=0.01)
    city_t = torch.tensor(city)
    for _ in range(epochs):
        for b in torch.randperm(len(pairs)).split(2048):
            a, p = pairs[b, 0], pairs[b, 1]
            n = torch.randint(0, N, (len(b), neg))                   # catalogue-wide negatives
            if same_city_neg:                                        # half the negatives from a's own city
                local = city_t[a][:, None] * PER + torch.randint(0, PER, (len(b), neg // 2 + 1))
                n = torch.cat([n[:, : neg - local.shape[1]], local], 1)
            va = emb_in(a)
            loss = (-torch.nn.functional.logsigmoid((va * emb_out(p)).sum(1)).mean()
                    - torch.nn.functional.logsigmoid(-(va[:, None] * emb_out(n)).sum(2)).mean())
            opt.zero_grad(); loss.backward(); opt.step()
    return torch.nn.functional.normalize(emb_in.weight.detach(), dim=1).numpy()


def evaluate(E, sessions):
    sim = E @ E.T
    np.fill_diagonal(sim, -9)
    same = (city[np.argsort(-sim, axis=1)[:, :10]] == city[:, None]).mean()      # before any filter
    sim[city[:, None] != city[None, :]] = -9                                     # serving filters to the city
    top = np.argsort(-sim, axis=1)[:, :10]
    nxt = [n in top[a] for clicks, _ in sessions for a, n in zip(clicks, clicks[1:])]
    book = [b in top[clicks[0]] for clicks, b in sessions if b is not None and b != clicks[0]]
    return same, np.mean(nxt), np.mean(book)


if __name__ == "__main__":
    train_s, test_s = make(20000), make(2000)
    print(f"{N} listings in {CITIES} cities; {len(train_s):,} training sessions, {len(test_s):,} held out\n")
    print(f"{'':<44}{'top-10 in':>12}{'after same-city filter':>32}")
    print(f"{'variant':<44}{'same city':>12}{'next-click HR@10':>17}{'booking HR@10':>15}")
    rand = rng.normal(size=(N, DIM)); rand /= np.linalg.norm(rand, axis=1, keepdims=True)
    print(f"{'random vectors':<44}" + "".join(f"{v:>{w}.3f}" for v, w in zip(evaluate(rand, test_s), (12, 17, 15))))
    results = {}
    for name, sc, bk in [("a) skip-gram, catalogue-wide negatives", False, False),
                         ("b) + same-city negatives", True, False),
                         ("c) + booked listing as global context", True, True)]:
        results[name] = E = train(train_s, sc, bk)
        print(f"{name:<44}" + "".join(f"{v:>{w}.3f}" for v, w in zip(evaluate(E, test_s), (12, 17, 15))))

    # cold start: pretend 10% of listings are brand new (no clicks) -> mean of 3 nearest known listings in the
    # same city by OBSERVABLE attributes (we give it the hidden attributes plus noise, like price/type/size fields)
    E = results["c) + booked listing as global context"].copy()
    new = rng.choice(N, N // 10, replace=False)
    known = np.setdiff1d(np.arange(N), new)
    obs = attr + 0.3 * rng.normal(size=attr.shape)
    for i in new:
        cand = known[city[known] == city[i]]
        near = cand[np.argsort(((obs[cand] - obs[i]) ** 2).sum(1))[:3]]
        E[i] = E[near].mean(0); E[i] /= np.linalg.norm(E[i])
    new_set = set(new.tolist())


    def hr_from_new(M):
        sim = M @ M.T - 9 * np.eye(N) - 9 * (city[:, None] != city[None, :])
        top = np.argsort(-sim, axis=1)[:, :10]
        return np.mean([n in top[a] for clicks, _ in test_s for a, n in zip(clicks, clicks[1:]) if a in new_set])


    print(f"\ncold start: {len(new)} listings treated as new (no clicks); next-click HR@10 starting from them")
    print(f"  random vector                         {hr_from_new(np.where(np.isin(np.arange(N), new)[:, None], rand, E)):.3f}")
    print(f"  mean of 3 similar nearby listings     {hr_from_new(E):.3f}")
    print(f"  (vector learned from real clicks      {hr_from_new(results['c) + booked listing as global context']):.3f})")
