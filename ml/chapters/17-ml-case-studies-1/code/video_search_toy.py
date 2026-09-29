"""Toy text-to-video search: three evidence channels, rank fusion, and frame pooling for 'moment' queries.

Each synthetic video has a hidden topic vector (its true content). We assume encoders already exist that map
text and frames into ONE shared space (CLIP-style), so every channel yields a vector comparable to the query:
  - title/tags   : often empty or misleading (clickbait), otherwise a good view of the topic
  - frames       : 16 frame embeddings; noisy individually, but always present
  - transcript   : speech-to-text; present only for videos with speech
1) Main-topic queries: recall@10 per channel, a naive average of scores, and reciprocal rank fusion (RRF).
2) 'Moment' queries: the queried thing appears in only 2 of 16 frames (a short scene in a long video).
   Mean-pooling the frames dilutes it; max over short segments keeps it.
"""
import numpy as np
from sklearn.linear_model import LogisticRegression

rng = np.random.default_rng(0)
N, D, F = 5000, 32, 16                       # videos, embedding dims, frames per video


def noise(*shape):
    """Gaussian noise with expected norm ~1 per vector, so '0.5 * noise' means half the signal's length."""
    return rng.normal(size=shape) / np.sqrt(D)


def unit(x):
    return x / np.linalg.norm(x, axis=-1, keepdims=True)


def recall_at_10(scores, target):
    top = np.argsort(-scores, axis=1)[:, :10]
    return float(np.mean([t in row for t, row in zip(target, top)]))


def ranks(scores):
    """Rank of every video per query (0 = best)."""
    order = np.argsort(-scores, axis=1)
    r = np.empty_like(order)
    rows = np.arange(len(scores))[:, None]
    r[rows, order] = np.arange(scores.shape[1])[None, :]
    return r


if __name__ == "__main__":
    topic = unit(rng.normal(size=(N, D)))

    # title: 30% empty/uninformative (random vector), 10% clickbait (points at a popular unrelated topic)
    title = unit(topic + 0.5 * noise(N, D))
    empty = rng.random(N) < 0.30
    title[empty] = unit(rng.normal(size=(empty.sum(), D)))
    bait = (~empty) & (rng.random(N) < 0.10)
    title[bait] = unit(topic[rng.integers(0, 50, bait.sum())] + 0.3 * noise(bait.sum(), D))

    # frames: each frame a very noisy view of the topic; a 'moment' topic appears in 2 consecutive frames
    frames = unit(topic[:, None, :] + 4.0 * noise(N, F, D))
    moment = unit(rng.normal(size=(N, D)))
    start = rng.integers(0, F - 1, N)
    for i in range(N):
        frames[i, start[i]:start[i] + 2] = unit(moment[i] + 0.35 * noise(2, D))

    # transcript: only 45% of videos have speech
    speech = rng.random(N) < 0.45
    trans = unit(topic + 0.7 * noise(N, D))

    Q = 1000
    qi = rng.choice(N, Q, replace=False)
    query = unit(topic[qi] + 0.7 * noise(Q, D))       # typed query = noisy view of the topic

    s_title = query @ title.T
    s_visual = query @ unit(frames.mean(axis=1)).T                 # mean-pooled frames
    s_trans = np.where(speech[None, :], query @ trans.T, -1.0)      # no speech -> no evidence (lowest score)
    s_avg = (s_title + s_visual + np.where(speech[None, :], s_trans, 0.0)) / 3
    K = 60                                                          # RRF constant (Cormack et al., 2009)
    s_rrf = sum(1.0 / (K + 1 + ranks(s)) for s in (s_title, s_visual, s_trans))


    def feats(i):
        """Per (query, video) features for a learned fusion: channel scores gated by channel availability."""
        has_t, has_s = (~empty).astype(float), speech.astype(float)
        return np.stack([s_title[i] * has_t, s_visual[i], np.where(speech, s_trans[i], 0) * has_s,
                         has_t, has_s], axis=1)


    # learned fusion: logistic regression on the first half of the queries, evaluated on the second half
    half = Q // 2
    X, y = [], []
    for i in range(half):
        hard = np.argsort(-s_rrf[i])[:30]                            # hard negatives: fusion's own top results
        cand = np.unique(np.concatenate([[qi[i]], hard, rng.integers(0, N, 30)]))
        X.append(feats(i)[cand]); y.append(cand == qi[i])
    lr = LogisticRegression(max_iter=1000).fit(np.concatenate(X), np.concatenate(y))
    s_learned = np.stack([feats(i) @ lr.coef_[0] for i in range(Q)])

    print(f"{N:,} videos; {empty.mean():.0%} empty titles, {bait.mean():.0%} clickbait titles, "
          f"{speech.mean():.0%} with speech\n")
    test = np.arange(half, Q)
    e = test[empty[qi[test]]]                                       # test queries whose target has an EMPTY title
    print(f"main-topic queries, recall@10 on {len(test)} test queries   (all | target title empty, n={len(e)})")
    for name, s in [("title/tags only", s_title), ("visual (mean of frames)", s_visual),
                    ("transcript only", s_trans), ("average of raw scores", s_avg),
                    ("reciprocal rank fusion", s_rrf), ("learned fusion (LR)", s_learned)]:
        print(f"  {name:<26}{recall_at_10(s[test], qi[test]):.3f} | {recall_at_10(s[e], qi[e]):.3f}")
    print("  learned weights [title, visual, transcript, has_title, has_speech]:", np.round(lr.coef_[0], 1))

    # 2) moment queries
    mq = unit(moment[qi] + 0.3 * noise(Q, D))
    mean_pool = unit(frames.mean(axis=1))
    seg = unit(frames.reshape(N, F // 2, 2, D).mean(axis=2))       # 8 segments of 2 frames
    seg_scores = np.einsum("qd,nsd->qns", mq, seg)
    print("\n'moment' queries (scene lasts 2 of 16 frames), recall@10")
    print(f"  mean-pool whole video       {recall_at_10(mq @ mean_pool.T, qi):.3f}")
    print(f"  max over 2-frame segments   {recall_at_10(seg_scores.max(axis=2), qi):.3f}")
    print(f"  index size: 1 vector/video = {N:,}; 8 segments/video = {N * 8:,}")
