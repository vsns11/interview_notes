"""Evaluate prompt versions on a small labeled test set.

A local stand-in model reacts to prompt quality the way real models tend to:
without an explicit label list and format it answers in free text; with them,
it answers with a label. The harness -- not the fake model -- is the point:
fixed test cases, automatic checks, one number per prompt version.
"""
import json

TEST_SET = [  # (ticket, expected category)
    ("I was charged twice for March.", "billing"),
    ("App crashes when I tap Export.", "bug"),
    ("How do I add a teammate?", "how-to"),
    ("Please delete my account.", "account"),
    ("Refund the duplicate payment please.", "billing"),
    ("Error 500 on login page.", "bug"),
    ("Where can I change my password?", "how-to"),
    ("Can you close my profile?", "account"),
    ("The invoice total looks wrong.", "billing"),   # no keyword -> a realistic miss
]
KEYWORDS = {"billing": ["charged", "refund", "payment"], "bug": ["crash", "error"],
            "how-to": ["how do", "where can"], "account": ["delete my", "close my"]}

PROMPT_V1 = "What is this ticket about?\n{ticket}"
PROMPT_V2 = ("Classify the support ticket into exactly one category: billing, bug, how-to, account.\n"
             'Reply with JSON only: {{"category": "<category>"}}\n<ticket>{ticket}</ticket>')


def fake_model(prompt: str) -> str:
    ticket = prompt.split("\n")[-1].replace("<ticket>", "").replace("</ticket>", "").lower()
    guess = next((c for c, kws in KEYWORDS.items() if any(k in ticket for k in kws)), "other")
    if "exactly one category" in prompt and "json" in prompt.lower():
        return json.dumps({"category": guess})
    return f"This looks like a customer who has a {guess}-related concern."   # free text


def score(template: str) -> dict:
    valid = correct = 0
    for ticket, expected in TEST_SET:
        out = fake_model(template.format(ticket=ticket))
        try:
            pred = json.loads(out)["category"]
            valid += 1
        except (json.JSONDecodeError, KeyError, TypeError):
            pred = None
        correct += pred == expected
    n = len(TEST_SET)
    return {"format_valid": f"{valid}/{n}", "accuracy": f"{correct}/{n}"}


if __name__ == "__main__":
    for name, tpl in [("v1 vague", PROMPT_V1), ("v2 specific", PROMPT_V2)]:
        print(f"{name:12} -> {score(tpl)}")
