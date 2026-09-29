// tools_run: skip  (helper imported by the other .mjs scripts in this folder)
// Same idea as Chapters 7–9: open one of this folder's pages in real headless Chrome and let the
// browser answer. Adds what a responsive chapter needs: viewport width, mobile emulation (which makes
// Chrome honour <meta name="viewport">), user-preference emulation (dark mode, reduced motion…),
// screenshots, and a "contact sheet" that puts several screenshots side by side in one PNG.
import puppeteer from 'puppeteer-core';
import path from 'node:path';
import fs from 'node:fs';

const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';

// opts: width, height, dpr (device pixel ratio), mobile (true = phone emulation),
//       media: [{ name: 'prefers-color-scheme', value: 'dark' }, …] (later entries win)
export async function withPage(file, fn, opts = {}) {
  const { width = 900, height = 700, dpr = 1, mobile = false, media = [] } = opts;
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new' });
  try {
    const page = await browser.newPage();
    await page.setViewport({ width, height, deviceScaleFactor: dpr, isMobile: mobile, hasTouch: mobile });
    // Emulate OS preferences through the DevTools protocol (it accepts every media feature).
    // Light scheme is forced unless asked otherwise, so results don't depend on this computer's theme.
    const features = [{ name: 'prefers-color-scheme', value: 'light' }, ...media];
    const cdp = await page.createCDPSession();
    await cdp.send('Emulation.setEmulatedMedia', { features });
    await page.goto('file://' + path.resolve(file), { waitUntil: 'load' });
    await page.evaluate(() => document.fonts.ready);
    return await fn(page);
  } finally { await browser.close(); }
}

// Screenshot a page (full page by default) and return the PNG bytes.
export const shot = (file, opts = {}) => withPage(file, page =>
  page.screenshot({ fullPage: opts.fullPage ?? true, ...(opts.clip ? { clip: opts.clip } : {}) }),
  { dpr: 2, ...opts });

// Put PNGs side by side (wrapping) with a caption above each, and save as one PNG.
// items: [{ png: Buffer, label: string, cssWidth?: number }]; each image is drawn at cssWidth
// (default: its own CSS width, i.e. pixel width / 2 because shots are taken at dpr 2).
export async function sheet(items, out, { width = 1000, scale = 1 } = {}) {
  const cells = items.map(({ png, label, cssWidth }) => {
    const pxW = png.readUInt32BE(16);                       // PNG header: width at byte 16
    const w = (cssWidth ?? pxW / 2) * scale;
    return `<figure><figcaption>${label}</figcaption>
      <img style="width:${w}px" src="data:image/png;base64,${png.toString('base64')}"></figure>`;
  }).join('');
  const html = `<!doctype html><meta charset="utf-8"><style>
    body{margin:10px;font:600 13px system-ui,sans-serif;background:#fff;display:flex;flex-wrap:wrap;gap:14px;align-items:flex-start}
    figure{margin:0} figcaption{margin:0 0 4px;color:#b91c1c}
    img{display:block;border:1px solid #94a3b8;box-shadow:0 1px 3px #0002}</style>${cells}`;
  const tmp = path.resolve(`.sheet_${process.pid}.html`);
  fs.writeFileSync(tmp, html);
  try {
    const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new' });
    const page = await browser.newPage();
    await page.setViewport({ width, height: 200, deviceScaleFactor: 2 });
    await page.goto('file://' + tmp, { waitUntil: 'load' });
    await page.screenshot({ path: out, fullPage: true });
    await browser.close();
  } finally { fs.rmSync(tmp, { force: true }); }
  console.log('wrote', out);
}
