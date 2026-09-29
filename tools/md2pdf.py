#!/usr/bin/env python3
"""Convert a chapter README/notes Markdown file into a Notability-friendly PDF.

Usage:
    python3 tools/md2pdf.py <input.md> <output.pdf>

Renders code as monospace blocks, styled tables, and callout boxes, then uses
headless Chrome to print to PDF. The whole document uses the Monaco font.
"""
import html, re, os, sys, subprocess, tempfile

CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"


def inline(t):
    codes = []
    def stash(m):
        codes.append(m.group(1)); return "\x00%d\x00" % (len(codes) - 1)
    t = re.sub(r"`([^`]+)`", stash, t)
    t = html.escape(t)
    t = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", t)
    t = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", t)
    t = re.sub(r"(?<!\*)\*([^*]+)\*(?!\*)", r"<em>\1</em>", t)
    t = re.sub(r"\x00(\d+)\x00", lambda m: "<code>%s</code>" % html.escape(codes[int(m.group(1))]), t)
    return t


def md_to_html(md):
    lines = md.split("\n"); out = []; i = 0; n = len(lines)
    while i < n:
        ln = lines[i]
        if ln.startswith("```"):
            i += 1; buf = []
            while i < n and not lines[i].startswith("```"):
                buf.append(lines[i]); i += 1
            i += 1
            out.append("<pre><code>%s</code></pre>" % html.escape("\n".join(buf))); continue
        m = re.match(r"^(#{1,4})\s+(.*)$", ln)
        if m:
            lvl = len(m.group(1)); out.append("<h%d>%s</h%d>" % (lvl, inline(m.group(2)), lvl)); i += 1; continue
        if ln.strip() == "---":
            out.append("<hr>"); i += 1; continue
        if ln.lstrip().startswith("|"):
            tbl = []
            while i < n and lines[i].lstrip().startswith("|"):
                tbl.append(lines[i]); i += 1
            rows = [[c.strip() for c in r.strip().strip("|").split("|")] for r in tbl]
            out.append("<table><thead><tr>" + "".join("<th>%s</th>" % inline(c) for c in rows[0]) + "</tr></thead><tbody>")
            for r in rows[2:]:
                out.append("<tr>" + "".join("<td>%s</td>" % inline(c) for c in r) + "</tr>")
            out.append("</tbody></table>"); continue
        if ln.startswith(">"):
            buf = []
            while i < n and lines[i].startswith(">"):
                buf.append(lines[i][1:].strip()); i += 1
            out.append("<blockquote>%s</blockquote>" % inline(" ".join(buf))); continue
        if re.match(r"^\s*-\s+", ln):
            out.append("<ul>")
            while i < n and (re.match(r"^\s*-\s+", lines[i]) or (lines[i].startswith("  ") and lines[i].strip() and not lines[i].lstrip().startswith("|"))):
                if re.match(r"^\s*-\s+", lines[i]):
                    item = re.sub(r"^\s*-\s+", "", lines[i]); i += 1
                    while i < n and lines[i].startswith("  ") and lines[i].strip() and not re.match(r"^\s*-\s+", lines[i]) and not lines[i].lstrip().startswith("|"):
                        item += " " + lines[i].strip(); i += 1
                    out.append("<li>%s</li>" % inline(item))
                else:
                    i += 1
            out.append("</ul>"); continue
        if ln.strip() == "":
            i += 1; continue
        buf = [ln]; i += 1
        while i < n and lines[i].strip() and not lines[i].startswith(("#", "```", ">", "|")) and lines[i].strip() != "---" and not re.match(r"^\s*-\s+", lines[i]):
            buf.append(lines[i]); i += 1
        out.append("<p>%s</p>" % inline(" ".join(buf)))
    return "\n".join(out)


CSS = """
:root{color-scheme:light}
*{box-sizing:border-box}
body{font-family:Monaco,'Courier New',monospace;font-size:11px;line-height:1.55;color:#1b1b1b;max-width:840px;margin:0 auto;padding:24px 30px}
h1{font-size:20px;border-bottom:3px solid #2d6cdf;padding-bottom:6px;margin:0 0 14px}
h2{font-size:16px;margin:24px 0 8px;padding-top:6px;border-top:1px solid #e2e2e2;color:#12408a}
h3{font-size:13.5px;margin:16px 0 6px;color:#333}
p{margin:8px 0}
hr{border:0;border-top:1px solid #e2e2e2;margin:16px 0}
code{font-family:Monaco,'Courier New',monospace;font-size:10.5px;background:#eef1f6;color:#0b3a86;padding:1px 5px;border-radius:4px}
pre{background:#f6f8fb;border:1px solid #d6ddea;border-left:4px solid #2d6cdf;border-radius:6px;padding:11px 13px;overflow-x:auto;margin:10px 0;page-break-inside:avoid}
pre code{background:none;color:#10233f;padding:0;font-size:10.5px;line-height:1.5;white-space:pre;display:block}
table{border-collapse:collapse;width:100%;margin:10px 0;font-size:10.5px;page-break-inside:avoid}
th,td{border:1px solid #ccd4e0;padding:5px 9px;text-align:left;vertical-align:top}
th{background:#eaf0fb}
tr:nth-child(even) td{background:#fafbfd}
blockquote{margin:10px 0;padding:8px 13px;background:#fff8e6;border-left:4px solid #e0a92b;border-radius:5px}
ul{margin:8px 0 8px 4px;padding-left:20px}
li{margin:3px 0}
strong{color:#111}
@media print{body{max-width:none;padding:0 6px}h2{page-break-after:avoid}}
"""


def main():
    if len(sys.argv) != 3:
        print("usage: python3 tools/md2pdf.py <input.md> <output.pdf>"); sys.exit(1)
    src, out_pdf = sys.argv[1], sys.argv[2]
    title = os.path.splitext(os.path.basename(src))[0]
    body = md_to_html(open(src, encoding="utf-8").read())
    doc = "<!doctype html><html><head><meta charset='utf-8'><title>%s</title><style>%s</style></head><body>%s</body></html>" % (title, CSS, body)
    html_path = os.path.splitext(out_pdf)[0] + ".html"
    open(html_path, "w", encoding="utf-8").write(doc)
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--no-pdf-header-footer",
                    "--print-to-pdf=" + out_pdf, html_path], check=True,
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    print("wrote", out_pdf, "(", os.path.getsize(out_pdf), "bytes )")


if __name__ == "__main__":
    main()
