"""Toy event recommendation: every item is new, so the model must score events by their features.

Synthetic city: users and events have a home/venue location (km), topic interests and a price sensitivity.
Events live for 2 weeks and are gone; each 'week' brings a fresh batch of events nobody has interacted with.
True P(register | shown) depends on topic match, distance, friends already registered, price and host quality.
Train on impressions from weeks 1-4, test on the NEW events of week 5:
  - popularity from early registrations   (needs a warm-up period; zero at the moment of posting)
  - item-ID collaborative filtering       (no ID has any interactions -> random)
  - GBDT on features, with ablations      (drop distance / social / topic to see what each is worth)
"""
import numpy as np
from sklearn.ensemble import HistGradientBoostingClassifier

rng = np.random.default_rng(0)
sig = lambda z: 1 / (1 + np.exp(-z))
U, T, EV_PER_WEEK, WEEKS = 4000, 8, 300, 5

u_loc = rng.uniform(0, 30, (U, 2))                               # a 30 x 30 km city
u_topic = rng.dirichlet(np.full(T, 0.4), U)                      # each user likes a few of 8 topics
u_price = rng.uniform(0.2, 2.0, U)                               # price sensitivity
# friends live nearby (homophily): 20 friends drawn from the 200 closest users
near = np.argsort(((u_loc[:, None, :] - u_loc[None, :, :]) ** 2).sum(-1), axis=1)[:, 1:201]
friends = [rng.choice(near[u], 20, replace=False) for u in range(U)]


def make_events(n):
    return dict(loc=rng.uniform(0, 30, (n, 2)), topic=rng.integers(0, T, n),
                price=rng.choice([0, 10, 25, 60], n, p=[.35, .3, .25, .1]), host=rng.normal(0, 1, n))


def true_p(u, ev, j, n_friends):
    dist = np.linalg.norm(u_loc[u] - ev["loc"][j], axis=-1)
    z = (-3.0 + 6.0 * u_topic[u, ev["topic"][j]] - 0.18 * dist + 1.2 * np.log1p(n_friends)
         - 0.03 * u_price[u] * ev["price"][j] + 0.5 * ev["host"][j])
    return sig(z), dist


def simulate(week_events, impressions_per_user=15, registered=None):
    """Show each user random events of the week; return a feature table and labels."""
    rows, labels, ev_ids = [], [], []
    for u in range(U):
        js = rng.choice(len(week_events["topic"]), impressions_per_user, replace=False)
        nf = np.array([np.isin(friends[u], registered.get(j, [])).sum() for j in js])
        p, dist = true_p(u, week_events, js, nf)
        y = rng.random(len(js)) < p
        for j, yy in zip(js[y], y[y]):
            registered.setdefault(j, []).append(u)
        rows.append(np.column_stack([dist, nf, u_topic[u, week_events["topic"][js]], week_events["price"][js],
                                     u_price[u] * week_events["price"][js], week_events["host"][js]]))
        labels.append(y); ev_ids.append(js)
    return np.concatenate(rows), np.concatenate(labels), np.concatenate(ev_ids)


if __name__ == "__main__":
    COLS = ["distance_km", "friends_registered", "user_topic_affinity", "price", "price_x_sensitivity", "host_rating"]
    Xtr, ytr = [], []
    for w in range(WEEKS - 1):
        ev = make_events(EV_PER_WEEK)
        reg = {}
        simulate(ev, 30, reg)                                         # early registrations create social signal
        X, y, _ = simulate(ev, 15, reg)
        Xtr.append(X); ytr.append(y)
    Xtr, ytr = np.concatenate(Xtr), np.concatenate(ytr)

    ev5 = make_events(EV_PER_WEEK)
    reg5 = {}
    simulate(ev5, 30, reg5)
    Xte, yte, ids = simulate(ev5, 15, reg5)
    print(f"train: {len(ytr):,} impressions on {EV_PER_WEEK * (WEEKS - 1)} past events, register rate {ytr.mean():.1%}")
    print(f"test : {len(yte):,} impressions on {EV_PER_WEEK} NEW events (0 overlap with training IDs)\n")


    def hit_at_3(score):
        """Per user: of the 15 events shown, do the model's top 3 contain a registration? (users with >=1 only)"""
        s, y = score.reshape(U, 15), yte.reshape(U, 15)
        has = y.any(axis=1)
        top = np.argsort(-s, axis=1)[:, :3]
        return np.take_along_axis(y, top, axis=1).any(axis=1)[has].mean()


    def auc(score, y=None):
        y = yte if y is None else y
        pos, neg = score[y], score[~y]
        r = np.argsort(np.argsort(np.concatenate([pos, neg])))
        return (r[:len(pos)].sum() - len(pos) * (len(pos) - 1) / 2) / (len(pos) * len(neg))


    def auc_slice(score, mask):
        return auc(score[mask], yte[mask])


    soc = Xte[:, 1] > 0
    print(f"{soc.mean():.1%} of test impressions have >=1 friend already registered "
          f"(register rate {yte[soc].mean():.1%} vs {yte[~soc].mean():.1%} without)\n")
    print(f"{'model':<34}{'AUC':>7}{'hit@3':>8}{'AUC, friend-going slice':>25}")
    print(f"{'random / item-ID CF (all cold)':<34}{auc(rng.random(len(yte))):>7.3f}{hit_at_3(rng.random(len(yte))):>8.3f}")
    early = np.array([len(reg5.get(j, [])) for j in ids], dtype=float)
    print(f"{'popularity (early registrations)':<34}{auc(early + 1e-6 * rng.random(len(early))):>7.3f}"
          f"{hit_at_3(early + 1e-6 * rng.random(len(early))):>8.3f}")
    for drop in [None, "distance_km", "friends_registered", "user_topic_affinity"]:
        keep = [i for i, c in enumerate(COLS) if c != drop]
        m = HistGradientBoostingClassifier(max_iter=200, random_state=0).fit(Xtr[:, keep], ytr)
        p = m.predict_proba(Xte[:, keep])[:, 1]
        name = "GBDT, all features" if drop is None else f"GBDT without {drop}"
        print(f"{name:<34}{auc(p):>7.3f}{hit_at_3(p):>8.3f}{auc_slice(p, soc):>25.3f}")
