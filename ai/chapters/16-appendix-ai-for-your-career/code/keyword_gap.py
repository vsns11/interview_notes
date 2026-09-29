"""Compare a job description with a resume: which job keywords does the resume
already show, and which are missing?  Offline, standard library only.

A 'missing' keyword is a QUESTION, not an instruction: add it only if it is
true for you (you really used that tool or did that work).
"""
import re
from collections import Counter

STOP = set("""a an the and or of to in for with on at by from as is are be will you your
we our this that it its who have has into across using use able strong plus
work working team role years year experience including within about their
required needed part daily build engineer""".split())

# Multi-word terms we want to keep together (extend for your field).
PHRASES = ["machine learning", "data pipelines", "rest apis", "unit testing",
           "code review", "ci/cd", "a/b testing", "stakeholder communication"]


def normalise(text: str) -> str:
    return re.sub(r"\s+", " ", text.lower())


def keywords(text: str, top: int = 15) -> list[str]:
    """Return the job's key terms: known phrases first, then frequent words."""
    t = normalise(text)
    found = [p for p in PHRASES if p in t]
    for p in found:                      # remove phrases so words are not double-counted
        t = t.replace(p, " ")
    words = re.findall(r"[a-z][a-z0-9+#.]*[a-z0-9+#]|[a-z]", t)
    counts = Counter(w for w in words if w not in STOP and len(w) > 2)
    singles = [w for w, _ in counts.most_common(top)]
    return found + singles[: max(0, top - len(found))]


def gap_report(job: str, resume: str, top: int = 15) -> dict:
    r = normalise(resume)
    keys = keywords(job, top)
    matched = [k for k in keys if re.search(r"(?<![a-z])" + re.escape(k) + r"(?![a-z])", r)]
    missing = [k for k in keys if k not in matched]
    return {"keywords": keys, "matched": matched, "missing": missing,
            "coverage": round(100 * len(matched) / len(keys), 1)}


JOB = """Backend Engineer (Python). You will build REST APIs and data pipelines in
Python on AWS. Experience with PostgreSQL, Docker and CI/CD is required.
Unit testing and code review are part of daily work. Kafka is a plus.
Strong stakeholder communication. Python, AWS and PostgreSQL experience needed."""

RESUME = """Software engineer, 3 years. Built REST APIs in Python (FastAPI) backed by
PostgreSQL; containerised services with Docker; wrote unit testing suites
with pytest and took part in code review. Deployed to AWS."""

if __name__ == "__main__":
    rep = gap_report(JOB, RESUME)
    print("Job keywords :", ", ".join(rep["keywords"]))
    print("In resume    :", ", ".join(rep["matched"]))
    print("Not in resume:", ", ".join(rep["missing"]))
    print(f"Coverage     : {rep['coverage']}%")
    print("Next step    : for each missing term, ask 'Is this true for me?'"
          " Only true ones get added, in your own words.")
