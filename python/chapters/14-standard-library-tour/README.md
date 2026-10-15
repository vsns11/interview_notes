# Chapter 14 — A Tour of the Standard Library

> **What you will learn.** The modules you reach for every week, and the traps in each: dates and time zones (`datetime`, `zoneinfo`, `time`), regular expressions in depth (`re`), `logging`, `argparse`, the OS (`os`, `sys`, `subprocess`, `shutil`, `pathlib`), `random` vs `secrets`, numbers (`math`, `statistics`, `decimal`/`fractions`), small modules (`copy`, `pprint`, `enum`, `uuid`, `hashlib`, `json`, `csv`), the embedded database `sqlite3`, basic HTTP (`urllib`, `http` — see [System Design Fundamentals](../../../system-design-patterns/modules/00-system-design-fundamentals/README.md)), and when a third-party package is the better choice.
>
> **Prerequisites.** Chapters 1–12. `collections`, `itertools` and `functools` are in Chapters 6, 7 and 11 — pointer in §14.10.
>
> **Code.** `chapters/14-standard-library-tour/code/`, stdlib only, no internet (the HTTP demo uses a server it starts on `127.0.0.1`), Python 3.12+:
>
> ```bash
> python chapters/14-standard-library-tour/code/01_datetime_time.py
> ```

"Batteries included": the standard library ships a few hundred modules. You need to know which one to reach for and its known traps.

```mermaid
flowchart LR
    A["Time: datetime, zoneinfo, time"] --> B["Text: re"]
    B --> C["Ops: logging, argparse"]
    C --> D["OS: os, sys, subprocess, shutil, pathlib"]
    D --> E["Numbers: random, secrets, math, statistics"]
    E --> F["Toolbox: copy, pprint, enum, uuid, hashlib, json, csv"]
    F --> G["Data and network: sqlite3, urllib, http"]
    G --> H["When to go third-party"]
```

---

## 14.1 `datetime` and `zoneinfo` — naive vs aware

### What it is

| Type | Holds | Example |
|---|---|---|
| `date` | year, month, day | `date(2026, 9, 28)` |
| `time` | hour … microsecond (no date) | `time(14, 30)` |
| `datetime` | date + time (+ optional time zone) | `datetime(2026, 9, 28, 14, 30)` |
| `timedelta` | a duration | `timedelta(days=2, hours=3)` |

A datetime is **naive** if `tzinfo is None` — "14:30 on some wall clock, somewhere" — and **aware** if it carries a `tzinfo`, so it names one exact instant. `zoneinfo.ZoneInfo("Asia/Kolkata")` (3.9+) supplies real rules from the IANA time-zone database, including **DST** (daylight saving time). `datetime.UTC` (3.11+, alias of `timezone.utc`) is UTC, the reference clock with no DST.

### Why it matters
Classic time bugs: meetings an hour off after a DST change, reports missing a day because the server runs in UTC and the user in IST (UTC+5:30), crashes from comparing naive and aware values.

### How it works
1. **Store and compute in UTC; convert to local time only for display.**
2. Create aware values: `datetime.now(UTC)` or `datetime(..., tzinfo=ZoneInfo("Europe/London"))`. Avoid `datetime.utcnow()` — it returns a *naive* value and is deprecated since 3.12.
3. Convert with `.astimezone(ZoneInfo(...))` — same instant, different wall clock.
4. Parse/format: `fromisoformat("2026-09-28T09:15:00+05:30")` / `.isoformat()` for ISO 8601 machine formats; `strptime(text, fmt)` / `strftime(fmt)` for custom ones (`%Y-%m-%d %H:%M`, `%A` weekday, `%I %p` 12-hour clock, `%Z` zone name).
5. Arithmetic: `datetime ± timedelta`, `datetime − datetime → timedelta`. **Caution:** adding a `timedelta` to an aware datetime is *wall-clock* arithmetic in its zone — no DST adjustment. For "exactly N hours later", convert to UTC, add, convert back.
6. Naive and aware values can't be compared or subtracted (`TypeError`).
7. On Windows (no system zone database) install `tzdata` so `zoneinfo` works.

Script `01_datetime_time.py` (key parts):

```python
naive = datetime(2026, 3, 8, 1, 30)
aware = datetime(2026, 3, 8, 1, 30, tzinfo=ZoneInfo("America/New_York"))
naive < aware                                    # TypeError

later = aware + timedelta(hours=1)               # wall-clock arithmetic
real = (aware.astimezone(UTC) + timedelta(hours=1)).astimezone(aware.tzinfo)

meeting_utc = datetime(2026, 9, 28, 14, 0, tzinfo=UTC)
for tz in ["Asia/Kolkata", "Europe/London", "America/Los_Angeles"]:
    print(f"  {tz:<20}", meeting_utc.astimezone(ZoneInfo(tz)).strftime("%a %H:%M %Z"))
```

Output:
```
naive tzinfo      : None
aware             : 2026-03-08T01:30:00-05:00
compare naive/aware: can't compare offset-naive and offset-aware datetimes
aware + 1h (wall) : 2026-03-08T02:30:00-05:00
aware + 1h (real) : 2026-03-08T03:30:00-04:00
  Asia/Kolkata         Mon 19:30 IST
  Europe/London        Mon 15:00 BST
  America/Los_Angeles  Mon 07:00 PDT
fromisoformat     : 2026-09-28 09:15:00+05:30 | utcoffset 5:30:00
strptime          : 2026-09-28 18:05:00 | strftime: 2026-09-28 06:05 PM
date + 30 days    : 2026-03-02 | weekday Monday
days until 2027   : 95
```

On 8 March 2026 New York clocks jump from 02:00 to 03:00. The "wall" result, 02:30, never existed on a New York clock; the UTC round-trip gives the correct 03:30 EDT.

