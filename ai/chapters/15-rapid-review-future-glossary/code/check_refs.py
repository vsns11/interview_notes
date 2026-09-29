"""Verify cross-references in the handbook's README files.

Checks, for every Markdown link [text](target) that is not a web URL:
  1. the target file exists (relative to the README that links to it);
  2. if the link text names a section like "§7.3" (or "7.3"), the target
     README really has a heading "## 7.3 ...";
  3. images ![alt](img/x.png) exist.
Usage:  python check_refs.py [README.md ...]   (default: every chapter README)
"""
import re
import sys
from pathlib import Path

LINK = re.compile(r"(!?)\[([^\]]*)\]\(([^)\s]+)\)")
SECTION = re.compile(r"§\s?(\d+\.\d+)")


def headings(md: Path) -> set[str]:
    """Section numbers of all '## N.M ...' headings in a README."""
    nums = set()
    for line in md.read_text(encoding="utf-8").splitlines():
        m = re.match(r"^##\s+(\d+\.\d+)\b", line)
        if m:
            nums.add(m.group(1))
    return nums


def check(readme: Path) -> list[str]:
    problems, cache = [], {}
    text = readme.read_text(encoding="utf-8")
    in_code = False
    for lineno, line in enumerate(text.splitlines(), 1):
        if line.lstrip().startswith("```"):
            in_code = not in_code
            continue
        if in_code:
            continue
        for bang, label, target in LINK.findall(line):
            if re.match(r"^[a-z]+:", target) or target.startswith("#"):
                continue                      # web link or in-page anchor
            path = (readme.parent / target.split("#")[0]).resolve()
            if not path.exists():
                problems.append(f"{readme.parent.name}:{lineno}: missing {target}")
                continue
            if bang or path.suffix != ".md":
                continue
            for sec in SECTION.findall(label):
                if path not in cache:
                    cache[path] = headings(path)
                if sec not in cache[path]:
                    problems.append(f"{readme.parent.name}:{lineno}: §{sec} not in {target}")
    return problems


if __name__ == "__main__":
    here = Path(__file__).resolve().parent.parent          # this chapter folder
    files = [Path(a) for a in sys.argv[1:]] or sorted(here.parent.glob("*/README.md"))
    total = 0
    for f in files:
        probs = check(f)
        total += len(probs)
        for p in probs:
            print(p)
    print(f"Checked {len(files)} README file(s): {total} problem(s).")
