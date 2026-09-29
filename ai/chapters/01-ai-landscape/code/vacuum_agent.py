"""The classic two-square vacuum world: a rational agent in 25 lines.

An agent maps percepts to actions. We compare a simple reflex agent with a
random agent using a performance measure: +1 per clean square per time step."""
import random

ACTIONS = ["Left", "Right", "Suck"]


def reflex_agent(percept):
    location, status = percept
    if status == "Dirty":
        return "Suck"
    return "Right" if location == "A" else "Left"


def random_agent(percept):
    return random.choice(ACTIONS)


def run(agent, steps=10, seed=0):
    random.seed(seed)
    world = {"A": "Dirty", "B": "Dirty"}
    loc, score = "A", 0
    for _ in range(steps):
        action = agent((loc, world[loc]))          # perceive -> decide
        if action == "Suck":                        # act on the environment
            world[loc] = "Clean"
        elif action == "Right":
            loc = "B"
        else:
            loc = "A"
        score += sum(v == "Clean" for v in world.values())   # performance measure
    return score


def main() -> None:
    print("reflex agent score :", run(reflex_agent))
    print("random agent score :", sum(run(random_agent, seed=s) for s in range(100)) / 100,
          "(mean of 100 runs)")
    print("best possible      :", 1 + 1 + 2 * 8, "(suck A, move, suck B, then 8 steps fully clean)")


if __name__ == "__main__":
    main()
