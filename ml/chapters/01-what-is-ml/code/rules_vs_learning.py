"""Rules vs learning: a hand-written spam rule compared with a model that learns from labelled examples."""
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.naive_bayes import MultinomialNB

TRAIN = [
    ("win a free prize now", 1),
    ("claim your free money", 1),
    ("cheap meds limited offer", 1),
    ("you won a lottery claim now", 1),
    ("exclusive offer just for you click", 1),
    ("meeting moved to 3pm", 0),
    ("can you review my pull request", 0),
    ("lunch tomorrow?", 0),
    ("here are the slides from today", 0),
    ("your invoice for september is attached", 0),
]
TEST = [
    "limited offer click to claim",   # spam, but no word "free"
    "free coffee in the kitchen",     # not spam, but has "free"
    "slides for the review meeting",  # not spam
]


def rule_based(text: str) -> int:
    """A human wrote this rule. It only knows what its author thought of."""
    return int("free" in text or "prize" in text)


def main() -> None:
    texts, labels = zip(*TRAIN)
    vec = CountVectorizer()                 # turns text into word-count features
    X = vec.fit_transform(texts)
    model = MultinomialNB().fit(X, labels)  # learns P(word | spam) from the examples

    print(f"{'message':34s} rule  learned  P(spam)")
    for t in TEST:
        p = model.predict_proba(vec.transform([t]))[0, 1]
        print(f"{t:34s} {rule_based(t):>4}  {int(p > 0.5):>7}  {p:6.2f}")


if __name__ == "__main__":
    main()
