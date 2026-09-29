"""Guard against AI embellishment: build a career-writing prompt from a
verified FACT SHEET, then check the model's draft for numbers, tools and
titles that are NOT in the fact sheet.

The real API call is shown in the README (needs an API key - not run).
Here a FakeLLM stands in and deliberately embellishes, the way real models
sometimes do when details are missing.
"""
import re

FACTS = {
    "role": "Data Analyst",
    "employer": "a regional hospital network",
    "years": "2",
    "tools": ["SQL", "Python", "Tableau"],
    "achievements": [
        "Automated the weekly bed-occupancy report, cutting preparation time from 6 hours to 1 hour",
        "Built a Tableau dashboard used by 40 ward managers",
    ],
}


def build_prompt(facts: dict, task: str, audience: str, limit: str) -> str:
    """The master pattern: role + context + facts + task + constraints + format."""
    fact_lines = "\n".join(f"- {a}" for a in facts["achievements"])
    return (
        "You are an experienced technical recruiter and editor.\n"
        f"Context: I am a {facts['role']} with {facts['years']} years at "
        f"{facts['employer']}. Tools: {', '.join(facts['tools'])}.\n"
        f"Verified achievements (use ONLY these facts):\n{fact_lines}\n"
        f"Task: {task}\nAudience: {audience}\nLength: {limit}\n"
        "Rules: do not invent numbers, tools, titles or employers. "
        "If something is missing, write [ASK ME] instead of guessing."
    )


class FakeLLM:
    """Stand-in model. It embellishes on purpose to test the checker."""
    def complete(self, prompt: str) -> str:
        return ("Data Analyst with 2 years of experience who automated weekly "
                "reporting (6 hours to 1 hour) and built a Tableau dashboard for "
                "40 ward managers. Led a team of 5 and cut hospital costs by 18% "
                "using Snowflake and Power BI.")


def allowed_tokens(facts: dict) -> tuple[set, set]:
    text = " ".join([facts["role"], facts["employer"], facts["years"],
                     *facts["tools"], *facts["achievements"]])
    numbers = set(re.findall(r"\d+(?:\.\d+)?%?", text))
    names = {w.lower() for w in re.findall(r"[A-Za-z][A-Za-z+#.]*", text)}
    return numbers, names


def check_draft(draft: str, facts: dict) -> list[str]:
    numbers, names = allowed_tokens(facts)
    problems = []
    for n in re.findall(r"\d+(?:\.\d+)?%?", draft):
        if n not in numbers:
            problems.append(f"number '{n}' is not in your fact sheet")
    # Capitalised words not at sentence start are likely tools, titles or names.
    for m in re.finditer(r"(?<![.!?]\s)(?<!^)\b([A-Z][A-Za-z+#]+)", draft):
        w = m.group(1)
        if w.lower() not in names:
            problems.append(f"name/tool '{w}' is not in your fact sheet")
    return problems


if __name__ == "__main__":
    prompt = build_prompt(FACTS, "Write a 2-sentence resume summary.",
                          "hiring managers in healthcare analytics", "under 50 words")
    print(prompt, "\n")
    draft = FakeLLM().complete(prompt)
    print("DRAFT:", draft, "\n")
    issues = check_draft(draft, FACTS)
    print(f"{len(issues)} unverified claim(s):")
    for p in issues:
        print(" -", p)
    print("Verdict:", "REWRITE before using" if issues else "OK to edit and use")