### Common mistakes
- Naive datetimes in a server with users in several zones.
- Hard-coded offsets (`+05:30`) instead of zone names — offsets change with DST and politics; zone rules get updated.
- `pytz` in new code — its `localize()` API is error-prone; `zoneinfo` replaced it.
- Expecting month arithmetic — `timedelta` has no months. Use `dateutil.relativedelta` (third-party).

### Real-world examples
PostgreSQL stores `timestamptz` as UTC; Django (`USE_TZ = True`) and most APIs exchange ISO 8601 in UTC; calendar apps store UTC plus the zone name so recurring meetings follow DST.

### In the interview
"I keep all datetimes aware and in UTC internally, convert to the user's `ZoneInfo` zone only at display time, and serialise as ISO 8601. Naive datetimes are ambiguous and can't be compared with aware ones."
- *Why not `utcnow()`?* It returns a naive value that looks local; use `datetime.now(UTC)`.

---

## 14.2 `time` — timestamps and timers

| Function | Returns | Use for |
|---|---|---|
| `time.time()` | seconds since the Unix **epoch** (1970-01-01 UTC), float | timestamps (jumps if the system clock is changed) |
| `time.perf_counter()` | highest-resolution counter, arbitrary start | measuring short durations |
| `time.monotonic()` | never goes backwards, arbitrary start | timeouts, rate limiters (cap actions per second) |
| `time.sleep(s)` | — | pause the thread |
| `time.time_ns()`, `perf_counter_ns()` | integer nanoseconds | avoid float precision loss |

Timestamp → datetime: `datetime.fromtimestamp(ts, tz=UTC)` — `1_790_000_000` → `2026-09-21 14:13:20+00:00`.

**Mistake:** timing with `time.time()` — if NTP (network time sync) adjusts the clock mid-measurement you get wrong or negative durations. Use `perf_counter()`/`monotonic()`; for micro-benchmarks use `timeit`, which repeats the code and disables garbage collection during timing.

---

## 14.3 `re` — regular expressions, deeper

[Chapter 4](../04-strings/README.md) introduced `re` (`search`, `match`, `findall`, basic patterns). This adds what real parsing needs. `re` compiles a pattern into a matcher run by a **backtracking** engine (on failure it goes back and tries the next alternative).

### How it works — the essentials

| Feature | Syntax | Meaning |
|---|---|---|
| Raw strings | `r"\d+"` | always use them so `\` reaches `re` unchanged |
| Group | `( … )` | capture a part; `m.group(1)` or `m[1]` |
| Named group | `(?P<year>\d{4})` | `m["year"]`, `m.groupdict()` |
| Non-capturing | `(?: … )` | group without capturing |
| Alternation | `cat\|dog` | either |
| Quantifiers | `* + ? {m,n}` | how many times; greedy (as much as possible) by default |
| Lazy | `*? +? ??` | as little as possible |
| Anchors | `^ $ \b \A \Z` | line start/end, word boundary, string start/end |
| Lookahead | `(?=…)`, `(?!…)` | require / forbid what follows, without consuming |
| Lookbehind | `(?<=…)`, `(?<!…)` | same, for what precedes (fixed width) |
| Backreference | `\1`, `(?P=name)` | the same text again |

Functions: `search` (anywhere), `match` (start only), `fullmatch` (whole string — for validation), `findall` (strings, or tuples if there are groups), `finditer` (match objects — memory-friendly, gives positions), `sub(pattern, repl, s)` (repl is a string with `\1` or a **function** of the match), `split`.

Flags: `re.I` (IGNORECASE), `re.MULTILINE` (`^`/`$` at each line), `re.DOTALL` (`.` matches newline), `re.X` (VERBOSE: whitespace and `#` comments in the pattern).

**Compile once** at module level (`LINE = re.compile(...)`); `re` also caches recent patterns, so this is mostly for readability.

Script `02_regex_deeper.py`:

```python
LINE = re.compile(r"""
    ^(?P<date>\d{4}-\d{2}-\d{2})\s     # date
    (?P<time>[\d:]{8})\s+
    (?P<level>[A-Z]+)\s+
    \[(?P<svc>\w+)\]\s                 # service in brackets
    (?P<msg>.*?)\s                     # lazy: stop before the last field
    user=(?P<user>\d+)$
""", re.VERBOSE | re.MULTILINE)

for m in LINE.finditer(LOG):
    if m["level"] == "ERROR":
        print("error:", m.group("svc", "user"), "|", m["msg"])

re.sub(r"\d{12}(\d{4})", lambda m: "*" * 12 + m[1], card)   # function replacement
re.sub(r"(\w+), (\w+)", r"\2 \1", "Lovelace, Ada")            # backreferences
strong = re.compile(r"(?=.*\d)(?=.*[A-Z]).{8,}")              # two lookaheads
```

Output:
```
error: ('payments', '42') | card declined
error: ('auth', '7') | bad password
findall groups  : ['42', '7', '7']
greedy          : <a><b>
lazy            : <a>
masked          : pay ************1111 now
swap            : Ada Lovelace
strong('password'  ): False
strong('Passw0rdX' ): True
IGNORECASE      : ['ERROR', 'ERROR']
split multi     : ['a', 'b', 'c ', 'd']
match vs search : None 1
```

### Trade-offs / when not to use it
- **Structured formats have parsers**: JSON → `json`, CSV → `csv`, HTML → BeautifulSoup/lxml, URLs → `urllib.parse`, emails → a validation library.
- Simple checks read better as string methods: `s.startswith("http")`, `"@" in s`, `s.isdigit()`.
- **Catastrophic backtracking**: nested quantifiers like `(a+)+$` on a long non-matching input take exponential time — real outages at Stack Overflow (2016) and Cloudflare (2019). Avoid ambiguous quantifiers on untrusted input; since 3.11 atomic groups `(?>…)` and possessive quantifiers `*+` ("never backtrack into this") prevent it.

