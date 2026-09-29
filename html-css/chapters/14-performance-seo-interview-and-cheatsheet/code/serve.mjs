// tools_run: skip  (helper imported by the other .mjs scripts in this folder)
// A fake "network" for performance experiments — no server is started. Headless Chrome requests
// http://site.test/<file>; we intercept every request and answer from this folder after an artificial
// delay, so slow CSS / JS / images can be simulated deterministically. Any other URL is blocked.
import puppeteer from 'puppeteer-core';
import fs from 'node:fs';
import path from 'node:path';

const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const TYPES = { '.html': 'text/html', '.css': 'text/css', '.js': 'text/javascript', '.jpg': 'image/jpeg', '.png': 'image/png', '.woff2': 'font/woff2' };
export const ORIGIN = 'http://site.test/';

// delays: { 'file.css': 800, ... } in ms (default 20 ms per request).
// opts: width/height (viewport), cpu (CPU slowdown factor, 1 = none), init (script run before any page script)
export async function withSite(file, delays, fn, { width = 800, height = 600, cpu = 1, init } = {}) {
  // --window-size: make the real (headless) window larger than the emulated viewport. Otherwise content below
  // ~480px is outside the actual window, its paints are never 'presented', and Event Timing (INP) reports nothing.
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new', args: ['--window-size=1200,1400'] });
  try {
    const page = await browser.newPage();
    await page.setViewport({ width, height });
    await page.setCacheEnabled(false);
    if (cpu > 1) await page.emulateCPUThrottling(cpu);
    if (init) await page.evaluateOnNewDocument(init);
    await page.setRequestInterception(true);
    page.on('request', req => {
      const url = req.url();
      if (!url.startsWith(ORIGIN)) return req.abort();
      const rel = decodeURIComponent(new URL(url).pathname.slice(1));
      const abs = path.resolve(rel);
      if (!fs.existsSync(abs)) return req.respond({ status: 404, body: 'not found' });
      setTimeout(() => req.respond({ status: 200, contentType: TYPES[path.extname(abs)] ?? 'application/octet-stream',
        body: fs.readFileSync(abs) }), delays[rel] ?? 20);
    });
    return await fn(page, ORIGIN + file);
  } finally { await browser.close(); }
}
