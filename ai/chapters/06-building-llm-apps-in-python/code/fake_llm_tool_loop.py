"""A local stand-in for an LLM API that shows the tool-calling loop end to end.

FakeLLM mimics the *shape* of the Anthropic Messages API (content blocks,
stop_reason, usage) without any network call. It "decides" to call tools with
simple rules, so the loop logic around it is exactly what you would write for a
real model:

  send messages + tool schemas -> model returns tool_use blocks -> your code
  runs the tools -> you send tool_result blocks back -> repeat until end_turn.
"""
from __future__ import annotations

import json
import re
from dataclasses import dataclass, field


# ---------------------------------------------------------------- data shapes
@dataclass
class Block:
    type: str                      # "text" | "tool_use"
    text: str = ""
    id: str = ""
    name: str = ""
    input: dict = field(default_factory=dict)


@dataclass
class Response:
    content: list[Block]
    stop_reason: str               # "end_turn" | "tool_use" | "max_tokens"
    usage: dict


def rough_tokens(text: str) -> int:
    """~4 characters per token is a common rule of thumb for English."""
    return max(1, len(text) // 4)


# ---------------------------------------------------------------- the fake model
class FakeLLM:
    """Rule-based 'model'. Real models decide from learned weights; the loop is the same."""

    def __init__(self) -> None:
        self._next_id = 0

    def _id(self) -> str:
        self._next_id += 1
        return f"toolu_{self._next_id:03d}"

    def create(self, *, system: str, messages: list[dict], tools: list[dict],
               max_tokens: int = 512) -> Response:
        prompt_text = system + json.dumps(messages, default=str) + json.dumps(tools)
        last = messages[-1]
        tool_names = {t["name"] for t in tools}

        # Case 1: the last user turn carries tool results -> write the final answer.
        if isinstance(last["content"], list) and last["content"][0].get("type") == "tool_result":
            parts = []
            for r in last["content"]:
                if r.get("is_error"):
                    parts.append(f"I could not get one result ({r['content']}).")
                else:
                    parts.append(r["content"])
            answer = " ".join(parts)
            return self._reply([Block("text", text=answer)], "end_turn", prompt_text)

        # Case 2: a fresh question -> decide which tools to call (possibly several).
        question = last["content"]
        calls = []
        m = re.search(r"weather in ([A-Z][a-z]+)", question)
        if m and "get_weather" in tool_names:
            calls.append(Block("tool_use", id=self._id(), name="get_weather",
                               input={"city": m.group(1)}))
        m = re.search(r"(\d+(?:\.\d+)?) ([A-Z]{3}) in ([A-Z]{3})", question)
        if m and "convert_currency" in tool_names:
            calls.append(Block("tool_use", id=self._id(), name="convert_currency",
                               input={"amount": float(m.group(1)), "from": m.group(2),
                                      "to": m.group(3)}))
        if calls:
            preamble = Block("text", text="Let me look that up.")
            return self._reply([preamble, *calls], "tool_use", prompt_text)
        return self._reply([Block("text", text="I can only help with weather and currency.")],
                           "end_turn", prompt_text)

    def stream(self, **kwargs):
        """Yield the final text in small pieces, like server-sent events."""
        resp = self.create(**kwargs)
        text = "".join(b.text for b in resp.content if b.type == "text")
        for i in range(0, len(text), 12):
            yield text[i:i + 12]

    @staticmethod
    def _reply(blocks, stop_reason, prompt_text) -> Response:
        out = " ".join(b.text or json.dumps(b.input) for b in blocks)
        return Response(blocks, stop_reason,
                        {"input_tokens": rough_tokens(prompt_text),
                         "output_tokens": rough_tokens(out)})


# ---------------------------------------------------------------- your tools
WEATHER = {"Paris": "18°C and cloudy", "Tokyo": "24°C and sunny"}
RATES = {("USD", "EUR"): 0.92, ("EUR", "USD"): 1.09}   # made-up demo rates


def get_weather(city: str) -> str:
    if city not in WEATHER:
        raise ValueError(f"no weather data for {city}")
    return f"Weather in {city}: {WEATHER[city]}."


def convert_currency(amount: float, **kw) -> str:
    src, dst = kw["from"], kw["to"]
    return f"{amount:g} {src} is about {amount * RATES[(src, dst)]:.2f} {dst}."


TOOLS = [  # JSON Schema tool definitions: this is what the model sees
    {"name": "get_weather", "description": "Current weather for a city.",
     "input_schema": {"type": "object", "properties": {"city": {"type": "string"}},
                      "required": ["city"]}},
    {"name": "convert_currency", "description": "Convert money between ISO currencies.",
     "input_schema": {"type": "object",
                      "properties": {"amount": {"type": "number"},
                                     "from": {"type": "string"}, "to": {"type": "string"}},
                      "required": ["amount", "from", "to"]}},
]
REGISTRY = {"get_weather": get_weather, "convert_currency": convert_currency}


def validate(args: dict, schema: dict) -> None:
    """Minimal check: required keys present. Real code: use jsonschema/pydantic."""
    missing = [k for k in schema.get("required", []) if k not in args]
    if missing:
        raise ValueError(f"missing arguments: {missing}")


# ---------------------------------------------------------------- the loop
def run_agent(llm: FakeLLM, question: str, max_steps: int = 5) -> str:
    messages = [{"role": "user", "content": question}]
    schemas = {t["name"]: t["input_schema"] for t in TOOLS}
    total_in = total_out = 0
    for step in range(1, max_steps + 1):
        resp = llm.create(system="You are a helpful travel assistant.",
                          messages=messages, tools=TOOLS)
        total_in += resp.usage["input_tokens"]
        total_out += resp.usage["output_tokens"]
        print(f"step {step}: stop_reason={resp.stop_reason}, "
              f"blocks={[b.type for b in resp.content]}")
        if resp.stop_reason != "tool_use":
            print(f"tokens used: input={total_in}, output={total_out}")
            return "".join(b.text for b in resp.content if b.type == "text")

        # 1) keep the assistant turn (including tool_use blocks) in the history
        messages.append({"role": "assistant", "content": resp.content})
        # 2) run every requested tool; send ALL results back in ONE user message
        results = []
        for block in resp.content:
            if block.type != "tool_use":
                continue
            try:
                validate(block.input, schemas[block.name])
                output = REGISTRY[block.name](**block.input)
                results.append({"type": "tool_result", "tool_use_id": block.id,
                                "content": output})
            except Exception as exc:               # report errors, don't crash
                results.append({"type": "tool_result", "tool_use_id": block.id,
                                "content": str(exc), "is_error": True})
            print(f"   ran {block.name}({block.input}) -> {results[-1]['content']}")
        messages.append({"role": "user", "content": results})
    return "Stopped: too many tool steps."        # guard against infinite loops


if __name__ == "__main__":
    llm = FakeLLM()
    q1 = "What's the weather in Paris, and what is 100 USD in EUR?"
    print("Q1:", q1)
    print("A1:", run_agent(llm, q1), "\n")

    q2 = "What's the weather in Oslo?"
    print("Q2:", q2)
    print("A2:", run_agent(llm, q2), "\n")

    print("Streaming the same final answer in chunks:")
    msgs = [{"role": "user", "content": [{"type": "tool_result", "tool_use_id": "x",
                                          "content": "Weather in Tokyo: 24°C and sunny."}]}]
    for chunk in llm.stream(system="", messages=msgs, tools=TOOLS):
        print(repr(chunk))
