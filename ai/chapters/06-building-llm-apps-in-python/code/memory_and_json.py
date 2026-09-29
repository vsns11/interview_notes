"""Two everyday LLM-app chores, with local stand-ins:

1. Conversation memory: the API is stateless, so YOU resend history. Keep it
   under a token budget with a sliding window + a running summary.
2. Structured output: ask for JSON, validate it against a schema, and retry
   with the error message if it is invalid (the fallback when the provider's
   native structured-output mode is not available).
"""
import json


def tokens(text: str) -> int:
    return max(1, len(text) // 4)          # rough rule of thumb


# ------------------------------------------------------------- 1. memory
def fake_summarize(turns):
    """A real app would call a small, cheap model here."""
    facts = [t["content"].split(".")[0] for t in turns if t["role"] == "user"]
    return "Earlier the user said: " + "; ".join(facts) + "."


def build_context(history, budget, keep_last=2):
    """Keep the newest turns that fit the budget; summarize the rest."""
    kept, used = [], 0
    for turn in reversed(history):
        cost = tokens(turn["content"])
        if len(kept) >= keep_last and used + cost > budget:
            break
        kept.insert(0, turn)
        used += cost
    dropped = history[: len(history) - len(kept)]
    if dropped:
        summary = {"role": "user", "content": "[summary] " + fake_summarize(dropped)}
        kept.insert(0, summary)
    return kept, dropped


# ------------------------------------------------------------- 2. JSON output
SCHEMA = {"required": {"name": str, "email": str, "plan": str, "seats": int}}


def validate(obj):
    errors = []
    for key, typ in SCHEMA["required"].items():
        if key not in obj:
            errors.append(f"missing field '{key}'")
        elif not isinstance(obj[key], typ):
            errors.append(f"'{key}' must be {typ.__name__}, got {type(obj[key]).__name__}")
    return errors


class SloppyFakeLLM:
    """First answer is chatty and wrong-typed; after feedback it complies."""

    def __init__(self):
        self.calls = 0

    def complete(self, prompt):
        self.calls += 1
        if self.calls == 1:
            return 'Sure! Here is the JSON:\n{"name": "Asha Rao", "email": "asha@example.com", "plan": "Team", "seats": "12"}'
        return '{"name": "Asha Rao", "email": "asha@example.com", "plan": "Team", "seats": 12}'


def extract_json(text):
    start, end = text.find("{"), text.rfind("}")
    return json.loads(text[start:end + 1])       # tolerate chatter around JSON


def get_structured(llm, request, max_attempts=3):
    prompt = f"Return ONLY JSON with keys name, email, plan, seats (int).\n{request}"
    for attempt in range(1, max_attempts + 1):
        raw = llm.complete(prompt)
        try:
            obj = extract_json(raw)
            errors = validate(obj)
        except json.JSONDecodeError as e:
            errors = [f"invalid JSON: {e}"]
        print(f"  attempt {attempt}: errors={errors}")
        if not errors:
            return obj
        prompt += f"\nYour last answer had problems: {errors}. Fix them."
    raise ValueError("model never produced valid output")


if __name__ == "__main__":
    history = [
        {"role": "user", "content": "My name is Asha. I run a 12-person design studio."},
        {"role": "assistant", "content": "Nice to meet you, Asha! How can I help?"},
        {"role": "user", "content": "We use the Team plan. Billing is monthly."},
        {"role": "assistant", "content": "Got it: Team plan, billed monthly."},
        {"role": "user", "content": "Can we switch to annual billing and add 3 seats?"},
        {"role": "assistant", "content": "Yes. Annual billing saves money; seats can be added any time."},
        {"role": "user", "content": "Great, what will the new total be?"},
    ]
    total = sum(tokens(t["content"]) for t in history)
    ctx, dropped = build_context(history, budget=40)
    print(f"full history: {len(history)} turns, ~{total} tokens")
    print(f"sent to model: {len(ctx)} messages, "
          f"~{sum(tokens(t['content']) for t in ctx)} tokens, {len(dropped)} turns summarized")
    for t in ctx:
        print(f"  {t['role']:<9} {t['content']}")

    print("\nStructured output with validate-and-retry:")
    print("  result:", get_structured(SloppyFakeLLM(), "Asha Rao, asha@example.com, Team plan, 12 seats"))
