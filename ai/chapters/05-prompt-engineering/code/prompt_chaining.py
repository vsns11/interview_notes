"""Prompt chaining: split one big ask into small steps with checks in between.

Each step is a separate "model call" with a narrow prompt. A local stand-in
(rule-based) plays the model so the pipeline runs offline.
"""
import json
import re

REVIEW = ("The battery lasts two days, which is great. The charger broke after a week. "
          "Support took 9 days to reply. The screen is sharp.")


def fake_llm(step: str, text: str) -> str:
    """Stand-in for a model call; a real app would send a step-specific prompt."""
    if step == "extract":
        sentences = [s.strip() for s in re.split(r"(?<=[.!?])\s+", text) if s.strip()]
        return json.dumps(sentences)
    if step == "classify":
        items = json.loads(text)
        neg = ("broke", "took", "slow", "bad")
        return json.dumps([{"claim": s, "sentiment": "negative" if any(w in s for w in neg) else "positive"}
                           for s in items])
    if step == "draft":
        rows = json.loads(text)
        issues = [r["claim"] for r in rows if r["sentiment"] == "negative"]
        return ("Thank you for the detailed review. We're sorry about: "
                + "; ".join(i[0].lower() + i[1:].rstrip(".") for i in issues)
                + ". A replacement charger is on its way.")
    raise ValueError(step)


def run_chain(review: str) -> str:
    # Step 1: extract claims (prompt: "List each distinct claim as a JSON array of strings")
    claims = fake_llm("extract", review)
    assert isinstance(json.loads(claims), list), "step 1 must return a JSON list"
    print("step 1 claims   :", claims)

    # Step 2: classify each claim (prompt: "Label each claim positive/negative. JSON.")
    labeled = fake_llm("classify", claims)
    assert all(r["sentiment"] in {"positive", "negative"} for r in json.loads(labeled))
    print("step 2 labeled  :", labeled)

    # Gate: only draft an apology when there is something to apologize for
    if not any(r["sentiment"] == "negative" for r in json.loads(labeled)):
        return "No reply needed."

    # Step 3: draft a reply using ONLY the negative claims
    reply = fake_llm("draft", labeled)
    assert len(reply.split()) < 80, "reply must stay short"
    return reply


if __name__ == "__main__":
    print("step 3 reply    :", run_chain(REVIEW))
