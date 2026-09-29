"""Toy music radio: why session context and audio embeddings matter for next-track prediction.

Tracks have a genre and a mood (workout / sleep / focus); their 'audio embedding' encodes both.
Users like 2 genres; each session has ONE mood, so the same user wants very different tracks
in a workout session and a sleep session. 20% of tracks are released after training (no plays yet).
Task: given a user and the first 3 tracks of a test session, predict the next track. Metric: Hit@10.
"""
import numpy as np

rng = np.random.default_rng(2)
N_TRACKS, N_GENRES, N_MOODS, DIM, N_USERS = 2000, 10, 3, 16, 1000
genre = rng.integers(0, N_GENRES, N_TRACKS)
mood = rng.integers(0, N_MOODS, N_TRACKS)
is_new = rng.random(N_TRACKS) < 0.2
g_cent, m_cent = rng.normal(size=(N_GENRES, DIM)), rng.normal(size=(N_MOODS, DIM))
audio = g_cent[genre] + 1.5 * m_cent[mood] + 0.5 * rng.normal(size=(N_TRACKS, DIM))
audio /= np.linalg.norm(audio, axis=1, keepdims=True)
likes = np.array([rng.choice(N_GENRES, 2, replace=False) for _ in range(N_USERS)])


def session(user, allow_new, length=8):
    m = rng.integers(0, N_MOODS)
    ok = np.isin(genre, likes[user]) & (mood == m) & (allow_new | ~is_new)
    pool = np.flatnonzero(ok)
    return rng.choice(pool, min(length, len(pool)), replace=False)


if __name__ == "__main__":
    train = [(u, session(u, False)) for u in range(N_USERS) for _ in range(6)]
    test = [(u, session(u, True)) for u in range(N_USERS)]
    plays = np.zeros(N_TRACKS)
    user_genre = np.zeros((N_USERS, N_GENRES))
    trans = np.zeros((N_TRACKS, N_TRACKS), dtype=np.float32)          # "played B right after A"
    for u, s in train:
        np.add.at(plays, s, 1)
        np.add.at(user_genre[u], genre[s], 1)
        trans[s[:-1], s[1:]] += 1

    def rank(u, prefix, method):
        if method == "popularity":
            score = plays.copy()
        elif method == "user taste (no session)":
            score = user_genre[u][genre] * (plays + 1)
        elif method == "session transitions":
            score = trans[prefix[-1]] + 1e-3 * plays
        elif method == "session audio embedding":
            score = audio @ audio[prefix].mean(0)
        elif method == "audio + user taste":                            # blend: audio session + user genre prior
            score = audio @ audio[prefix].mean(0) + 0.3 * (user_genre[u][genre] > 0)
        else:                                                           # ceiling: knows the true pool, picks at random
            score = (np.isin(genre, likes[u]) & (mood == mood[prefix[0]])) + 0.01 * rng.random(N_TRACKS)
        score = score.astype(float)
        score[prefix] = -np.inf
        return np.argsort(-score)[:10]

    methods = ["popularity", "user taste (no session)", "session transitions",
               "session audio embedding", "audio + user taste", "ceiling (true pool, random)"]
    print(f"{"method":<28}{'Hit@10':>8}{'Hit@10 on NEW tracks':>22}")
    for mth in methods:
        hit, hit_new, n_new = 0, 0, 0
        for u, s in test:
            prefix, target = s[:3], s[3]
            h = target in rank(u, prefix, mth)
            hit += h
            if is_new[target]:
                n_new += 1
                hit_new += h
        print(f"{mth:<28}{hit / len(test):>8.3f}{hit_new / n_new:>22.3f}")
    print(f"({n_new} of {len(test)} test targets are tracks released after training)")
