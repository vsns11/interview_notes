"""Constraint satisfaction problems (CSPs) solved by backtracking search.

1) Map colouring of Australia: variables = 7 regions, domain = {red, green, blue},
   constraint = neighbouring regions get different colours.
2) N-queens as a CSP (one variable per column, value = row) to show how much the
   MRV heuristic + forward checking prune on a bigger problem."""

AUSTRALIA = {
    "WA": ["NT", "SA"], "NT": ["WA", "SA", "Q"], "SA": ["WA", "NT", "Q", "NSW", "V"],
    "Q": ["NT", "SA", "NSW"], "NSW": ["Q", "SA", "V"], "V": ["SA", "NSW"], "T": [],
}


def solve(variables, domains, ok, smart):
    """Generic backtracking. ok(var, val, other_var, other_val) -> bool for a binary constraint."""
    stats = {"assignments": 0}

    def backtrack(assignment, domains):
        if len(assignment) == len(variables):
            return dict(assignment)
        unassigned = [v for v in variables if v not in assignment]
        # MRV (minimum remaining values): choose the most constrained variable first
        var = min(unassigned, key=lambda v: len(domains[v])) if smart else unassigned[0]
        for val in domains[var]:
            if not all(ok(var, val, o, assignment[o]) for o in assignment):
                continue
            stats["assignments"] += 1
            assignment[var] = val
            if smart:                                   # forward checking
                pruned = {v: [x for x in domains[v] if ok(var, val, v, x)] if v not in assignment else domains[v]
                          for v in variables}
                if all(pruned[v] for v in variables if v not in assignment):
                    result = backtrack(assignment, pruned)
                    if result:
                        return result
            else:
                result = backtrack(assignment, domains)
                if result:
                    return result
            del assignment[var]                         # undo and try the next value
        return None

    return backtrack({}, domains), stats["assignments"]


def main() -> None:
    colours = ["red", "green", "blue"]
    ok = lambda a, x, b, y: not (b in AUSTRALIA[a] and x == y)
    solution, n = solve(list(AUSTRALIA), {v: colours for v in AUSTRALIA}, ok, smart=True)
    print(f"Australia: {solution}  ({n} assignments)")

    print("\nN-queens as a CSP: assignments made before the first solution")
    print(f"{'N':>3}{'plain backtracking':>20}{'MRV + forward check':>22}")
    for n_q in (8, 12, 16, 20):
        cols = list(range(n_q))
        q_ok = lambda a, x, b, y: x != y and abs(x - y) != abs(a - b)
        doms = {c: list(range(n_q)) for c in cols}
        _, plain = solve(cols, doms, q_ok, smart=False)
        _, smart = solve(cols, doms, q_ok, smart=True)
        print(f"{n_q:>3}{plain:>20,}{smart:>22,}")


if __name__ == "__main__":
    main()
