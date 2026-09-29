"""15.9 Two-phase commit, including the blocking problem.
Participants must log PREPARED before voting yes; after that they may not decide alone."""

class Participant:
    def __init__(self, name, can_commit=True):
        self.name, self.can_commit, self.state = name, can_commit, "working"

    def prepare(self):
        self.state = "prepared" if self.can_commit else "aborted"
        return self.can_commit

    def finish(self, decision):
        self.state = decision


def run(title, parts, coordinator_crashes_after_votes=False):
    print(f"--- {title}")
    votes = {p.name: p.prepare() for p in parts}
    print("  phase 1 votes:", votes)
    if coordinator_crashes_after_votes:
        print("  coordinator crashes before sending a decision")
        for p in parts:
            if p.state == "prepared":
                print(f"  {p.name}: prepared, holding locks, cannot commit or abort alone -> BLOCKED")
        return
    decision = "committed" if all(votes.values()) else "aborted"
    print("  coordinator writes decision to its log:", decision.upper())
    for p in parts:
        p.finish(decision)
    print("  phase 2 final states:", {p.name: p.state for p in parts})


run("happy path: debit on node 1, credit on node 2",
    [Participant("node1"), Participant("node2")])
run("one participant votes no (e.g. constraint check failed)",
    [Participant("node1"), Participant("node2", can_commit=False)])
run("coordinator failure after the votes",
    [Participant("node1"), Participant("node2")], coordinator_crashes_after_votes=True)
