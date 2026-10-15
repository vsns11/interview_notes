# Video workshop pipeline

Turns a workshop script (JSON) into a narrated 1080p video: sepia slides that build step by step, an offline neural voice-over (Piper), and subtitles (embedded + `.srt`).

```bash
# one-time: Piper TTS + a voice
python3 -m venv .venv && .venv/bin/pip install piper-tts
curl -LO https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx
curl -LO https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx.json

# build
CHROME_PATH=/path/to/chrome python3 build_video.py script.json out/ --piper .venv/bin/piper --voice en_US-lessac-medium.onnx
```

| File | Purpose |
|---|---|
| `render_slides.mjs` | Renders one PNG per narration step (layouts: title, section, bullets, twocol, table, diagram (mermaid), number, quiz, recap). Uses puppeteer-core + mermaid from `../system-design-patterns/tools/node_modules`. |
| `build_video.py` | Piper TTS per step (cached), pads pauses, assembles images + audio with ffmpeg, writes the MP4 and `subtitles.srt`. |
