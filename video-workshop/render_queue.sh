#!/usr/bin/env bash
# Render every approved episode script, in book order, one or two at a time.
# A script is "approved" when <dir>/<book>/epNN.json has a sibling epNN.ok marker.
# Finished videos go to <dir>/<book>/out/<name>.mp4 plus a delivery copy
# (<= ~29 MB parts) in <dir>/deliver/<book>/. Stops when <dir>/STOP exists and
# nothing is left to do.
#   usage: render_queue.sh <videoDir> "<book1> <book2> ..." [parallel=2]
set -u
DIR=$1; BOOKS=$2; PAR=${3:-2}
HERE=$(cd "$(dirname "$0")" && pwd)
export CHROME_PATH=${CHROME_PATH:-/opt/pw-browsers/chromium-1194/chrome-linux/chrome}
PIPER=$DIR/venv/bin/piper; VOICE=$DIR/voices/en_US-lessac-medium.onnx

render_one() {  # $1 book  $2 json
  local book=$1 js=$2 ep; ep=$(basename "$js" .json)
  local out=$DIR/$book/out/$ep; mkdir -p "$out" "$DIR/deliver/$book"
  if python3 "$HERE/build_video.py" "$js" "$out" --piper "$PIPER" --voice "$VOICE" > "$out/build.log" 2>&1; then
    local mp4; mp4=$(ls "$out"/*.mp4 | head -1)
    # delivery copy: 1080p, still-image tuned, small; split into <=29 MB parts if needed
    local base; base=$(basename "$mp4" .mp4)
    ffmpeg -y -v error -i "$mp4" -map 0 -c:v libx264 -preset veryslow -tune stillimage -crf 28 -r 10 -g 600 \
      -c:a aac -b:a 64k -ac 1 -c:s mov_text -movflags +faststart "$out/$base.small.mp4"
    local size; size=$(stat -c %s "$out/$base.small.mp4")
    if [ "$size" -le 30000000 ]; then
      cp "$out/$base.small.mp4" "$DIR/deliver/$book/$base.mp4"
    else
      local d parts seg; d=$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$out/$base.small.mp4")
      parts=$(python3 -c "import math;print(math.ceil($size/28000000))")
      seg=$(python3 -c "print(int($d/$parts)+1)")
      ffmpeg -y -v error -i "$out/$base.small.mp4" -map 0 -c copy -f segment -segment_time "$seg" \
        -reset_timestamps 1 "$DIR/deliver/$book/$base-Part%d.mp4"
    fi
    cp "$out/subtitles.srt" "$DIR/deliver/$book/$base.srt"
    rm -rf "$out/frames" "$out/audio" "$out/narration.wav"
    echo "$(date +%H:%M) done $book/$ep $(grep -o '([0-9.]* min' "$out/build.log")" >> "$DIR/queue.log"
    touch "${js%.json}.rendered"
  else
    echo "$(date +%H:%M) FAILED $book/$ep (see $out/build.log)" >> "$DIR/queue.log"
    touch "${js%.json}.failed"
  fi
}

while true; do
  todo=()
  for b in $BOOKS; do
    for ok in $(ls "$DIR/$b"/ep*.ok 2>/dev/null | sort); do
      js=${ok%.ok}.json
      [ -e "${js%.json}.rendered" ] || [ -e "${js%.json}.failed" ] || [ -e "${js%.json}.busy" ] || todo+=("$b|$js")
    done
  done
  if [ ${#todo[@]} -eq 0 ]; then
    [ -e "$DIR/STOP" ] && break
    sleep 60; continue
  fi
  running=$(ls "$DIR"/*/ep*.busy 2>/dev/null | wc -l)
  if [ "$running" -ge "$PAR" ]; then sleep 20; continue; fi
  item=${todo[0]}; b=${item%%|*}; js=${item#*|}
  touch "${js%.json}.busy"
  ( render_one "$b" "$js"; rm -f "${js%.json}.busy" ) &
  sleep 5
done
wait
echo "$(date +%H:%M) queue finished" >> "$DIR/queue.log"
