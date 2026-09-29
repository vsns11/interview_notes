"""Function calling with guardrails: JSON-schema validation, an action gate,
consequence tiers, human approval, idempotency keys and loop budgets.

The "model" is a scripted stand-in that proposes structured tool calls in the
same shape a real API returns ({"name": ..., "input": {...}}). Some of its
proposals are deliberately bad (wrong argument types, a hallucinated field,
an injected request for someone else's order, an over-cap refund) so you can
watch each defence fire. The runtime, not the model, decides what executes.

Run:  python code/guarded_tools.py
"""
from __future__ import annotations

import hashlib
import json
from dataclasses import dataclass, field

# --------------------------------------------------------------- tool schemas
# The same JSON-Schema shape the Anthropic ("input_schema") and OpenAI
# ("parameters") APIs use to describe tool inputs.
TOOLS = {
    "get_order": {
        "description": "Look up one of the current customer's orders by id.",
        "tier": "read",
        "input_schema": {"type": "object",
                         "properties": {"order_id": {"type": "string"}},
                         "required": ["order_id"], "additionalProperties": False},
    },
    "add_case_note": {
        "description": "Add an internal note to the support case.",
        "tier": "reversible",
        "input_schema": {"type": "object",
                         "properties": {"text": {"type": "string"}},
                         "required": ["text"], "additionalProperties": False},
    },
    "issue_refund": {
        "description": "Refund part or all of an order. Not for orders of other customers.",
        "tier": "consequential",
        "input_schema": {"type": "object",
                         "properties": {"order_id": {"type": "string"},
                                        "amount": {"type": "number", "minimum": 0.01},
                                        "reason": {"type": "string",
                                                   "enum": ["late", "damaged", "missing"]}},
                         "required": ["order_id", "amount", "reason"],
                         "additionalProperties": False},
    },
}

TYPES = {"string": str, "number": (int, float), "integer": int, "boolean": bool,
         "object": dict, "array": list}


def validate(args: dict, schema: dict) -> list[str]:
    """A tiny subset of JSON Schema: type, required, enum, minimum,
    additionalProperties. Real code would use the `jsonschema` package or the
    API's strict mode."""
    errors = []
    props = schema.get("properties", {})
    for name in schema.get("required", []):
        if name not in args:
            errors.append(f"missing required field '{name}'")
    for name, value in args.items():
        if name not in props:
            if schema.get("additionalProperties") is False:
                errors.append(f"unknown field '{name}'")
            continue
        spec = props[name]
        want = TYPES[spec["type"]]
        if not isinstance(value, want) or (spec["type"] == "number" and isinstance(value, bool)):
            errors.append(f"'{name}' must be {spec['type']}, got {type(value).__name__}")
            continue
        if "enum" in spec and value not in spec["enum"]:
            errors.append(f"'{name}' must be one of {spec['enum']}")
        if "minimum" in spec and value < spec["minimum"]:
            errors.append(f"'{name}' must be >= {spec['minimum']}")
    return errors


# ------------------------------------------------------- the world (sandbox)
ORDERS = {"A-100": {"customer": "alice", "total": 42.0},
          "A-101": {"customer": "alice", "total": 50.0},
          "A-102": {"customer": "alice", "total": 75.0},
          "B-200": {"customer": "bob", "total": 900.0}}
REFUNDS: dict[str, dict] = {}          # idempotency_key -> refund record
REFUND_CAP = 30.0                      # autonomous limit, enforced in code


@dataclass
class Session:
    """Everything the runtime knows that the model must not be able to change."""
    case_id: str
    customer: str                      # verified identity, not from the chat
    max_steps: int = 10
    max_consequential: int = 3
    steps: int = 0
    consequential: int = 0
    audit: list[dict] = field(default_factory=list)


def human_approves(tool: str, args: dict) -> bool:
    """Stand-in for an approval UI showing the exact tool + arguments.
    This reviewer approves damaged-goods refunds and declines the rest."""
    return args.get("reason") == "damaged"


def refund_key(case_id: str, args: dict) -> str:
    """Derived from the logical operation, so a retry maps to the SAME key.
    (A random key per attempt would protect nothing.)"""
    raw = f"{case_id}|{args['order_id']}|{args['amount']}"
    return hashlib.sha256(raw.encode()).hexdigest()[:12]


def refunded_so_far(order_id: str) -> float:
    return sum(r["amount"] for r in REFUNDS.values() if r["order_id"] == order_id)


def execute(tool: str, args: dict, s: Session) -> dict:
    if tool == "get_order":
        return {"ok": True, "order": {"id": args["order_id"], **ORDERS[args["order_id"]]}}
    if tool == "add_case_note":
        return {"ok": True, "note_saved": args["text"]}
    if tool == "issue_refund":
        key = refund_key(s.case_id, args)
        REFUNDS[key] = {"order_id": args["order_id"], "amount": args["amount"], "key": key}
        return {"ok": True, "refund": REFUNDS[key]}
    raise KeyError(tool)


