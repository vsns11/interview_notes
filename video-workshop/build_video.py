#!/usr/bin/env python3
"""Build a narrated workshop video from a script JSON.

  python3 build_video.py <script.json> <outDir> [--piper PATH] [--voice MODEL.onnx]

Steps: render slides (render_slides.mjs) -> Piper TTS per step (cached by text)
-> pad each clip with a short pause -> one audio track + an image sequence with
exact durations -> H.264/AAC MP4 with soft subtitles, plus a .srt file.
Needs: node, Chromium (CHROME_PATH), ffmpeg/ffprobe, Piper (offline neural TTS).
"""
import argparse, hashlib, json, os, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
GAP = 0.45          # seconds of silence after each step
SCENE_GAP = 0.35    # extra silence when the slide changes


def run(cmd, **kw):
    subprocess.run(cmd, check=True, **kw)


def dur(path):
    out = subprocess.check_output(['ffprobe', '-v', 'error', '-show_entries', 'format=duration',
                                   '-of', 'csv=p=0', path], text=True)
    return float(out.strip())


def srt_time(t):
    ms = int(round(t * 1000)); h, ms = divmod(ms, 3600000); m, ms = divmod(ms, 60000); s, ms = divmod(ms, 1000)
    return f'{h:02}:{m:02}:{s:02},{ms:03}'


def chunks(text, n=14):
    """Split narration into subtitle-sized pieces (~n words)."""
    words = text.split(); out = []
    while words:
        out.append(' '.join(words[:n])); words = words[n:]
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('script'); ap.add_argument('out')
    ap.add_argument('--piper', default=os.environ.get('PIPER', 'piper'))
    ap.add_argument('--voice', default=os.environ.get('PIPER_VOICE'))
    ap.add_argument('--skip-render', action='store_true')
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    if not a.skip_render:
        run(['node', os.path.join(HERE, 'render_slides.mjs'), a.script, a.out])
    frames = json.load(open(os.path.join(a.out, 'frames.json')))
    script = json.load(open(a.script))
    adir = os.path.join(a.out, 'audio'); os.makedirs(adir, exist_ok=True)

    clips, images, subs, t = [], [], [], 0.0
    for i, f in enumerate(frames):
        text = f['say'].strip()
        key = hashlib.sha1(text.encode()).hexdigest()[:16]
        raw = os.path.join(adir, f'{key}.wav')
        if not os.path.exists(raw):
            run([a.piper, '-m', a.voice, '-f', raw, '--sentence_silence', '0.25'],
                input=text.encode(), stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        last_of_scene = i + 1 == len(frames) or frames[i + 1]['scene'] != f['scene']
        pad = GAP + (SCENE_GAP if last_of_scene else 0)
        padded = os.path.join(adir, f'p{i:04}.wav')
        run(['ffmpeg', '-y', '-v', 'error', '-i', raw, '-af', f'apad=pad_dur={pad}',
             '-ar', '22050', '-ac', '1', padded])
        d = dur(padded); speech = dur(raw)
        clips.append(padded); images.append((f['file'], d))
        # subtitles: spread the pieces over the spoken part, proportional to length
        pieces = chunks(text); total_words = sum(len(p.split()) for p in pieces); st = t
        for p in pieces:
            pd = speech * len(p.split()) / total_words
            subs.append((st, st + pd, p)); st += pd
        t += d

    with open(os.path.join(a.out, 'audio.txt'), 'w') as fh:
        for c in clips: fh.write(f"file '{os.path.abspath(c)}'\n")
    with open(os.path.join(a.out, 'images.txt'), 'w') as fh:
        for img, d in images: fh.write(f"file '{os.path.abspath(img)}'\nduration {d:.3f}\n")
        fh.write(f"file '{os.path.abspath(images[-1][0])}'\n")
    srt = os.path.join(a.out, 'subtitles.srt')
    with open(srt, 'w') as fh:
        for n, (s, e, p) in enumerate(subs, 1): fh.write(f'{n}\n{srt_time(s)} --> {srt_time(e)}\n{p}\n\n')
    full = os.path.join(a.out, 'narration.wav')
    run(['ffmpeg', '-y', '-v', 'error', '-f', 'concat', '-safe', '0', '-i', os.path.join(a.out, 'audio.txt'), '-c', 'copy', full])
    name = f"{script['series'].replace(' ', '-')}-Ep{int(script['episode']):02}-{script['title'].replace(' ', '-')}"
    name = ''.join(ch for ch in name if ch.isalnum() or ch in '-_')
    mp4 = os.path.join(a.out, name + '.mp4')
    run(['ffmpeg', '-y', '-v', 'error', '-f', 'concat', '-safe', '0', '-i', os.path.join(a.out, 'images.txt'),
         '-i', full, '-i', srt, '-map', '0:v', '-map', '1:a', '-map', '2:s',
         '-c:v', 'libx264', '-preset', 'medium', '-tune', 'stillimage', '-crf', '20', '-r', '30',
         '-pix_fmt', 'yuv420p', '-vf', 'scale=1920:1080',
         '-c:a', 'aac', '-b:a', '128k', '-c:s', 'mov_text', '-metadata:s:s:0', 'language=eng',
         '-movflags', '+faststart', mp4])
    print(f'wrote {mp4}  ({dur(mp4)/60:.1f} min, {os.path.getsize(mp4)/1e6:.1f} MB, {len(frames)} steps)')


if __name__ == '__main__':
    sys.exit(main())
