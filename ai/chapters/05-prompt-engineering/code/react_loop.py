"""A minimal ReAct loop (Reason + Act): the model alternates Thought / Action,
the program runs the tool and appends an Observation, until a Final Answer.

ScriptedModel replays what a real model would write, so this runs offline.
"""
import ast
import operator
import re

FACTS = {"eiffel tower height": "330 m (including antennas, as of 2022)",
         "statue of liberty height": "93 m (ground to torch)"}


def calculator(expr: str) -> str:
    ops = {ast.Add: operator.add, ast.Sub: operator.sub, ast.Mult: operator.mul, ast.Div: operator.truediv}

    def ev(node):   # safe arithmetic only -- never eval() model output
        if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)):
            return node.value
        if isinstance(node, ast.BinOp) and type(node.op) in ops:
            return ops[type(node.op)](ev(node.left), ev(node.right))
        raise ValueError("unsupported expression")
    return str(round(ev(ast.parse(expr, mode="eval").body), 3))


def lookup(query: str) -> str:
    return FACTS.get(query.lower().strip(), "no result")


TOOLS = {"lookup": lookup, "calculator": calculator}


class ScriptedModel:
    script = [
        "Thought: I need both heights.\nAction: lookup[Eiffel Tower height]",
        "Thought: Now the Statue of Liberty.\nAction: lookup[Statue of Liberty height]",
        "Thought: Divide 330 by 93.\nAction: calculator[330 / 93]",
        "Thought: About 3.5 times.\nFinal Answer: The Eiffel Tower (330 m) is about 3.5x the Statue of Liberty (93 m).",
    ]

    def __init__(self):
        self.turn = 0

    def __call__(self, transcript: str) -> str:
        out = self.script[self.turn]
        self.turn += 1
        return out


def react(question: str, model, max_steps: int = 6) -> str:
    transcript = f"Question: {question}\n"
    for _ in range(max_steps):
        step = model(transcript)
        transcript += step + "\n"
        print(step)
        if "Final Answer:" in step:
            return step.split("Final Answer:", 1)[1].strip()
        m = re.search(r"Action: (\w+)\[(.*)\]", step)
        if not m or m.group(1) not in TOOLS:
            observation = "error: unknown or malformed action"
        else:
            observation = TOOLS[m.group(1)](m.group(2))
        transcript += f"Observation: {observation}\n"
        print(f"Observation: {observation}")
    return "stopped: step limit reached"


if __name__ == "__main__":
    print("RESULT:", react("How many times taller is the Eiffel Tower than the Statue of Liberty?", ScriptedModel()))
