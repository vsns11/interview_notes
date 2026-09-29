"""Getting reliable JSON out of a model: validate, and retry with the error.

FakeLLM stands in for a real API so the loop runs offline. It returns broken
JSON the first time and valid JSON once it is shown the validation error --
the same repair loop you would run against a real model.
"""
import json

SCHEMA = {  # tiny hand-rolled schema: field -> (type, allowed values or None)
    "category": (str, {"billing", "bug", "how-to", "account", "other"}),
    "urgency": (str, {"low", "medium", "high"}),
    "summary": (str, None),
}


def validate(text: str) -> tuple[dict | None, str | None]:
    try:
        obj = json.loads(text)
    except json.JSONDecodeError as e:
        return None, f"invalid JSON: {e.msg} at char {e.pos}"
    if not isinstance(obj, dict):
        return None, "top level must be an object"
    for key, (typ, allowed) in SCHEMA.items():
        if key not in obj:
            return None, f"missing field '{key}'"
        if not isinstance(obj[key], typ):
            return None, f"field '{key}' must be {typ.__name__}"
        if allowed and obj[key] not in allowed:
            return None, f"field '{key}' must be one of {sorted(allowed)}, got {obj[key]!r}"
    extra = set(obj) - set(SCHEMA)
    if extra:
        return None, f"unexpected fields {sorted(extra)}"
    return obj, None


class FakeLLM:
    """Scripted stand-in: realistic failure modes, then a correct answer."""

    def __init__(self):
        self.calls = 0

    def complete(self, prompt: str) -> str:
        self.calls += 1
        if self.calls == 1:   # classic failure: chatty preamble + trailing comma
            return 'Sure! Here is the JSON:\n{"category": "bug", "urgency": "critical", "summary": "Checkout 500s",}'
        if self.calls == 2:   # parses, but violates the allowed values
            return '{"category": "bug", "urgency": "critical", "summary": "Checkout returns 500 for all users"}'
        return '{"category": "bug", "urgency": "high", "summary": "Checkout returns 500 for all users"}'


def extract_with_retries(llm, prompt: str, max_attempts: int = 3) -> dict:
    current = prompt
    for attempt in range(1, max_attempts + 1):
        raw = llm.complete(current)
        obj, error = validate(raw)
        print(f"attempt {attempt}: {'OK' if obj else 'FAIL - ' + error}")
        if obj:
            return obj
        # feed the exact error back, and restate the contract
        current = (f"{prompt}\n\nYour previous reply was rejected: {error}.\n"
                   "Reply with ONLY a JSON object that matches the schema.")
    raise ValueError("model never produced valid output; fall back or escalate")


if __name__ == "__main__":
    prompt = ('Classify this ticket. Reply with JSON only: {"category": one of billing|bug|how-to|account|other, '
              '"urgency": low|medium|high, "summary": string}.\n<ticket>Checkout returns 500 for everyone.</ticket>')
    result = extract_with_retries(FakeLLM(), prompt)
    print("final:", result)
