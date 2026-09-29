"""re beyond the basics: groups, named groups, finditer, sub with a function,
flags, VERBOSE, lookarounds, greedy vs lazy, fullmatch."""
import re

LOG = """\
2026-09-28 10:15:02 ERROR [payments] card declined user=42
2026-09-28 10:15:07 INFO  [auth] login ok user=7
2026-09-28 10:16:11 ERROR [auth] bad password user=7
"""

LINE = re.compile(r"""
    ^(?P<date>\d{4}-\d{2}-\d{2})\s     # date
    (?P<time>[\d:]{8})\s+
    (?P<level>[A-Z]+)\s+
    \[(?P<svc>\w+)\]\s                 # service in brackets
    (?P<msg>.*?)\s                     # lazy: stop before the last field
    user=(?P<user>\d+)$
""", re.VERBOSE | re.MULTILINE)

if __name__ == "__main__":
    for m in LINE.finditer(LOG):
        if m["level"] == "ERROR":
            print("error:", m.group("svc", "user"), "|", m["msg"])

    print("findall groups  :", re.findall(r"user=(\d+)", LOG))
    print("greedy          :", re.search(r"<.+>", "<a><b>").group())
    print("lazy            :", re.search(r"<.+?>", "<a><b>").group())

    # sub with a function: mask all but the last 4 digits
    card = "pay 4111111111111111 now"
    print("masked          :", re.sub(r"\d{12}(\d{4})", lambda m: "*" * 12 + m[1], card))
    # backreference in replacement: swap "last, first"
    print("swap            :", re.sub(r"(\w+), (\w+)", r"\2 \1", "Lovelace, Ada"))

    # lookahead: password needs a digit and an uppercase letter, 8+ chars
    strong = re.compile(r"(?=.*\d)(?=.*[A-Z]).{8,}")
    for pw in ["password", "Passw0rdX"]:
        print(f"strong({pw!r:12}):", bool(strong.fullmatch(pw)))

    print("IGNORECASE      :", re.findall(r"error", LOG, flags=re.I)[:2])
    print("split multi     :", re.split(r"[,;]\s*", "a, b;c ,d"))
    print("match vs search :", re.match(r"\d", "a1"), re.search(r"\d", "a1").group())
