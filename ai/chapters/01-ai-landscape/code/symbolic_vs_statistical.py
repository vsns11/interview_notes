"""Symbolic AI (hand-written rules) vs statistical AI (learned from data)
on a tiny review-sentiment task."""
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.naive_bayes import MultinomialNB

# --- Symbolic: a human writes the knowledge down as rules -------------------
POSITIVE = {"great", "good", "love", "excellent"}
NEGATIVE = {"bad", "terrible", "hate", "awful"}


def rule_based(text: str) -> str:
    words = set(text.lower().split())
    score = len(words & POSITIVE) - len(words & NEGATIVE)
    if score > 0:
        return "pos"
    if score < 0:
        return "neg"
    return "unknown"          # no rule fires -> the system has nothing to say


# --- Statistical: the machine infers word weights from labelled examples ----
TRAIN = [
    ("great phone love it", "pos"), ("excellent battery good screen", "pos"),
    ("superb camera and fast", "pos"), ("fast delivery superb value", "pos"),
    ("terrible battery hate it", "neg"), ("awful screen bad value", "neg"),
    ("slow and laggy camera", "neg"), ("laggy apps and slow charging", "neg"),
]
TEST = ["superb and fast", "slow laggy phone", "good but slow", "love it"]


def main() -> None:
    texts, labels = zip(*TRAIN)
    vec = CountVectorizer()
    model = MultinomialNB().fit(vec.fit_transform(texts), labels)
    print(f"{'review':<18} {'rules':<8} {'learned':<8} P(pos)")
    for review in TEST:
        proba = model.predict_proba(vec.transform([review]))[0]
        p_pos = proba[list(model.classes_).index("pos")]
        pred = model.predict(vec.transform([review]))[0]
        print(f"{review:<18} {rule_based(review):<8} {pred:<8} {p_pos:.2f}")


if __name__ == "__main__":
    main()