### Common mistakes
- No `r` prefix (`"\b"` is a backspace, not a word boundary).
- `match` when you meant `search` (or `fullmatch` for validation).
- Greedy `.*` swallowing too much — use `.*?` or a negated class `[^\]]*`.
- `findall` with groups returns only the groups.

### In the interview
"I compile patterns with named groups and `re.VERBOSE`, use `finditer` for large inputs and `fullmatch` for validation, avoid regex for structured formats, and watch nested quantifiers because of catastrophic backtracking on untrusted input."

---

## 14.4 `logging` — levels, handlers, formatters, best practice

### What it is

| Part | Job | Example |
|---|---|---|
| **Logger** | the object your code calls; named hierarchically with dots | `logging.getLogger("shop.orders")` |
| **Handler** | sends records (one per logged event) somewhere | `StreamHandler` (console), `FileHandler`, `RotatingFileHandler`, `TimedRotatingFileHandler`, `SysLogHandler`, `QueueHandler` |
| **Formatter** | turns a record into text | `"%(asctime)s %(levelname)s %(name)s %(message)s"` |
| **Filter** | drops or edits records | add a request ID to each record |

**Levels:** `DEBUG` (10) developer detail · `INFO` (20) normal events · `WARNING` (30) unexpected but working · `ERROR` (40) an operation failed · `CRITICAL` (50) the program may not continue.

### Why it matters (logging vs print)
`print` has no levels, timestamps or destinations and can't be silenced per module. With logging the **same code** is silent in tests, shows INFO on the console in development, and ships JSON to a log collector in production — only configuration changes.

### How it works
1. Each module: `log = logging.getLogger(__name__)`. Names form a tree: `shop.orders` → `shop` → root.
2. `log.info("order placed qty=%d", qty)` creates a **LogRecord** if the logger's effective level allows INFO.
3. The record goes to the logger's handlers, then **propagates** to parent handlers up to the root.
4. Each handler has its own level and formatter — one record can reach the console (INFO+) and a file (DEBUG+) in different formats.
5. With no configuration, WARNING+ goes to stderr via a "last resort" handler; DEBUG/INFO are dropped (root default level is WARNING).
6. Configure **once, at program start**: `logging.basicConfig(level=..., format=...)` for simple apps, `logging.config.dictConfig({...})` for real ones. `basicConfig` does nothing if the root already has handlers, unless `force=True`.

Script `03_logging_demo.py`:

```python
log = logging.getLogger("shop.orders")      # normally getLogger(__name__)

def place_order(qty: int) -> None:
    log.debug("validating qty=%s", qty)
    if qty <= 0:
        log.warning("rejected order with qty=%d", qty)
        return
    try:
        1 / (qty - 3)
    except ZeroDivisionError:
        log.exception("pricing failed for qty=%d", qty)   # ERROR + traceback
        return
    log.info("order placed qty=%d", qty)

logging.config.dictConfig({
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {"short": {"format": "%(levelname)-7s %(name)s: %(message)s"},
                   "full":  {"format": "%(asctime)s %(levelname)s %(name)s "
                                       "%(filename)s:%(lineno)d %(message)s"}},
    "handlers": {"console": {"class": "logging.StreamHandler", "level": "INFO",
                             "formatter": "short", "stream": "ext://sys.stdout"},
                 "file": {"class": "logging.StreamHandler", "level": "DEBUG",
                          "formatter": "full", "stream": file_like}},
    "loggers": {"shop": {"level": "DEBUG", "handlers": ["console", "file"]}},
})
```

Output (console handler; path shortened):
```
INFO    shop.orders: order placed qty=2
WARNING shop.orders: rejected order with qty=0
ERROR   shop.orders: pricing failed for qty=3
Traceback (most recent call last):
  File ".../03_logging_demo.py", line 15, in place_order
    1 / (qty - 3)
    ~~^~~~~~~~~~~
ZeroDivisionError: division by zero
--- file handler got 11 lines; DEBUG line present: True
logger chain: shop.orders -> shop -> root
effective level: DEBUG
```
The console got INFO+; the "file" handler (a `StringIO`) also got DEBUG.

### Best practice
1. `getLogger(__name__)` per module; libraries never log on the root logger.
2. **Libraries never configure logging** — at most add `logging.NullHandler()`; the application decides destinations.
3. Pass values as arguments — `log.info("user %s", uid)`, not f-strings — so formatting is skipped when disabled and aggregators can group identical templates.
4. In `except`, use `log.exception(...)` (ERROR + traceback) or `exc_info=True`.
5. Never log secrets or personal data.
6. Production: **structured** (JSON) logs with context fields (request ID, user ID), rotated files or stdout collected by the platform ("12-factor": log to stdout, let Docker/Kubernetes collect). Helpers: `structlog`, `python-json-logger`.
7. High-throughput or async apps: `QueueHandler` + `QueueListener` moves slow I/O off the hot path.

### Common mistakes
- `basicConfig` in an imported module (silently wins or silently does nothing).
- Adding handlers in a repeatedly called function → duplicated messages. Configure once.
- Messages twice: handlers on both a child and the root with `propagate` True.
- `dictConfig`'s `disable_existing_loggers` defaults to **True**, silencing loggers created earlier — set `False`.

### Real-world examples
Django (`LOGGING` setting) and Flask configure via `dictConfig`; `uvicorn`, `requests`/`urllib3`, `boto3` log through named loggers you can tune: `logging.getLogger("urllib3").setLevel(logging.WARNING)`.

