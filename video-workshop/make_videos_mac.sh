#!/usr/bin/env bash
# Render the workshop videos on your Mac (no cloud, no quota).
#   one-time setup:  brew install node ffmpeg python imagemagick && brew install --cask google-chrome
#   usage:           ./video-workshop/make_videos_mac.sh system-design-patterns   (or lld, …)
# Videos, subtitles and slide decks are written to <subject>/videos/. Re-running skips finished episodes.
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd); HERE=$ROOT/video-workshop; BOOK=${1:?usage: $0 <subject-folder>}
export CHROME_PATH=${CHROME_PATH:-"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"}
TOOLS=$HERE/.local; mkdir -p "$TOOLS"
[ -d "$ROOT/system-design-patterns/tools/node_modules" ] || (cd "$ROOT/system-design-patterns/tools" && npm install --silent)
[ -x "$TOOLS/venv/bin/piper" ] || { python3 -m venv "$TOOLS/venv"; "$TOOLS/venv/bin/pip" install -q piper-tts; }
VOICE=$TOOLS/en_US-lessac-medium.onnx
[ -f "$VOICE" ] || for f in en_US-lessac-medium.onnx en_US-lessac-medium.onnx.json; do
  curl -sSL -o "$TOOLS/$f" "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/$f"; done
OUT=$ROOT/$BOOK/videos
for js in "$OUT"/scripts/ep*.json; do
  ep=$(basename "$js" .json); work=$TOOLS/work/$BOOK/$ep
  if ls "$OUT"/*"$(python3 -c "import json,sys;d=json.load(open(sys.argv[1]));print('Ep%02d'%int(d['episode']))" "$js")"-*.mp4 >/dev/null 2>&1; then echo "skip $ep (done)"; continue; fi
  echo "== $BOOK $ep"; mkdir -p "$work"
  python3 "$HERE/build_video.py" "$js" "$work" --piper "$TOOLS/venv/bin/piper" --voice "$VOICE"
  mp4=$(ls "$work"/*.mp4 | head -1); base=$(basename "$mp4" .mp4)
  cp "$mp4" "$OUT/"; cp "$work/subtitles.srt" "$OUT/$base.srt"
  python3 "$HERE/make_deck.py" "$work" "$OUT/$base-Slides.pdf"
  rm -rf "$work"
done
echo "All done: $OUT"
