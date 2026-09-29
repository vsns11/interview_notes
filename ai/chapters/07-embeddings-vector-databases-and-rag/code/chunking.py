"""Naive fixed-size chunking vs structure-aware chunking with breadcrumbs.

Structure-aware: split on headings, keep tables/lists whole, pack paragraphs
up to a token budget with a small overlap, and prefix each chunk with its
heading trail ("Handbook > Travel > Meals") so it makes sense on its own.
"""
import re

HANDBOOK = """# Employee Handbook
## Travel
### Meals
The limit is $50 per day. Alcohol is not reimbursed.
Receipts are required for every item above $10.

### Hotels
Book through the travel portal. The nightly cap depends on the city:

| City tier | Nightly cap |
|---|---|
| Tier 1 (London, New York) | $300 |
| Tier 2 (all other cities) | $180 |

## Leave
### Parental leave
Full-time employees receive 16 weeks of paid parental leave.
Leave can start up to two weeks before the due date.
"""


def tokens(text):
    return len(text.split())          # word count as a simple token proxy


def naive_chunks(text, size=25, overlap=0):
    words = text.split()
    step = size - overlap
    return [" ".join(words[i:i + size]) for i in range(0, len(words), step)]


def structure_chunks(md, max_tokens=40, overlap_lines=1):
    """Return (breadcrumb, text) chunks that respect headings and tables."""
    trail, blocks, chunks = [], [], []

    def flush():
        # pack blocks into chunks <= max_tokens; carry a little overlap
        cur = []
        for blk in blocks:
            if cur and tokens("\n".join(cur + [blk])) > max_tokens:
                chunks.append((" > ".join(trail), "\n".join(cur)))
                cur = cur[-overlap_lines:] if not cur[-1].startswith("|") else []
            cur.append(blk)
        if cur:
            chunks.append((" > ".join(trail), "\n".join(cur)))
        blocks.clear()

    lines = md.strip().splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        m = re.match(r"(#+) (.*)", line)
        if m:                                   # new section: close the previous one
            flush()
            level = len(m.group(1))
            del trail[level - 1:]
            trail.append(m.group(2))
        elif line.startswith("|"):              # keep the whole table as ONE block
            table = []
            while i < len(lines) and lines[i].startswith("|"):
                table.append(lines[i])
                i += 1
            blocks.append("\n".join(table))
            continue
        elif line.strip():
            blocks.append(line)
        i += 1
    flush()
    return chunks


if __name__ == "__main__":
    print("NAIVE: every 25 words, no overlap")
    for n, c in enumerate(naive_chunks(HANDBOOK), 1):
        print(f"--- chunk {n}: {c[:150]}")

    print("\nSTRUCTURE-AWARE: headings, whole tables, breadcrumbs, 1-line overlap")
    for n, (crumb, text) in enumerate(structure_chunks(HANDBOOK), 1):
        print(f"--- chunk {n} [{crumb}] ({tokens(text)} words)")
        print("    " + text.replace("\n", "\n    "))
