"""Chapter 11 - functional-dependency toolkit used to verify every worked answer in the chapter.

Pure Python, no packages. Attributes are strings; a dependency is (frozenset lhs, frozenset rhs).
Run:  python fd_tools.py
"""
from itertools import combinations


def fd(lhs, rhs):
    """fd('A B', 'C') -> ({'A','B'}, {'C'})"""
    return frozenset(lhs.split()), frozenset(rhs.split())


def closure(attrs, fds, trace=False):
    """X+ : keep adding the right side of any FD whose WHOLE left side is already in the set."""
    result = set(attrs)
    changed = True
    step = 0
    while changed:
        changed = False
        for lhs, rhs in fds:
            if lhs <= result and not rhs <= result:
                result |= rhs
                changed = True
                step += 1
                if trace:
                    print(f"    step {step}: {fmt(lhs)} -> {fmt(rhs)} fires, set = {fmt(result)}")
    return frozenset(result)


def fmt(s):
    return "{" + ", ".join(sorted(s)) + "}"


def implies(fds, lhs, rhs):
    """Does X -> Y follow from the FDs?  Exactly when Y is inside X+."""
    return frozenset(rhs) <= closure(lhs, fds)


def candidate_keys(R, fds):
    """All minimal sets whose closure is R. Attributes never on a right side must be in every key."""
    R = frozenset(R)
    on_right = set().union(*(r - l for l, r in fds)) if fds else set()
    core = R - on_right                              # must be in every key
    rest = sorted(R - core)
    keys = []
    for size in range(0, len(rest) + 1):
        for extra in combinations(rest, size):
            cand = core | frozenset(extra)
            if any(k <= cand for k in keys):       # a superset of a key is not minimal
                continue
            if closure(cand, fds) >= R:
                keys.append(cand)
    return keys


def prime_attributes(R, fds):
    return frozenset().union(*candidate_keys(R, fds))


def split_rhs(fds):
    return [(l, frozenset([a])) for l, r in fds for a in r if a not in l]   # drop trivial parts


def violations(R, fds):
    """Return lists of 2NF, 3NF and BCNF violations for relation R (FDs must mention only R)."""
    R = frozenset(R)
    keys = candidate_keys(R, fds)
    prime = frozenset().union(*keys)
    v2, v3, vb = [], [], []
    partial = {}                                                  # 2NF: non-prime on part of a key
    for k in keys:
        for size in range(1, len(k)):
            for part in combinations(sorted(k), size):
                hit = (closure(part, fds) - frozenset(part)) & (R - prime)
                if hit:
                    partial.setdefault(frozenset(part), set()).update(hit)
    v2 = [f"{fmt(p)} -> {fmt(a)}" for p, a in partial.items()]
    for lhs, rhs in split_rhs(fds):
        superkey = closure(lhs, fds) >= R
        if not superkey:
            vb.append(f"{fmt(lhs)} -> {fmt(rhs)}")
            if not rhs <= prime:
                v3.append(f"{fmt(lhs)} -> {fmt(rhs)}")
    return sorted(set(v2)), v3, vb


def highest_nf(R, fds):
    v2, v3, vb = violations(R, fds)
    if v2:
        return "1NF"
    if v3:
        return "2NF"
    if vb:
        return "3NF"
    return "BCNF"


def minimal_cover(fds):
    """Single-attribute right sides, no extraneous left attributes, no redundant FDs."""
    g = split_rhs(fds)
    changed = True
    while changed:                                            # remove extraneous LHS attributes
        changed = False
        for i, (l, r) in enumerate(g):
            for a in sorted(l):
                smaller = l - {a}
                if smaller and r <= closure(smaller, g):
                    g[i] = (smaller, r)
                    changed = True
                    break
    out = list(dict.fromkeys(g))                              # remove duplicates, keep order
    for f in list(out):                                       # remove redundant FDs
        others = [x for x in out if x != f]
        if f[1] <= closure(f[0], others):
            out = others
    return out


def project(fds, S):
    """FDs that hold on sub-schema S (brute force over subsets; fine for class-sized examples)."""
    S = frozenset(S)
    out = []
    for size in range(1, len(S)):
        for lhs in combinations(sorted(S), size):
            rhs = (closure(lhs, fds) & S) - frozenset(lhs)
            if rhs:
                out.append((frozenset(lhs), rhs))
    return minimal_cover(out) if out else []


def bcnf_decompose(R, fds):
    """Classic algorithm: while some piece has an FD X -> Y with X not a superkey, split on it."""
    todo, done = [frozenset(R)], []
    while todo:
        Ri = todo.pop(0)
        Fi = project(fds, Ri)
        bad = None
        for lhs, rhs in Fi:
            if not closure(lhs, Fi) >= Ri:
                bad = lhs
                break
        if bad is None:
            done.append(Ri)
        else:
            left = closure(bad, Fi) & Ri                   # X plus everything X determines here
            todo += [left, (Ri - left) | bad]
    return done


def synthesize_3nf(R, fds):
    """3NF synthesis: one table per minimal-cover group, plus a key table if no table holds a key."""
    mc = minimal_cover(fds)
    groups = {}
    for l, r in mc:
        groups.setdefault(l, set()).update(r)
    tables = [frozenset(l | r) for l, r in groups.items()]
    if not any(closure(t, fds) >= frozenset(R) for t in tables):
        tables.append(candidate_keys(R, fds)[0])
    return [t for t in tables if not any(t < u for u in tables)]


def preserved(fds, pieces):
    """Is every FD checkable inside one piece (or derivable from FDs that are)?"""
    local = [f for p in pieces for f in project(fds, p)]
    return [f"{fmt(l)} -> {fmt(r)}" for l, r in fds if not r <= closure(l, local)]


