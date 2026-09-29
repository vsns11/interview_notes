"""A double-entry wallet ledger in ~120 lines.

Teaching goals (Module 17, "Design a Digital Wallet"):
  1. Every transfer is ONE journal entry whose legs sum to zero, so money can
     only move, never appear or vanish (the trial balance is always 0).
  2. Idempotency keys turn at-least-once delivery into exactly-once effects.
  3. The journal is the source of truth (event sourcing): balances are a cache
     that can be rebuilt by replaying the journal.
  4. Mistakes are fixed with a reversing entry, never by editing history.

Amounts are integer cents. Run:  python ledger.py
"""
from __future__ import annotations

import random
import threading
from dataclasses import dataclass

# Sign convention: a leg is +amount for a DEBIT and -amount for a CREDIT.
# Asset accounts (money we hold at the bank) grow with debits; liability
# accounts (money we owe users) and revenue grow with credits. `shown()` flips
# the sign for credit-normal accounts so balances read naturally.
CREDIT_NORMAL = {"liability", "revenue"}


@dataclass(frozen=True)
class Entry:
    entry_id: int
    key: str                                   # idempotency key
    memo: str
    legs: tuple[tuple[str, int], ...]          # (account, signed cents)


class InsufficientFunds(Exception):
    pass


class Ledger:
    def __init__(self, accounts: dict[str, str]) -> None:
        self.kind = dict(accounts)             # account -> asset | liability | revenue
        self.journal: list[Entry] = []         # append-only source of truth
        self.balance = {a: 0 for a in accounts}  # derived projection (raw signed)
        self.by_key: dict[str, Entry] = {}
        self.lock = threading.Lock()           # stands in for "one writer per shard"

    def shown(self, acct: str, raw: dict | None = None) -> int:
        v = (raw or self.balance)[acct]
        return -v if self.kind[acct] in CREDIT_NORMAL else v

    def post(self, key: str, memo: str, legs: list[tuple[str, int]],
             must_not_go_negative: tuple[str, ...] = ()) -> Entry:
        if sum(a for _, a in legs) != 0:
            raise ValueError("legs must sum to zero")
        with self.lock:                        # check + append are one atomic step
            if key in self.by_key:             # duplicate delivery -> same answer
                return self.by_key[key]
            for acct in must_not_go_negative:
                delta = sum(a for ac, a in legs if ac == acct)
                raw = self.balance[acct] + delta
                if (-raw if self.kind[acct] in CREDIT_NORMAL else raw) < 0:
                    raise InsufficientFunds(acct)
            e = Entry(len(self.journal) + 1, key, memo, tuple(legs))
            self.journal.append(e)
            for acct, amt in legs:
                self.balance[acct] += amt
            self.by_key[key] = e
            return e

    # ---- business operations ---------------------------------------------
    def top_up(self, key: str, user: str, cents: int) -> Entry:
        # card money lands in our bank account (asset up) and we now owe the user
        return self.post(key, f"top-up {user}", [("bank", +cents), (user, -cents)])

    def transfer(self, key: str, src: str, dst: str, cents: int, fee: int = 0) -> Entry:
        legs = [(src, +(cents + fee)), (dst, -cents)]
        if fee:
            legs.append(("fees", -fee))
        return self.post(key, f"{src}->{dst}", legs, must_not_go_negative=(src,))

    def reverse(self, key: str, original: Entry) -> Entry:
        return self.post(key, f"reversal of #{original.entry_id}",
                         [(a, -amt) for a, amt in original.legs])

    # ---- checks -------------------------------------------------------------
    def trial_balance(self) -> int:
        return sum(self.balance.values())      # must always be exactly 0

    def replay(self) -> dict[str, int]:
        raw = {a: 0 for a in self.kind}
        for e in self.journal:
            for acct, amt in e.legs:
                raw[acct] += amt
        return raw


def main() -> None:
    users = [f"u{i}" for i in range(20)]
    L = Ledger({"bank": "asset", "fees": "revenue", **{u: "liability" for u in users}})

    print("=== 1. Top-ups and one transfer with a fee ===")
    for u in users:
        L.top_up(f"topup-{u}", u, 10_000)                     # $100.00 each
    e = L.transfer("t-001", "u0", "u1", 2_500, fee=25)
    for acct, amt in e.legs:
        print(f"  {acct:5s} {'DEBIT ' if amt > 0 else 'CREDIT'} {abs(amt):>6}")
    print("  u0 =", L.shown("u0"), " u1 =", L.shown("u1"), " fees =", L.shown("fees"),
          " bank =", L.shown("bank"), " trial balance =", L.trial_balance())
    assert L.shown("u0") == 7_475 and L.shown("u1") == 12_500 and L.trial_balance() == 0

    print("\n=== 2. The same request delivered twice (client retry) ===")
    again = L.transfer("t-001", "u0", "u1", 2_500, fee=25)
    print("  second call returned entry #%d (same as first: %s); u0 still %d"
          % (again.entry_id, again is e, L.shown("u0")))
    assert again is e and L.shown("u0") == 7_475

    print("\n=== 3. Overdraft is refused inside the atomic step ===")
    try:
        L.transfer("t-002", "u0", "u2", 1_000_000)
    except InsufficientFunds as ex:
        print("  refused: insufficient funds in", ex)

    print("\n=== 4. 8 threads x 2,000 random transfers, every one sent twice ===")
    total_user_money = sum(L.shown(u) for u in users) + L.shown("fees")
    refused = [0]

    def worker(tid: int) -> None:
        rng = random.Random(tid)
        for i in range(2_000):
            a, b = rng.sample(users, 2)
            key = f"w{tid}-{i}"
            for _ in range(2):                                # at-least-once delivery
                try:
                    L.transfer(key, a, b, rng.randrange(1, 3_000), fee=1)
                except InsufficientFunds:
                    refused[0] += 1
                    break

    threads = [threading.Thread(target=worker, args=(t,)) for t in range(8)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    after = sum(L.shown(u) for u in users) + L.shown("fees")
    negatives = [u for u in users if L.shown(u) < 0]
    print(f"  journal entries: {len(L.journal):,}   refused (no funds): {refused[0]:,}")
    print(f"  user money + fees before: {total_user_money:,}  after: {after:,}")
    print(f"  accounts below zero: {negatives}   trial balance: {L.trial_balance()}")
    assert after == total_user_money and not negatives and L.trial_balance() == 0

    print("\n=== 5. Rebuild balances from the journal (event sourcing) ===")
    rebuilt = L.replay()
    print("  projection == replay:", rebuilt == L.balance)
    assert rebuilt == L.balance

    print("\n=== 6. Fix a mistaken transfer with a reversing entry ===")
    bad = L.transfer("t-oops", "u3", "u4", 500)
    u3 = L.shown("u3")
    fix = L.reverse("rev-t-oops", bad)
    print(f"  #{bad.entry_id} {bad.memo}  ->  #{fix.entry_id} {fix.memo}")
    print(f"  u3 back from {u3} to {L.shown('u3')}; both entries stay in the journal "
          f"({len(L.journal):,} entries)")
    assert L.shown("u3") == u3 + 500 and L.trial_balance() == 0
    print("\nall asserts passed")


if __name__ == "__main__":
    main()