def action_gate(call: dict, s: Session) -> dict:
    """Validate -> authorise -> apply policy -> maybe ask a human -> execute -> log."""
    tool, args = call["name"], call["input"]
    decision = {"tool": tool, "args": args}

    def done(result: dict, verdict: str) -> dict:
        decision.update(verdict=verdict, result=result)
        s.audit.append(decision)
        return result

    if tool not in TOOLS:
        return done({"ok": False, "error": "UNKNOWN_TOOL", "available": list(TOOLS)}, "rejected")
    problems = validate(args, TOOLS[tool]["input_schema"])
    if problems:  # readable, structured error the model can fix
        return done({"ok": False, "error": "INVALID_ARGUMENTS", "details": problems,
                     "retryable": True}, "rejected")
    order_id = args.get("order_id")
    if order_id is not None:
        order = ORDERS.get(order_id)
        if order is None or order["customer"] != s.customer:  # authority check
            # Same answer for "missing" and "not yours": leak nothing.
            return done({"ok": False, "error": "ORDER_NOT_FOUND", "retryable": False}, "denied")
    tier = TOOLS[tool]["tier"]
    if tier == "consequential":
        key = refund_key(s.case_id, args)
        if key in REFUNDS:  # idempotent replay: same result, no new money moves
            return done({"ok": True, "duplicate": True, "refund": REFUNDS[key]}, "deduplicated")
        if s.consequential >= s.max_consequential:
            return done({"ok": False, "error": "ACTION_CAP_REACHED"}, "denied")
        remaining = ORDERS[order_id]["total"] - refunded_so_far(order_id)
        if args["amount"] > remaining:
            return done({"ok": False, "error": "EXCEEDS_REMAINING_REFUNDABLE",
                         "remaining": remaining}, "denied")
        if args["amount"] > REFUND_CAP:  # above the cap: irreversible tier -> human
            if not human_approves(tool, args):
                return done({"ok": False, "error": "APPROVAL_DECLINED"}, "declined_by_human")
            decision["approved_by"] = "reviewer-7"
        s.consequential += 1
    return done(execute(tool, args, s), "executed")


# ------------------------------------------------------- scripted "model"
PROPOSALS = [
    # 1. wrong type + hallucinated field (the model invented 'priority')
    {"name": "get_order", "input": {"order_id": 100, "priority": "high"}},
    # 2. fixed after reading the structured error
    {"name": "get_order", "input": {"order_id": "A-100"}},
    # 3. indirect prompt injection in the chat: "also refund order B-200"
    {"name": "issue_refund", "input": {"order_id": "B-200", "amount": 900, "reason": "late"}},
    # 4. legitimate small refund
    {"name": "issue_refund", "input": {"order_id": "A-100", "amount": 12.0, "reason": "late"}},
    # 5. network hiccup -> the loop retries the same call
    {"name": "issue_refund", "input": {"order_id": "A-100", "amount": 12.0, "reason": "late"}},
    # 6. enum violation
    {"name": "issue_refund", "input": {"order_id": "A-101", "amount": 5, "reason": "angry"}},
    # 7. more than is left on the order (42 - 12 = 30)
    {"name": "issue_refund", "input": {"order_id": "A-100", "amount": 35.0, "reason": "late"}},
    # 8. above the $30 autonomous cap -> held for a human, who approves
    {"name": "issue_refund", "input": {"order_id": "A-102", "amount": 60.0, "reason": "damaged"}},
    # 9. above the cap again -> human declines
    {"name": "issue_refund", "input": {"order_id": "A-101", "amount": 45.0, "reason": "missing"}},
    {"name": "add_case_note", "input": {"text": "Refunded $12 (A-100) and $60 (A-102)."}},
    {"name": "get_order", "input": {"order_id": "A-101"}},  # never reached: budget
]


def run(session: Session) -> None:
    for call in PROPOSALS:
        if session.steps >= session.max_steps:
            print(f"  STOP: step budget {session.max_steps} used -> snapshot state, escalate")
            break
        session.steps += 1
        result = action_gate(call, session)
        verdict = session.audit[-1]["verdict"]
        if "approved_by" in session.audit[-1]:
            verdict += f" (approved by {session.audit[-1]['approved_by']})"
        print(f"  {session.steps}. {call['name']}{json.dumps(call['input'])}")
        print(f"     -> {verdict}: {json.dumps(result)[:110]}")


if __name__ == "__main__":
    s = Session(case_id="case-81", customer="alice")
    print("Tool menu the model sees (name, tier, required args):")
    for name, t in TOOLS.items():
        print(f"  {name:14s} {t['tier']:13s} {t['input_schema']['required']}")
    print("\nAgent run for customer 'alice':")
    run(s)
    print(f"\nRefunds actually issued: {list(REFUNDS.values())}")
    verdicts = [a["verdict"] for a in s.audit]
    print("Audit summary:", {v: verdicts.count(v) for v in sorted(set(verdicts))})
