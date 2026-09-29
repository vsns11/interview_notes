"""datetime + zoneinfo (naive vs aware, DST), timedelta, parsing/formatting, time."""
import time
from datetime import UTC, date, datetime, timedelta
from zoneinfo import ZoneInfo

if __name__ == "__main__":
    # naive vs aware
    naive = datetime(2026, 3, 8, 1, 30)
    aware = datetime(2026, 3, 8, 1, 30, tzinfo=ZoneInfo("America/New_York"))
    print("naive tzinfo      :", naive.tzinfo)
    print("aware             :", aware.isoformat())
    try:
        naive < aware
    except TypeError as e:
        print("compare naive/aware:", e)

    # DST: in New York clocks jump 02:00 -> 03:00 on 2026-03-08
    later = aware + timedelta(hours=1)          # wall-clock arithmetic!
    print("aware + 1h (wall) :", later.isoformat())
    real = (aware.astimezone(UTC) + timedelta(hours=1)).astimezone(aware.tzinfo)
    print("aware + 1h (real) :", real.isoformat())

    # convert between zones: store UTC, display local
    meeting_utc = datetime(2026, 9, 28, 14, 0, tzinfo=UTC)
    for tz in ["Asia/Kolkata", "Europe/London", "America/Los_Angeles"]:
        print(f"  {tz:<20}", meeting_utc.astimezone(ZoneInfo(tz)).strftime("%a %H:%M %Z"))

    # parsing and formatting
    d = datetime.fromisoformat("2026-09-28T09:15:00+05:30")
    print("fromisoformat     :", d, "| utcoffset", d.utcoffset())
    p = datetime.strptime("28/09/2026 18:05", "%d/%m/%Y %H:%M")
    print("strptime          :", p, "| strftime:", p.strftime("%Y-%m-%d %I:%M %p"))

    # date arithmetic
    due = date(2026, 1, 31) + timedelta(days=30)
    print("date + 30 days    :", due, "| weekday", due.strftime("%A"))
    print("days until 2027   :", (date(2027, 1, 1) - date(2026, 9, 28)).days)

    # time module: timestamps and timers
    ts = 1_790_000_000
    print("from timestamp    :", datetime.fromtimestamp(ts, tz=UTC))
    t0 = time.perf_counter()
    sum(range(1_000_000))
    print("perf_counter >= 0 :", time.perf_counter() - t0 > 0)
    print("monotonic is float:", isinstance(time.monotonic(), float))
