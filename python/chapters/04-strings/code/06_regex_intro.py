"""A first look at regular expressions with the re module."""
import re

LOG = """2024-03-09 12:01:07 ERROR db timeout user=alice
2024-03-09 12:01:09 INFO  login ok user=bob
2024-03-10 08:15:00 ERROR disk full user=carol"""


def search_vs_match() -> None:
    s = "Order #123 shipped"
    print(re.match(r"\d+", s))                  # None: match only tries at position 0
    m = re.search(r"\d+", s)                    # scans the whole string
    print(m, m.group(), m.start(), m.end(), m.span())
    print(re.fullmatch(r"\d{3}", "123") is not None, re.fullmatch(r"\d{3}", "1234"))


def findall_finditer() -> None:
    print(re.findall(r"user=(\w+)", LOG))        # one group -> list of strings
    print(re.findall(r"(\d{4})-(\d\d)-(\d\d)", "2024-03-09 and 2025-01-02"))  # tuples
    for m in re.finditer(r"ERROR (.+?) user=(\w+)", LOG):
        print("  ", m.group(1), "->", m.group(2), "at", m.start())


def groups() -> None:
    pat = re.compile(r"(?P<date>\d{4}-\d\d-\d\d) (?P<time>[\d:]+) (?P<level>\w+)")
    m = pat.search(LOG)
    print(m.group("level"), m.group(1), m.groups())
    print(m.groupdict())
    print(re.search(r"(?:ab)+", "xxababab").group())   # (?:...) groups without capturing


def substitution() -> None:
    print(re.sub(r"\s+", " ", "too    many   spaces"))
    print(re.sub(r"(\d{4})-(\d\d)-(\d\d)", r"\3/\2/\1", "due 2024-03-09"))   # backrefs
    print(re.sub(r"\d+", lambda m: str(int(m.group()) * 2), "3 apples, 10 pears"))
    print(re.split(r"[,;]\s*", "a, b;c ,d"))
    print(re.sub(r"user=\w+", "user=***", LOG.splitlines()[0]))


def flags_and_greedy() -> None:
    print(re.findall(r"error", LOG, flags=re.IGNORECASE))
    html = "<b>bold</b><i>it</i>"
    print(re.findall(r"<.+>", html))     # greedy: grabs as much as possible
    print(re.findall(r"<.+?>", html))    # lazy: as little as possible
    print(re.escape("price: $5.00 (approx)"))


if __name__ == "__main__":
    for f in (search_vs_match, findall_finditer, groups, substitution, flags_and_greedy):
        print(f"--- {f.__name__}")
        f()
