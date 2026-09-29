"""A complete ReAct agent loop in pure Python.

ReAct = Reason + Act. The model writes a "Thought", then either an "Action"
(tool name + JSON input) or a "Final Answer". Our code runs the tool and
appends an "Observation". Repeat until a final answer or a bound stops us.

The LLM here is a FAKE, deterministic stand-in (ScriptedLLM). It reads the
transcript exactly as a real model would receive it and replies in the same
text format, so every other line of this file is what you would write for a
real model. The tools are real: a safe calculator and a keyword search over
local documents.

Run:  python code/react_agent.py
"""
from __future__ import annotations

import ast
import json
import math
import operator
import re
from collections import Counter
from dataclasses import dataclass, field

# ----------------------------------------------------------------- the tools
# A tiny, fictional company handbook: our "local docs" to search.
DOCS = {
    "pricing.md": "Pricing. The Basic plan costs $8 per seat per month. "
                  "The Pro plan costs $20 per seat per month.",
    "billing.md": "Billing. Customers who pay annually get a 15% discount "
                  "on the monthly price. Invoices are sent on the 1st.",
    "shipping.md": "Shipping. Hardware ships from Ohio. Delivery to the USA "
                   "takes 2-4 business days; delivery to Canada takes 5-8 "
                   "business days.",
    "refunds.md": "Refunds. Unused seats can be refunded within 30 days of "
                  "purchase. Refunds above $500 need manager approval.",
    "offices.md": "Offices. Acme has offices in Austin and Toronto.",
}

_OPS = {ast.Add: operator.add, ast.Sub: operator.sub, ast.Mult: operator.mul,
        ast.Div: operator.truediv, ast.Pow: operator.pow,
        ast.USub: operator.neg, ast.UAdd: operator.pos}


def calculator(expression: str) -> str:
    """Evaluate + - * / ** and brackets safely (no eval(), no names)."""
    def ev(node):
        if isinstance(node, ast.Expression):
            return ev(node.body)
        if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)):
            return node.value
        if isinstance(node, ast.BinOp) and type(node.op) in _OPS:
            return _OPS[type(node.op)](ev(node.left), ev(node.right))
        if isinstance(node, ast.UnaryOp) and type(node.op) in _OPS:
            return _OPS[type(node.op)](ev(node.operand))
        raise ValueError(f"unsupported syntax: {type(node).__name__}")
    value = ev(ast.parse(expression, mode="eval"))
    if isinstance(value, float) and value.is_integer():
        value = int(value)
    return str(round(value, 4) if isinstance(value, float) else value)


def _tokens(text: str) -> list[str]:
    return re.findall(r"[a-z0-9]+", text.lower())


def search_docs(query: str, k: int = 2) -> str:
    """Rank docs by TF-IDF-style keyword overlap; return the top k."""
    n = len(DOCS)
    df = Counter(t for body in DOCS.values() for t in set(_tokens(body)))
    scores = {}
    for name, body in DOCS.items():
        tf = Counter(_tokens(body))
        scores[name] = sum(tf[t] * math.log(1 + n / df[t])
                           for t in _tokens(query) if t in tf)
    best = [d for d, s in sorted(scores.items(), key=lambda x: -x[1]) if s > 0][:k]
    if not best:
        return "NO_RESULTS: nothing in the handbook matches that query."
    return " | ".join(f"[{d}] {DOCS[d]}" for d in best)


TOOLS = {
    "calculator": {"fn": calculator,
                   "description": "Do arithmetic. Input: {\"expression\": \"2*(3+4)\"}"},
    "search_docs": {"fn": search_docs,
                    "description": "Search the company handbook. Input: {\"query\": \"...\"}"},
}

SYSTEM_PROMPT = """You answer questions using tools. Use this exact format:
Thought: <your reasoning>
Action: <one of: {names}>
Action Input: <JSON object>
... then you will receive an Observation. When you know the answer write:
Thought: <reasoning>
Final Answer: <answer>
Tools:
{descriptions}"""


