// Screenshot a local HTML file (or a URL) to PNG with headless Chrome — for HTML/CSS/JS/React book figures.
//   node tools_web_shot.mjs <page.html> <out.png> [width=900] [height=auto|px] [--dark] [--wait=ms]
import puppeteer from 'puppeteer-core';
import path from 'path';
const pos = process.argv.slice(2).filter(a => !a.startsWith('--')); const [src, out, w = '900', h = 'auto'] = pos;
const b = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: 'new' });
const p = await b.newPage();
await p.setViewport({ width: +w, height: h === 'auto' ? 600 : +h, deviceScaleFactor: 2 });
await p.emulateMediaFeatures([{ name: 'prefers-color-scheme', value: process.argv.includes('--dark') ? 'dark' : 'light' }]);  // don't inherit the Mac's theme
await p.goto(/^https?:/.test(src) ? src : 'file://' + path.resolve(src), { waitUntil: 'networkidle0' });
const wm = process.argv.find(a => a.startsWith('--wait=')); if (wm) await new Promise(r => setTimeout(r, +wm.slice(7)));
await p.screenshot({ path: out, fullPage: h === 'auto' });
await b.close(); console.log('wrote', out);
