// tools_run: skip  (asset generator — run once: node make_video.mjs)
// Records a 3-second animated <canvas> in headless Chrome with MediaRecorder and saves it
// as media/clip.webm (VP8/VP9 WebM). No downloads: the browser draws and encodes the video itself.
import puppeteer from 'puppeteer-core';
import fs from 'fs';

const browser = await puppeteer.launch({
  executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: 'new' });
const page = await browser.newPage();
const b64 = await page.evaluate(async () => {
  const c = Object.assign(document.createElement('canvas'), { width: 640, height: 360 });
  document.body.append(c);
  const ctx = c.getContext('2d');
  const rec = new MediaRecorder(c.captureStream(30), { mimeType: 'video/webm' });
  const chunks = [];
  rec.ondataavailable = e => chunks.push(e.data);
  const done = new Promise(r => (rec.onstop = r));
  rec.start();
  const t0 = performance.now();
  await new Promise(resolve => {
    (function frame() {
      const t = (performance.now() - t0) / 1000;
      ctx.fillStyle = '#9cc4f0'; ctx.fillRect(0, 0, 640, 360);           // sky
      ctx.fillStyle = '#ffcd3c';                                         // sun moving left to right
      ctx.beginPath(); ctx.arc(60 + t * 170, 90, 40, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#2e7d4f'; ctx.fillRect(0, 260, 640, 100);          // ground
      ctx.fillStyle = '#123'; ctx.font = '28px sans-serif';
      ctx.fillText(`t = ${t.toFixed(1)} s`, 20, 330);
      t < 3 ? requestAnimationFrame(frame) : resolve();
    })();
  });
  rec.stop(); await done;
  const buf = await new Blob(chunks, { type: 'video/webm' }).arrayBuffer();
  let s = ''; new Uint8Array(buf).forEach(b => (s += String.fromCharCode(b)));
  return btoa(s);
});
fs.writeFileSync('media/clip.webm', Buffer.from(b64, 'base64'));
console.log('wrote media/clip.webm', fs.statSync('media/clip.webm').size, 'bytes');
await browser.close();
