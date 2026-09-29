"""15.9 Crash recovery from a write-ahead log: redo committed work, undo unfinished work.
Each log record has an LSN. A checkpoint says: every change up to this LSN is already
in the data files. Recovery starts from the checkpoint instead of the log's beginning."""

log = [  # (lsn, txn, action, account, before, after)
    (200, "T1", "BEGIN", None, None, None),
    (201, "T1", "UPDATE", "A", 900, 600),     # T1 moves 300 from A to B
    (202, "T1", "UPDATE", "B", 100, 400),
    (203, "T1", "COMMIT", None, None, None),
    (204, None, "CHECKPOINT", None, None, None),
    (205, "T2", "BEGIN", None, None, None),
    (206, "T2", "UPDATE", "C", 500, 450),     # T2 moves 50 from C to D
    (207, "T2", "UPDATE", "D", 0, 50),
    (208, "T2", "COMMIT", None, None, None),
    (209, "T3", "BEGIN", None, None, None),
    (210, "T3", "UPDATE", "A", 600, 580),     # T3 moves 20 from A to C ... then crash
]

# Disk state at the crash: the checkpoint flushed T1; T2's pages were not yet written,
# but one of T3's dirty pages happened to be flushed early (allowed with steal/undo logging).
disk = {"A": 580, "B": 400, "C": 500, "D": 0}
print("on disk after crash :", disk)

ckpt = max(lsn for lsn, _, a, *_ in log if a == "CHECKPOINT")
committed = {t for _, t, a, *_ in log if a == "COMMIT"}
tail = [rec for rec in log if rec[0] > ckpt]

# REDO pass (forward): re-apply every update after the checkpoint (repeat history).
for lsn, txn, action, acct, before, after in tail:
    if action == "UPDATE":
        disk[acct] = after
        print(f"redo  LSN {lsn} {txn}: {acct} = {after}")

# UNDO pass (backward): roll back transactions that never committed, using before-images.
losers = {t for _, t, a, *_ in log if a == "BEGIN"} - committed
for lsn, txn, action, acct, before, after in reversed(log):
    if action == "UPDATE" and txn in losers:
        disk[acct] = before
        print(f"undo  LSN {lsn} {txn}: {acct} = {before}")

print("recovered state     :", disk)
print("money conserved     :", sum(disk.values()) == 900 + 100 + 500 + 0)