# -------------------------------------------------------- the fake "LLM"
class ScriptedLLM:
    """Deterministic stand-in for a chat model.

    It only sees the prompt text (like a real model) and replies with ReAct
    text. Its "skills" are a few hand-written rules; they READ the
    observations, so the numbers in the final answer come from the tools.
    """

    def complete(self, prompt: str) -> str:
        question = re.search(r"Question: (.*)", prompt).group(1).lower()
        obs = re.findall(r"Observation: (.*)", prompt)
        if "pro plan" in question:
            return self._pricing(obs)
        if "canada" in question:
            return self._shipping(obs)
        return self._unknown(obs)

    def _pricing(self, obs):
        joined = " ".join(obs)
        price = re.search(r"Pro plan costs \$(\d+)", joined)
        disc = re.search(r"(\d+)% discount", joined)
        if not price:
            return ('Thought: I need the Pro plan price first.\n'
                    'Action: search_docs\nAction Input: {"query": "Pro plan price"}')
        if not disc:
            return ('Thought: Pro is $%s/seat/month. Now I need the annual discount.\n'
                    'Action: search_docs\nAction Input: {"query": "annual discount billing"}'
                    % price.group(1))
        if not re.search(r"^[\d.]+$", obs[-1]):
            expr = f"3 * {price.group(1)} * 12 * (1 - {disc.group(1)}/100)"
            return ('Thought: Pro is $%s, annual discount %s%%. Compute 3 seats x 12 months.\n'
                    % (price.group(1), disc.group(1)) +
                    f'Action: calculator\nAction Input: {{"expression": "{expr}"}}')
        return (f"Thought: The calculator returned {obs[-1]}.\n"
                f"Final Answer: 3 Pro seats billed annually cost ${obs[-1]} per year.")

    def _shipping(self, obs):
        if not obs:  # a typical failure: calling a tool that does not exist
            return ('Thought: I will look up shipping times.\n'
                    'Action: lookup_shipping\nAction Input: {"country": "Canada"}')
        if obs[-1].startswith("ERROR"):
            return ('Thought: That tool does not exist; search the handbook instead.\n'
                    'Action: search_docs\nAction Input: {"query": "delivery Canada days"}')
        days = re.search(r"Canada takes ([\d-]+) business days", obs[-1]).group(1)
        return f"Thought: Found it.\nFinal Answer: Delivery to Canada takes {days} business days."

    def _unknown(self, obs):  # keeps repeating the same search: a stuck loop
        return ('Thought: Maybe the handbook mentions it.\n'
                'Action: search_docs\nAction Input: {"query": "CEO favourite colour"}')


# ------------------------------------------------------------ the agent loop
@dataclass
class Step:
    thought: str
    action: str | None = None
    action_input: dict | None = None
    observation: str | None = None
    prompt_tokens: int = 0


@dataclass
class AgentResult:
    answer: str
    stopped_because: str
    steps: list[Step] = field(default_factory=list)


def parse(reply: str) -> tuple[str, str | None, dict | None, str | None]:
    """Return (thought, action, action_input, final_answer)."""
    thought = (re.search(r"Thought: (.*)", reply) or [None, ""])[1]
    final = re.search(r"Final Answer: (.*)", reply, re.S)
    if final:
        return thought, None, None, final.group(1).strip()
    action = re.search(r"Action: (\S+)", reply)
    raw = re.search(r"Action Input: (\{.*\})", reply, re.S)
    if not action or not raw:
        raise ValueError("reply has neither a Final Answer nor Action + Action Input")
    return thought, action.group(1), json.loads(raw.group(1)), None


def run_tool(name: str, args: dict) -> str:
    """Execute a tool; turn every failure into a readable observation."""
    if name not in TOOLS:
        return f"ERROR unknown tool '{name}'. Available tools: {sorted(TOOLS)}"
    try:
        return TOOLS[name]["fn"](**args)
    except TypeError as e:  # wrong / hallucinated argument names
        return f"ERROR bad arguments for {name}: {e}"
    except Exception as e:  # tool bug, bad expression, ...
        return f"ERROR {type(e).__name__}: {e}"


def react(question: str, llm, max_steps: int = 6) -> AgentResult:
    header = SYSTEM_PROMPT.format(
        names=", ".join(TOOLS),
        descriptions="\n".join(f"- {n}: {t['description']}" for n, t in TOOLS.items()))
    transcript = f"{header}\nQuestion: {question}\n"
    steps: list[Step] = []
    seen_actions: set[str] = set()
    for _ in range(max_steps):                      # bound 1: step budget
        reply = llm.complete(transcript)
        tokens = len(transcript) // 4                # ~4 chars per token
        try:
            thought, action, args, final = parse(reply)
        except (ValueError, json.JSONDecodeError) as e:
            transcript += f"{reply}\nObservation: ERROR could not parse reply ({e})\n"
            steps.append(Step("<unparseable>", observation=str(e), prompt_tokens=tokens))
            continue
        if final is not None:
            steps.append(Step(thought, prompt_tokens=tokens))
            return AgentResult(final, "final_answer", steps)
        key = f"{action}:{json.dumps(args, sort_keys=True)}"
        if key in seen_actions:                     # bound 2: no-progress detector
            steps.append(Step(thought, action, args, "(repeat blocked)", tokens))
            return AgentResult("I could not find this; escalating to a human.",
                               "repeated_action", steps)
        seen_actions.add(key)
        observation = run_tool(action, args)
        steps.append(Step(thought, action, args, observation, tokens))
        transcript += f"{reply}\nObservation: {observation}\n"
    return AgentResult("Step budget used up; escalating to a human.", "max_steps", steps)


def show(result: AgentResult) -> None:
    for i, s in enumerate(result.steps, 1):
        print(f"  [{i}] Thought: {s.thought}   (prompt ~{s.prompt_tokens} tokens)")
        if s.action:
            print(f"      Action: {s.action} {json.dumps(s.action_input)}")
            print(f"      Observation: {s.observation[:90]}")
    print(f"  => {result.answer}   [stopped: {result.stopped_because}]")


if __name__ == "__main__":
    llm = ScriptedLLM()
    for q in ["How much do 3 Pro plan seats cost per year if we pay annually?",
              "How long does delivery to Canada take?",
              "What is the CEO's favourite colour?"]:
        print(f"Q: {q}")
        r = react(q, llm)
        show(r)
        total = sum(s.prompt_tokens for s in r.steps)
        print(f"  total input tokens sent across steps: ~{total}\n")
