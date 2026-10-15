#!/usr/bin/env python3
"""Make a downloadable slide deck (PDF) from rendered frames: the fully built
(last) frame of every scene, in order.   usage: make_deck.py <outDir> <deck.pdf>"""
import json, os, subprocess, sys
out, pdf = sys.argv[1], sys.argv[2]
frames = json.load(open(os.path.join(out, 'frames.json')))
last = {}
for f in frames:
    last[f['scene']] = f['file']          # dict keeps first-seen scene order
pages = list(last.values())
subprocess.run(['convert', *pages, '-resize', '1600x900', '-quality', '80', '-compress', 'jpeg', pdf], check=True)
print(f'wrote {pdf} ({len(pages)} slides, {os.path.getsize(pdf)/1e6:.1f} MB)')