### In the interview
"Each module gets `getLogger(__name__)`; the application configures handlers and formatters once with `dictConfig`; libraries don't configure logging. I use lazy `%s` arguments, `log.exception` in except blocks, and structured JSON logs to stdout in containers."
- *Why not print?* No levels, routing, per-module control, timestamps or context.
- *Why are my debug logs missing?* Logger or handler level above DEBUG — root defaults to WARNING.

---

## 14.5 `argparse` — command-line interfaces

`argparse` turns `sys.argv` (the words typed after the program name) into a validated object, generates `--help`, and prints usage errors.

1. `ArgumentParser(prog=..., description=...)`.
2. `add_argument("title")` — a **positional** (required, identified by position).
3. `add_argument("--priority", type=int, choices=..., default=...)` — an **option**; `type` converts, `choices` restricts.
4. `action="store_true"` for flags, `action="count"` for `-vv`, `action="append"` for repeatable options, `nargs="+"` for one-or-more, `required=True` to force an option.
5. `add_subparsers()` for git-style subcommands (`todo add`, `todo list`).
6. `parse_args()` returns a `Namespace`; on invalid input it prints usage to stderr and exits with status **2** (0 = success, non-zero = failure). `-h/--help` is automatic.
7. `main(argv=None)` accepting a list lets tests call it without a real command line.

Script `04_argparse_cli.py`:

```python
p = argparse.ArgumentParser(prog="todo", description="A tiny todo CLI.")
p.add_argument("-v", "--verbose", action="count", default=0)
sub = p.add_subparsers(dest="command", required=True)

add = sub.add_parser("add", help="add a task")
add.add_argument("title")
add.add_argument("--priority", type=int, choices=range(1, 4), default=2)
add.add_argument("--tag", action="append", default=[])

ls = sub.add_parser("list", help="list tasks")
ls.add_argument("--done", action="store_true")
ls.add_argument("--limit", type=int, default=10)
```

