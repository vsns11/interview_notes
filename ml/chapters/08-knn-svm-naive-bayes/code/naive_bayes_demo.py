"""Naive Bayes: Bayes' theorem by hand, a spam filter by hand vs MultinomialNB, and the three NB flavours."""
import numpy as np
from sklearn.datasets import load_iris
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.model_selection import cross_val_score
from sklearn.naive_bayes import BernoulliNB, GaussianNB, MultinomialNB


def bayes_theorem():
    # A test for a disease with 1% prevalence, 90% sensitivity, 5% false-positive rate
    p_d, p_pos_d, p_pos_nd = 0.01, 0.90, 0.05
    p_pos = p_pos_d * p_d + p_pos_nd * (1 - p_d)
    print(f"P(disease | positive) = {p_pos_d * p_d:.4f} / {p_pos:.4f} = {p_pos_d * p_d / p_pos:.3f}")


TRAIN = [
    ("win cash now", "spam"),
    ("win free prize now", "spam"),
    ("free cash offer", "spam"),
    ("meeting at noon", "ham"),
    ("lunch meeting tomorrow", "ham"),
    ("project update at noon", "ham"),
]


def spam_by_hand(message, alpha=1.0):
    docs = {"spam": [], "ham": []}
    for text, label in TRAIN:
        docs[label].extend(text.split())
    vocab = sorted({w for words in docs.values() for w in words})
    V = len(vocab)
    log_scores = {}
    for c, words in docs.items():
        prior = sum(1 for _, l in TRAIN if l == c) / len(TRAIN)
        score = np.log(prior)
        for w in message.split():
            if w not in vocab:           # unseen word: ignored (as CountVectorizer does)
                continue
            p = (words.count(w) + alpha) / (len(words) + alpha * V)   # Laplace smoothing
            score += np.log(p)
        log_scores[c] = score
    m = max(log_scores.values())
    z = sum(np.exp(s - m) for s in log_scores.values())
    return {c: np.exp(s - m) / z for c, s in log_scores.items()}, V


def spam_with_sklearn(message):
    texts, labels = zip(*TRAIN)
    vec = CountVectorizer()
    X = vec.fit_transform(texts)
    nb = MultinomialNB(alpha=1.0).fit(X, labels)
    proba = nb.predict_proba(vec.transform([message]))[0]
    return dict(zip(nb.classes_, proba))


def gaussian_nb():
    X, y = load_iris(return_X_y=True)
    print(f"\nGaussianNB on iris, 5-fold CV accuracy = {cross_val_score(GaussianNB(), X, y, cv=5).mean():.3f}")
    Xb = (X > X.mean(axis=0)).astype(int)   # binarised features for BernoulliNB
    print(f"BernoulliNB on binarised iris          = {cross_val_score(BernoulliNB(), Xb, y, cv=5).mean():.3f}")


if __name__ == "__main__":
    bayes_theorem()
    msg = "free cash for the meeting"
    hand, V = spam_by_hand(msg)
    print(f"\nvocabulary size = {V}")
    print(f"by hand : P(spam|'{msg}') = {hand['spam']:.3f}, P(ham) = {hand['ham']:.3f}")
    sk = spam_with_sklearn(msg)
    print(f"sklearn : P(spam|'{msg}') = {sk['spam']:.3f}, P(ham) = {sk['ham']:.3f}")
    gaussian_nb()
