"""Plan-and-execute with a verifier and reflection (Reflexion-style lessons).

1. PLANNER writes the whole plan up front (a list of tool steps).
2. EXECUTOR runs the steps one by one; no LLM call per step.
3. VERIFIER checks the outcome against the goal with plain code.
4. On failure, REFLECTION writes a short lesson into memory and the planner
   re-plans with that lesson in its context. Lessons persist across attempts.

Planner and reflector are deterministic stand-ins for LLM calls; the rooms
"database" and the checks are real code.

Run:  python code/plan_execute_reflect.py
"""
from __future__ import annotations

from dataclasses import dataclass

ROOMS = {  # name -> capacity, booked slots
    "Aspen": {"capacity": 4, "booked": set()},
    "Birch": {"capacity": 8, "booked": {"fri-pm"}},
    "Cedar": {"capacity": 10, "booked": set()},
}
BOOKINGS: list[tuple[str, str, int]] = []


# ------------------------------------------------------------------- tools
def list_rooms() -> list[str]:
    return sorted(ROOMS)


def room_info(name: str) -> dict:
    return {"name": name, "capacity": ROOMS[name]["capacity"]}


def is_free(name: str, slot: str) -> bool:
    return slot not in ROOMS[name]["booked"]


def book(name: str, slot: str, people: int) -> str:
    if slot in ROOMS[name]["booked"]:
        return f"FAILED: {name} already booked for {slot}"
    ROOMS[name]["booked"].add(slot)
    BOOKINGS.append((name, slot, people))
    return f"BOOKED {name} for {slot}"


TOOLS = {"list_rooms": list_rooms, "room_info": room_info, "is_free": is_free, "book": book}


# ------------------------------------------------ stand-ins for LLM calls
@dataclass
class Goal:
    people: int
    slot: str


def planner(goal: Goal, lessons: list[str]) -> list[tuple[str, dict]]:
    """A 'model' that writes a plan. With no lessons it makes the naive
    choice (first room alphabetically); lessons change its behaviour."""
    rooms = list_rooms()
    if any("capacity" in l for l in lessons):
        rooms = [r for r in rooms if room_info(r)["capacity"] >= goal.people]
    if any("availability" in l for l in lessons):
        rooms = [r for r in rooms if is_free(r, goal.slot)]
        # a smarter plan: check first, then act
        return [("is_free", {"name": rooms[0], "slot": goal.slot}),
                ("book", {"name": rooms[0], "slot": goal.slot, "people": goal.people})]
    return [("book", {"name": rooms[0], "slot": goal.slot, "people": goal.people})]


def verifier(goal: Goal) -> tuple[bool, str]:
    """Checks the WORLD, not the agent's claim that it succeeded."""
    ok = [b for b in BOOKINGS if b[1] == goal.slot and ROOMS[b[0]]["capacity"] >= goal.people]
    if ok:
        return True, f"verified: {ok[-1][0]} holds {ROOMS[ok[-1][0]]['capacity']} >= {goal.people}"
    if BOOKINGS and BOOKINGS[-1][1] == goal.slot:
        name = BOOKINGS[-1][0]
        return False, f"{name} fits only {ROOMS[name]['capacity']} people"
    return False, "no booking exists for the slot"


def reflect(error: str) -> str:
    """A 'model' turning a failure into a reusable, specific lesson."""
    if "fits only" in error:
        return "Filter rooms by capacity >= party size before booking."
    return "Check availability before booking; do not assume a room is free."


def undo_last_booking() -> None:
    name, slot, _ = BOOKINGS.pop()
    ROOMS[name]["booked"].discard(slot)


# ------------------------------------------------------------- the driver
def run(goal: Goal, max_attempts: int = 3) -> None:
    lessons: list[str] = []                 # long-lived "reflection memory"
    for attempt in range(1, max_attempts + 1):
        plan = planner(goal, lessons)
        print(f"Attempt {attempt}: plan = {[(t, a) for t, a in plan]}")
        for tool, args in plan:              # executor: no LLM needed per step
            out = TOOLS[tool](**args)
            print(f"   run {tool}{args} -> {out}")
            if out is False or str(out).startswith("FAILED"):
                break
        ok, why = verifier(goal)
        print(f"   verifier: {'PASS' if ok else 'FAIL'} ({why})")
        if ok:
            return
        if BOOKINGS and BOOKINGS[-1][1] == goal.slot:
            undo_last_booking()                # compensate the bad side effect
            print("   compensating action: cancelled the wrong booking")
        lesson = reflect(why)
        if lesson in lessons:                   # same failure twice -> new lesson
            lesson = reflect("no booking")
        lessons.append(lesson)
        print(f"   reflection -> lesson stored: '{lesson}'")
    print("Gave up after max attempts; escalate with lessons:", lessons)


if __name__ == "__main__":
    run(Goal(people=6, slot="fri-pm"))
    print("Final bookings:", BOOKINGS)