Output:
```
$ todo add Buy milk --priority 1 --tag home --tag shop
  parsed: {'verbose': 0, 'command': 'add', 'title': 'Buy milk', 'priority': 1, 'tag': ['home', 'shop']}
$ todo -vv list --done
  parsed: {'verbose': 2, 'command': 'list', 'done': True, 'limit': 10}
$ todo add x --priority 9
usage: todo add [-h] [--priority {1,2,3}] [--tag TAG] title
todo add: error: argument --priority: invalid choice: '9' (choose from '1', '2', '3')
  exit code: 2
usage: todo [-h] [-v] {add,list} ...
```
(`"Buy milk"` was one list item; in a real shell you'd quote it.)

**Making it runnable:** a `[project.scripts]` entry in `pyproject.toml` (`todo = "todo.cli:main"`) makes `pip install` create a `todo` command, or use `python -m todo` with a `__main__.py` ([Chapter 8](../08-modules-packages-environments/README.md)). End with `raise SystemExit(main())` so the return code reaches the shell.

| Library | Style | Good for |
|---|---|---|
| `argparse` (stdlib) | imperative, no dependency | scripts, tools that must not need installs |
| `click` | decorators, composable command groups | larger CLIs (Flask's `flask` command uses it) |
| `typer` | builds a click CLI from type hints | modern typed CLIs |
| `sys.argv` directly | manual | only for one or two trivial arguments |

**Common mistakes:** `type=bool` (`bool("False")` is `True` — use `store_true`); hand-parsing `sys.argv` for anything non-trivial; `parse_args()` at import time (breaks tests).

---

## 14.6 `os`, `sys`, `subprocess`, `shutil` (+ `pathlib` recap)

| Module | Area | Most-used parts |
|---|---|---|
| `os` | the operating system | `os.environ`, `os.getenv`, `os.cpu_count()`, `os.getpid()`, `os.walk`, `os.sep`, `os.replace` (atomic rename) |
| `sys` | the running interpreter | `sys.argv`, `sys.exit()`, `sys.path`, `sys.version_info`, `sys.platform`, `sys.executable`, `sys.stdin/stdout/stderr`, `sys.modules` |
| `subprocess` | running other programs | `subprocess.run([...], capture_output=True, text=True, check=True, timeout=...)` |
| `shutil` | high-level file operations | `copy2`, `copytree`, `move`, `rmtree`, `make_archive`, `which`, `disk_usage` |
| `pathlib` | paths as objects ([Chapter 9](../09-errors-exceptions-files/README.md)) | `Path`, `/`, `.read_text()`, `.glob()`, `.rglob()`, `.mkdir(parents=True)` |
| `tempfile` | throw-away files/dirs | `TemporaryDirectory()`, `NamedTemporaryFile()` |

### Key rules
1. **Config from the environment**: `os.environ.get("APP_ENV", "dev")` (`os.environ["X"]` raises `KeyError` if missing). Values are always strings.
2. **Paths**: `pathlib.Path` in new code; `os.path` still works.
3. **`subprocess.run`**:
   - Pass a **list** of arguments; never `shell=True` with user text (shell injection: `"file.txt; rm -rf ~"`).
   - `capture_output=True, text=True` → `result.stdout` as `str`.
   - `check=True` raises `CalledProcessError` on non-zero exit; `timeout=` raises `TimeoutExpired`.
   - `sys.executable` = "the same Python that runs me".
   - `os.system`/`os.popen` are older, less safe.
4. `shutil.rmtree` is permanent — double-check the path. `os.replace(tmp, final)` gives atomic "write then swap" updates.
5. `sys.exit(0)` success, non-zero failure (it raises `SystemExit`, so `finally` still runs).

Script `05_os_sys_subprocess_shutil.py` (key parts):

```python
with tempfile.TemporaryDirectory() as tmp:
    root = Path(tmp)
    (root / "src" / "pkg").mkdir(parents=True)
    (root / "src" / "pkg" / "a.py").write_text("print('hi')\n")
    shutil.copytree(root / "src", root / "backup")
    archive = shutil.make_archive(str(root / "bundle"), "zip", root / "src")
    shutil.rmtree(root / "backup")
    r = subprocess.run([sys.executable, str(root / "src" / "pkg" / "a.py")],
                       capture_output=True, text=True, check=True, timeout=10)
```

Output:
```
python      : (3, 14) | platform: darwin
argv[0]     : 05_os_sys_subprocess_shutil.py
HOME set    : True | APP_ENV = dev (default)
cpu_count>0 : True
copied      : ['backup/notes.txt', 'backup/pkg', 'backup/pkg/a.py']
py files    : ['a.py', 'a.py']
archive     : bundle.zip True
after rmtree: ['bundle.zip', 'src']
child stdout: hi | returncode 0
failed child: returncode 3
which python: True
disk free GB: True
```

### Common mistakes
- `subprocess.run("ls " + user_input, shell=True)` — injection.
- Ignoring the return code (no `check=True`).
- `os.chdir` in library code — changes the working directory for the whole process.
- Concatenating paths with `"/"` instead of `Path`/`os.path.join`.

### In the interview
"`subprocess.run` with a list, `check=True`, a timeout, never `shell=True` on user input. Config from `os.environ.get` with defaults, paths as `pathlib.Path`, scratch work in `tempfile.TemporaryDirectory`."

---

## 14.7 `random` vs `secrets`

- `random` — **pseudo-random** (deterministic formula, Mersenne Twister): fast, reproducible with a **seed**, statistically good, but **predictable** after enough outputs.
- `secrets` (3.6+) — the OS's cryptographically secure source (`os.urandom`): unpredictable, not reproducible.

**Rule: `random` for simulations, games, sampling, tests; `secrets` for passwords, tokens, keys, reset links.**

`random`: `random.Random(42)` (own seeded generator — other code can't disturb it, unlike global `random.seed`), `randint(a, b)` (both ends inclusive), `random()` in [0, 1), `uniform`, `choice`, `choices(pop, weights=, k=)` (with replacement), `sample(pop, k)` (without), `shuffle` (in place), `gauss(mu, sigma)`.

`secrets`: `token_bytes(n)`, `token_hex(n)` (2n hex chars), `token_urlsafe(n)` (≈1.3 × n URL-safe chars), `choice(seq)`, `randbelow(n)`, and `compare_digest(a, b)` — constant-time comparison against **timing attacks** (`==` stops at the first wrong character, leaking how many were right).

Output of `06_random_secrets_math_stats.py` (first part):
```
randint      : [6, 1, 1, 6, 3]
choice       : red
sample       : [28, 17, 94]
shuffle      : [7, 2, 5, 6, 0, 3, 4, 9, 8, 1]
weighted     : ['A', 'A', 'A', 'B', 'B', 'A', 'A', 'A']
gauss        : 1.1491
token_urlsafe: 22 chars
token_hex(8) : 16 hex chars
compare_digest: True
```

**Mistakes:** `random` for security tokens; `==` on secrets (use `compare_digest`). For NumPy use `numpy.random.default_rng(seed)` (ML handbook).

---

## 14.8 `math`, `statistics`, `decimal`, `fractions`

`math` = fast C functions on single numbers. `statistics` = descriptive statistics on small-to-medium lists — correct and readable, slower than NumPy/pandas on big arrays.

| Need | Function | Note |
|---|---|---|
| Compare floats | `math.isclose(a, b)` | default relative tolerance 1e-9; `0.1 + 0.2 == 0.3` is `False` |
| Rounding | `math.floor`, `math.ceil`, `math.trunc`, `round` | `round` uses **banker's rounding** (halves to even): `round(2.5) == 2`, `round(3.5) == 4` |
| Roots, distances | `sqrt`, `isqrt` (exact int), `hypot`, `dist` | |
| Counting | `comb(n, k)`, `perm(n, k)`, `factorial` | 5 choose 2 = 10 |
| Number theory | `gcd`, `lcm` | `lcm` since 3.9 |
| Logs, constants | `log(x, base)`, `log2`, `log10`, `exp`, `pi`, `e`, `inf`, `nan` | |
| Accurate sums | `math.fsum` | exact float sum |
| Averages | `statistics.mean`, `fmean`, `median`, `mode`, `multimode` | |
| Spread | `pstdev`/`pvariance` (population — all values), `stdev`/`variance` (sample; divides by n−1) | |
| Quartiles | `statistics.quantiles(data, n=4)` | |
| Relations | `correlation`, `covariance`, `linear_regression` | 3.10+ |

Output (second part):
```
isclose      : False True
floor/ceil   : -3 -2 2 4
sqrt, hypot  : 4.0 5.0
comb, perm   : 10 20 120
gcd, lcm     : 6 12
log          : 3.0 3.0 2.71828
fsum vs sum  : 1.0 1.0
mean/median  : 5 4.5
mode         : 4
pstdev/stdev : 2.0 2.1381
quantiles    : [4.0, 4.5, 6.5]
correlation  : 0.7746
linear_reg   : LinearRegression(slope=0.6, intercept=2.2)
```

Worked check: `[2, 4, 4, 4, 5, 5, 7, 9]` has mean 5; squared deviations sum to 9+1+1+1+0+0+4+16 = 32. Population variance 32/8 = 4 → `pstdev` 2.0; sample variance 32/7 ≈ 4.571 → `stdev` ≈ 2.1381.

`fsum vs sum`: since 3.12 built-in `sum()` of floats uses compensated (Neumaier) summation, so `sum([0.1] * 10)` is `1.0`; on 3.11 and earlier it was `0.9999999999999999`. `math.fsum` remains the exactly-rounded option.

### `decimal` and `fractions` — exact arithmetic
Floats are binary, so `0.1` isn't exact and money sums drift:

```python
from decimal import Decimal, ROUND_HALF_UP
from fractions import Fraction

Decimal("0.1") + Decimal("0.2")                                  # Decimal('0.3') - exact
Decimal(0.1)                                                     # 0.1000000000000000055511... (built from the float!)
Decimal("2.675").quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)   # Decimal('2.68')
round(2.675, 2)                                                  # 2.67 - the float is really 2.67499999...
Fraction(1, 3) + Fraction(1, 6)                                  # Fraction(1, 2)
```
- **`Decimal`**: base-10, configurable precision (28 significant digits default), explicit rounding modes. Use for money; build from **strings**, not floats.
- **`Fraction`**: exact numerator/denominator — exact ratios, slow if numbers grow.
- Both are much slower than `float`; use only where exactness matters.

### Common mistakes
- `math.floor(-2.5)` is `-3` (towards −∞); `int(-2.5)` is `-2` (towards zero).
- `==` on floats — use `isclose`, or `Decimal` for money.
- Mixing up `stdev` (sample) and `pstdev` (population).

---

## 14.9 A toolbox of small modules: `copy`, `pprint`, `enum`, `uuid`, `hashlib`, `json`, `csv`

### `copy` — shallow vs deep
- `copy.copy(x)` — **shallow**: new outer container, shared inner objects.
- `copy.deepcopy(x)` — recursive (handles cycles with a memo dict); slower; customise with `__copy__`/`__deepcopy__`.
- `copy.replace(obj, **changes)` (3.13+) — modified copy of a dataclass, namedtuple, datetime, etc.

```
shallow sees change: ['a', 'b', 'c'] | deep: ['a', 'b']
```
The shallow copy shares the `hosts` list, so appending through `cfg` shows in it.

### `pprint` — readable nested data
`pprint.pp(obj, width=50)` (3.8+) prints nested structures across lines in insertion order (`pprint.pprint` sorts dict keys by default); `pprint.pformat` returns the string.
```
{'service': 'api',
 'replicas': 3,
 'env': {'DB_URL': 'postgres://db:5432/app',
         'DEBUG': False},
 'ports': [80, 443],
 'owners': ['platform-team', 'sre']}
```

### `enum` — named constants
See [Chapter 10](../10-oop-in-python/README.md). `Enum` for a closed set, `StrEnum` (3.11+) whose members *are* strings (great for JSON/DBs), `IntEnum`, `Flag` for combinable bits, `auto()` (for `StrEnum`, the lower-cased member name):
```
StrEnum: active BANNED | Enum: Color.GREEN ['RED', 'GREEN']
```

### `uuid` — unique identifiers
A **UUID** is a 128-bit, 36-character identifier generated anywhere without coordination.

| Function | Based on | Use |
|---|---|---|
| `uuid4()` | random | the default for IDs |
| `uuid5(namespace, name)` / `uuid3` | SHA-1 / MD5 hash of a name | same input → same ID |
| `uuid1()` | time + MAC address | leaks the machine's MAC; avoid |
| `uuid7()` (3.14+) | Unix time + random | time-ordered, so database indexes stay compact |

```
uuid4 version: 4 | uuid5 is deterministic: 6639460f-3425-5329-8097-a58f06127860
```

### `hashlib` — cryptographic hashes
A **hash function** maps any bytes to a fixed-size **digest** (a short fingerprint); same input → same digest, and a good one makes collisions infeasible to find.

```python
hashlib.sha256(b"hello").hexdigest()      # '2cf24dba5fb0a30e...'
h = hashlib.sha256()
for chunk in (b"hel", b"lo"):              # hash large files chunk by chunk
    h.update(chunk)
key = hashlib.pbkdf2_hmac("sha256", b"s3cret", salt, 100_000)
```
Output:
```
sha256('hello'): 2cf24dba5fb0a30e ...
streamed equal : True
pbkdf2 key len : 32
```
- SHA-256 (or BLAKE2, `hashlib.blake2b`) for integrity checks, cache keys, content addressing (Git uses SHA-1 and is adding SHA-256).
- MD5 and SHA-1 are broken for security; OK only for non-security checksums (detecting accidental corruption).
- **Never store passwords with a fast hash.** Use a slow, salted **KDF** (key-derivation function; a **salt** is per-user random data so equal passwords hash differently): `hashlib.scrypt` or `pbkdf2_hmac` with `secrets.token_bytes(16)` salt and many iterations — or `argon2-cffi`/`bcrypt`. (The demo's fixed salt is for reproducibility only.)
- Message authentication (proving a message came from a holder of a shared key, unaltered): `hmac.new(key, msg, "sha256")`, compared with `hmac.compare_digest`.
- Built-in `hash()` is **not** stable — string hashes are randomised per process.

### `json` and `csv` — recap (Chapter 9)
- `json.dumps(obj, default=str, sort_keys=True, indent=2)` — `default` handles unsupported types (dates, sets, Decimal); tuples become lists; `json.loads` returns plain dicts/lists.
- `csv.DictWriter`/`DictReader` handle quoting (`"Li, Wei"` stays one field). Open files with `newline=""`.

```
json: {"id": 7, "tags": ["a", "b"], "when": "2026-09-28"} | back: ['a', 'b']
csv text: ['name,city', 'Asha,Pune', '"Li, Wei",Oslo']
csv read: {'name': 'Li, Wei', 'city': 'Oslo'}
```

### More small modules worth knowing

| Module | Does | Example |
|---|---|---|
| `tomllib` (3.11) | Read TOML config (read-only) | `tomllib.load(open("pyproject.toml", "rb"))` |
| `zipfile`, `tarfile`, `gzip` | Archives and compressed files | `gzip.open("log.gz", "rt")` streams compressed text |
| `base64` | Encode bytes as ASCII text | `base64.b64encode(b"hi")` → `b'aGk='` |
| `textwrap` | Wrap/indent/dedent paragraphs | `textwrap.dedent(help_text)` |
| `shlex` | Split a command line like a shell | `shlex.split('git commit -m "fix bug"')` → `['git', 'commit', '-m', 'fix bug']` |
| `difflib` | Diffs and "did you mean" matches | `difflib.get_close_matches("pyhton", ["python", "java"])` |
| `getpass` | Read a password without echo | `getpass.getpass()` |
| `string` | Constants and simple templates | `string.ascii_lowercase`, `string.Template("$name")` |
| `timeit`, `cProfile`, `tracemalloc` | Timing, profiling (where time is spent), memory tracing | [Chapter 17](../17-performance-memory-internals/README.md) |

---

## 14.10 Pointer: `collections`, `itertools`, `functools`

| Module | Highlights | Chapter |
|---|---|---|
| `collections` | `Counter`, `defaultdict`, `deque`, `namedtuple`, `OrderedDict`, `ChainMap` | 6 |
| `itertools` | `chain`, `islice`, `groupby`, `product`, `permutations`, `combinations`, `accumulate`, `batched` (3.12), `pairwise` | 11 |
| `functools` | `lru_cache`/`cache`, `partial`, `reduce`, `wraps`, `total_ordering`, `singledispatch`, `cached_property` | 7, 11, 12 |
| `heapq`, `bisect` | priority queues, binary search in sorted lists | 5, 6 |
| `dataclasses`, `abc`, `contextlib` | records, abstract classes, context managers | 10, 12 |

---

## 14.11 `sqlite3` — an embedded SQL database

**SQLite** is a full SQL database in a single file (or memory), running inside your process — no server. `sqlite3` is the built-in driver (DB-API 2.0, PEP 249, the same interface as PostgreSQL/MySQL drivers).

1. `con = sqlite3.connect("app.db")` (or `":memory:"`).
2. `con.execute(sql, params)`; `executemany` per row.
3. **Always use placeholders** (`?` or `:name`) — the driver escapes values, preventing **SQL injection**. Never build SQL with f-strings from user input.
4. `with con:` wraps a **transaction**: commit on success, rollback if an exception escapes (it does *not* close the connection — call `con.close()`).
5. `con.row_factory = sqlite3.Row` → columns by name.
6. Results: `.fetchone()`, `.fetchall()`, or iterate the cursor.

Script `08_sqlite_demo.py`:

```python
con = sqlite3.connect(":memory:")
con.row_factory = sqlite3.Row
con.execute("""CREATE TABLE expense (
                 id INTEGER PRIMARY KEY,
                 category TEXT NOT NULL,
                 amount REAL NOT NULL CHECK (amount > 0))""")
with con:
    con.executemany("INSERT INTO expense (category, amount) VALUES (?, ?)", rows)

total = con.execute("SELECT SUM(amount) AS s FROM expense WHERE category = ?",
                    (cat,)).fetchone()["s"]

try:
    with con:                          # both inserts, or neither
        con.execute("INSERT INTO expense (category, amount) VALUES ('gift', 5)")
        con.execute("INSERT INTO expense (category, amount) VALUES ('bad', -1)")
except sqlite3.IntegrityError as e:
    print("rolled back:", e)
```

Output:
```
food total: 19.75
  travel  n=1 sum=40.0
  food    n=2 sum=19.75
  books   n=1 sum=18.0
rolled back: CHECK constraint failed: amount > 0
rows now: 4
```
The valid `gift` insert rolled back with the invalid one — atomicity.

**When not to use it:** many concurrent writers (SQLite allows one writer at a time; WAL — write-ahead log — mode helps readers) or a database shared by several servers — use PostgreSQL/MySQL. Excellent for apps, tests, caches, CLIs, prototypes, small-to-medium sites. It ships in every Android/iOS device, Firefox and Chrome, many desktop apps, and is Django's default dev database.

**Common mistakes:** f-string SQL (injection); forgetting to commit; `(cat)` instead of `(cat,)` — parameters must be a sequence.

---

## 14.12 `urllib` and `http` (brief)

- `urllib.parse` — split, build, encode URLs: `urlparse`, `parse_qs`, `urlencode`, `urljoin`, `quote`.
- `urllib.request` — basic HTTP client: `urlopen(Request(url, headers=..., data=...), timeout=...)`.
- `urllib.error` — `HTTPError` (server replied 4xx client error / 5xx server error) and `URLError` (couldn't connect).
- `http.HTTPStatus` — named codes (`HTTPStatus.NOT_FOUND == 404`).
- `http.server` — minimal server (`python -m http.server 8000` shares a folder); **not for production**.

`09_urllib_http.py` starts a local server on `127.0.0.1` (a port is the numbered endpoint a service listens on; port 0 = any free port) in a background thread, then calls it:

```python
u = urlparse("https://api.example.com:8443/v1/users?id=7&sort=name#top")
print("parsed :", u.scheme, u.hostname, u.port, u.path, parse_qs(u.query))

req = Request(f"{base}/greet?{urlencode({'name': 'Asha'})}",
              headers={"Accept": "application/json"})
with urlopen(req, timeout=5) as resp:
    print("GET    :", resp.status, resp.headers["Content-Type"], json.load(resp))
```

Output:
```
parsed : https api.example.com 8443 /v1/users {'id': ['7'], 'sort': ['name']}
encode : q=type+hints&page=2
urljoin: https://x.org/docs/b
GET    : 200 application/json {'message': 'hello Asha'}
error  : 404 Not Found
```

**When not to use it:** real clients need connection pooling (reusing open connections across requests), JSON/auth/retry helpers and async, which `urllib.request` lacks — use **requests** (synchronous, most popular) or **httpx** (sync + async, HTTP/2). Servers: a framework (FastAPI, Flask, Django) behind uvicorn/gunicorn.

**Mistakes:** no `timeout=` (a stuck server hangs you forever); hand-built query strings instead of `urlencode`.

---

## 14.13 When to reach for third-party packages

### How to decide
1. **Is the stdlib good enough?** For scripts and tools that must run anywhere without installs (CI helpers, system scripts), prefer it.
2. **Is there a clear community standard** — de-facto, well maintained, widely used? Then the dependency is usually worth it.
3. **Cost:** install size, security updates, licence, supply-chain risk (compromised or typo-squatted packages). Pin versions with a lock file ([Chapter 8](../08-modules-packages-environments/README.md)).

| Need | stdlib | Common third-party choice | Why switch |
|---|---|---|---|
| HTTP client | `urllib.request` | **requests**, **httpx** | sessions/pooling, JSON, auth, retries, async (httpx) |
| CLI | `argparse` | **click**, **typer** | composable commands, less boilerplate, type-hint driven (typer) |
| Terminal output | `print`, `logging` | **rich** | colours, tables, progress bars, pretty tracebacks and logging handler |
| Dates | `datetime`, `zoneinfo` | `python-dateutil`, `pendulum`, `arrow` | month arithmetic, fuzzy parsing |
| Validation | dataclasses | **pydantic** | runtime validation ([Chapter 13](../13-type-hints/README.md)) |
| Data tables | `csv`, `statistics` | **pandas**, **polars** | vectorised analysis at scale |
| Numerics | `math`, `random` | **NumPy** | fast arrays |
| Testing | `unittest` | **pytest** | simpler tests, fixtures, plugins ([Chapter 15](../15-testing-and-debugging/README.md)) |
| Env config | `os.environ` | `python-dotenv`, `pydantic-settings` | `.env` files, typed settings |
| Passwords | `hashlib.scrypt` | `argon2-cffi`, `bcrypt` | modern password hashing |
| Database | `sqlite3` | **SQLAlchemy**, `psycopg` | ORM / PostgreSQL |

A taste (needs `pip install requests rich click` — not run here):

```python
import requests
r = requests.get("https://api.github.com/repos/python/cpython", timeout=10)
r.raise_for_status()                 # HTTPError on 4xx/5xx
print(r.json()["stargazers_count"])

from rich.console import Console
from rich.table import Table
t = Table("service", "status"); t.add_row("api", "[green]up[/]")
Console().print(t)                   # a coloured, boxed table

import click
@click.command()
@click.argument("name")
@click.option("--count", default=1, type=int)
def hello(name: str, count: int) -> None:
    for _ in range(count):
        click.echo(f"Hello {name}")
```

---

## 14.14 Chapter self-check

1. Naive vs aware datetime? → Aware has `tzinfo`; store aware UTC, convert with `astimezone(ZoneInfo(...))` for display.
2. Does `aware + timedelta(hours=1)` account for DST jumps? → No, wall-clock arithmetic; go via UTC.
3. Timer for measuring code? → `time.perf_counter()`.
4. Regex function for validating a whole string? → `re.fullmatch`.
5. Why `log.info("x=%s", x)` instead of an f-string? → Formatting skipped when disabled; aggregators group by template.
6. Root logger default level; which call logs a traceback; why duplicates? → WARNING; `log.exception` in `except`; handlers on several loggers in a propagating chain.
7. What should a library do about logging configuration? → Nothing beyond `getLogger(__name__)` (optionally `NullHandler`).
8. argparse error exit code? → 2.
9. Safest way to run a command with user input? → `subprocess.run([...list...], check=True, timeout=...)`, no `shell=True`.
10. `random` or `secrets` for a password-reset token? → `secrets`.
11. `round(2.5)`? → 2 (banker's rounding).
12. Shallow copy of `{"a": [1]}`, append to original's list — does the copy change? → Yes.
13. How to store passwords? → Salted slow KDF (scrypt/pbkdf2/argon2), never plain SHA-256.
14. How to pass values into SQL safely? → `?` placeholders with a parameters tuple.
15. Why prefer requests/httpx over urllib.request? → Pooling, JSON/auth/retries, async (httpx), nicer API.

## Chapter TL;DR
- **Time:** aware datetimes in UTC, `ZoneInfo` for display, ISO 8601 on the wire; `timedelta` has no months; `perf_counter` for durations, `monotonic` for timeouts.
- **re:** raw strings, named groups, `VERBOSE`, `finditer`, `fullmatch`, `sub` with a function; beware catastrophic backtracking; don't regex JSON/HTML/CSV.
- **logging:** `getLogger(__name__)`, configure once with `dictConfig`, lazy args, `log.exception`, libraries don't configure, structured logs in production.
- **argparse:** positionals/options/subcommands, `main(argv=None)`, exit code 2 on errors.
- **OS:** `os.environ.get`, `sys.argv`/`sys.exit`, `subprocess.run([...], check=True, timeout=...)`, `shutil` for trees, `pathlib` for paths, `tempfile` for scratch space.
- **Numbers:** `random` (seeded `Random`) vs `secrets`; `math.isclose`, banker's `round`; `statistics`; `Decimal` (from strings) for money.
- **Toolbox:** `deepcopy`, `pprint.pp`, `StrEnum`, `uuid4`/`uuid7`, `hashlib` (SHA-256; KDFs for passwords), `json` `default=`, `csv.Dict*`.
- **sqlite3:** placeholders, `with con:` transactions, `sqlite3.Row`.
- **HTTP:** `urllib.parse` always; `urllib.request` for quick scripts; requests/httpx for real work.
- **Third-party** when a community standard clearly beats the stdlib — then pin it.