def report(title, R, fds, show_keys=True):
    print(f"\n=== {title}")
    print("R =", fmt(R))
    for l, r in fds:
        print("   ", fmt(l), "->", fmt(r))
    keys = candidate_keys(R, fds)
    if show_keys:
        print("candidate keys:", ", ".join(fmt(k) for k in keys))
        print("prime:", fmt(frozenset().union(*keys)), " non-prime:", fmt(frozenset(R) - frozenset().union(*keys)))
    v2, v3, vb = violations(R, fds)
    print("2NF violations:", v2 or "none")
    print("3NF violations:", v3 or "none")
    print("BCNF violations:", vb or "none")
    print("highest normal form:", highest_nf(R, fds))


if __name__ == "__main__":
    # --- 11.2-11.5: the order_line_wide running example -------------------------------------
    R = "order_id product_id order_date customer_id customer_name customer_city product_name list_price qty".split()
    F = [fd("order_id", "order_date customer_id"),
         fd("customer_id", "customer_name customer_city"),
         fd("product_id", "product_name list_price"),
         fd("order_id product_id", "qty")]
    report("order_line_wide", R, F)
    print("\nclosure trace for {order_id}:")
    closure({"order_id"}, F, trace=True)
    for x in (["order_id"], ["product_id"], ["order_id", "product_id"]):
        print(f"{fmt(x)}+ = {fmt(closure(x, F))}")
    print("order_id -> customer_city follows?", implies(F, ["order_id"], ["customer_city"]))
    print("customer_id -> order_id follows?  ", implies(F, ["customer_id"], ["order_id"]))
    print("3NF synthesis:", [fmt(t) for t in synthesize_3nf(R, F)])

    # --- 11.3: pseudotransitivity check ------------------------------------------------------
    F2 = F + [fd("customer_id product_id", "review_stars")]
    print("\npseudotransitivity: order_id -> customer_id and {customer_id, product_id} -> review_stars")
    print("  so {order_id, product_id} -> review_stars?", implies(F2, ["order_id", "product_id"], ["review_stars"]))

    # --- 11.11: BCNF clinic example ----------------------------------------------------------
    R = ["patient", "specialty", "doctor"]
    F = [fd("patient specialty", "doctor"), fd("doctor", "specialty")]
    report("visit (3NF, not BCNF)", R, F)
    pieces = bcnf_decompose(R, F)
    print("BCNF decomposition:", [fmt(p) for p in pieces])
    print("FDs no longer checkable in one piece:", preserved(F, pieces))

    # --- 11.11: the "looks like BCNF but is really 2NF" trap ---------------------------------
    report("student-course-instructor trap", ["student", "course", "instructor"], [fd("course", "instructor")])

    # --- Exercise 1 ---------------------------------------------------------------------------
    R = "attendee_id workshop_id attendee_name attendee_email workshop_title room seat_no".split()
    F = [fd("attendee_id", "attendee_name attendee_email"), fd("attendee_email", "attendee_id"),
         fd("workshop_id", "workshop_title room"), fd("attendee_id workshop_id", "seat_no")]
    report("Exercise 1 - signup", R, F)
    print("3NF synthesis:", [fmt(t) for t in synthesize_3nf(R, F)])

    # --- Exercise 2 ---------------------------------------------------------------------------
    R = "flight_id flight_date tail_number aircraft_type seat_capacity".split()
    F = [fd("flight_id", "flight_date tail_number"), fd("tail_number", "aircraft_type seat_capacity")]
    report("Exercise 2 - flight", R, F)
    print("BCNF decomposition:", [fmt(p) for p in bcnf_decompose(R, F)])

    # --- Exercise 3 ---------------------------------------------------------------------------
    R = list("ABCDE")
    F = [fd("A", "B"), fd("B C", "D"), fd("D", "A"), fd("C", "E")]
    report("Exercise 3 - abstract R(A,B,C,D,E)", R, F)
    for x in ("C", "A C", "B C", "C D", "C E"):
        print(f"{fmt(x.split())}+ = {fmt(closure(x.split(), F))}")
    pieces = bcnf_decompose(R, F)
    print("BCNF decomposition:", [fmt(p) for p in pieces])
    print("FDs lost by it:", preserved(F, pieces) or "none")
    print("3NF synthesis:", [fmt(t) for t in synthesize_3nf(R, F)])

    # --- Exercise 4: quick-fire ---------------------------------------------------------------
    quick = [
        ("Q1 R(A,B,C)  A->B, B->C", "A B C", [fd("A", "B"), fd("B", "C")]),
        ("Q2 R(A,B,C)  AB->C, C->B", "A B C", [fd("A B", "C"), fd("C", "B")]),
        ("Q3 R(A,B,C,D)  AB->C, B->D", "A B C D", [fd("A B", "C"), fd("B", "D")]),
        ("Q4 R(A,B,C)  A->B, B->A, A->C", "A B C", [fd("A", "B"), fd("B", "A"), fd("A", "C")]),
    ]
    print("\n=== Exercise 4 - quick-fire")
    for name, attrs, fds_ in quick:
        ks = ", ".join(fmt(k) for k in candidate_keys(attrs.split(), fds_))
        print(f"{name:32} keys: {ks:18} highest NF: {highest_nf(attrs.split(), fds_)}")

    # --- minimal cover demo ------------------------------------------------------------------
    F = [fd("A", "B C"), fd("B", "C"), fd("A B", "D")]
    print("\n=== minimal cover of {A->BC, B->C, AB->D}:",
          ", ".join(f"{fmt(l)}->{fmt(r)}" for l, r in minimal_cover(F)))
