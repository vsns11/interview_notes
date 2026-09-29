# Generates every image/audio file the chapter's examples use -- no downloads.
# Run once:  python3 make_assets.py   (needs Pillow >= 11.2 for AVIF output)
# Writes into ./media/ :
#   hills-400.jpg, hills-800.jpg, hills-1600.jpg   same picture, three widths (for srcset)
#   hills-800.png / .webp / .avif                  same picture, other formats (size comparison)
#   photo-800.jpg/.png/.webp/.avif              same picture with photo-like grain (size comparison)
#   hills-square-600.jpg                           square crop (art direction in <picture>)
#   logo.png                                       small transparent PNG
#   poster.jpg                                     poster frame for the <video>
#   tone.wav                                       2-second 440 Hz beep for <audio>
import math, struct, wave
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

OUT = Path(__file__).parent / "media"
OUT.mkdir(exist_ok=True)

def scene(w, h, label):
    """Draw a simple landscape: sky gradient, sun, two hills, a label with the pixel width."""
    im = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(im)
    for y in range(h):                       # vertical sky gradient
        t = y / h
        d.line([(0, y), (w, y)], fill=(int(90 + 120 * t), int(150 + 80 * t), 235))
    s = w / 800                              # scale factor so every size looks the same
    d.ellipse([560 * s, 60 * s, 680 * s, 180 * s], fill=(255, 205, 60))
    d.polygon([(0, h), (0, 330 * s), (260 * s, 220 * s), (520 * s, 360 * s), (520 * s, h)], fill=(60, 140, 80))
    d.polygon([(300 * s, h), (620 * s, 260 * s), (w, 340 * s), (w, h)], fill=(40, 110, 70))
    font = ImageFont.load_default(size=int(44 * s))
    d.rounded_rectangle([20 * s, 20 * s, 330 * s, 90 * s], radius=12 * s, fill=(255, 255, 255))
    d.text((36 * s, 30 * s), label, fill=(20, 20, 60), font=font)
    return im

for w in (400, 800, 1600):
    scene(w, w * 2 // 3, f"{w}w file").save(OUT / f"hills-{w}.jpg", quality=80)

base = scene(800, 533, "800w file")
base.save(OUT / "hills-800.png", optimize=True)
base.save(OUT / "hills-800.webp", quality=80)
base.save(OUT / "hills-800.avif", quality=60)

# "photo-like" version: same scene plus fine grain/noise, the kind of detail real photos have
import random
random.seed(7)
photo = base.copy()
px = photo.load()
for y in range(photo.height):
    for x in range(photo.width):
        r, g, b = px[x, y]; n = random.randint(-18, 18)
        px[x, y] = (max(0, min(255, r + n)), max(0, min(255, g + n)), max(0, min(255, b + n)))
photo.save(OUT / "photo-800.jpg", quality=80)
photo.save(OUT / "photo-800.png", optimize=True)
photo.save(OUT / "photo-800.webp", quality=80)
photo.save(OUT / "photo-800.avif", quality=60)

# art direction: a square crop centred on the sun, with its own label
sq = scene(1200, 800, "square crop").crop((560, 0, 1160, 600))
ImageDraw.Draw(sq).text((20, 540), "square crop (600x600)", fill=(255, 255, 255),
                        font=ImageFont.load_default(size=36))
sq.save(OUT / "hills-square-600.jpg", quality=80)

# transparent logo (PNG keeps the alpha channel; JPEG cannot)
logo = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
ld = ImageDraw.Draw(logo)
ld.ellipse([8, 8, 152, 152], fill=(108, 92, 231, 255))
ld.text((42, 50), "HC", fill=(255, 255, 255, 255), font=ImageFont.load_default(size=56))
logo.save(OUT / "logo.png")

poster = scene(640, 360, "poster frame")
poster.save(OUT / "poster.jpg", quality=80)

# 2 s, 440 Hz sine tone, 16-bit mono WAV at 8 kHz (tiny file, plays in every browser)
rate, secs = 8000, 2
with wave.open(str(OUT / "tone.wav"), "wb") as wf:
    wf.setnchannels(1); wf.setsampwidth(2); wf.setframerate(rate)
    wf.writeframes(b"".join(struct.pack("<h", int(8000 * math.sin(2 * math.pi * 440 * i / rate)))
                            for i in range(rate * secs)))

for p in sorted(OUT.iterdir()):
    print(f"{p.name:22} {p.stat().st_size:>8,} bytes")
