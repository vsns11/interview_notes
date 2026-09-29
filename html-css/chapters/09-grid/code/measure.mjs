// tools_run: skip  (helper imported by the NN_*.mjs scripts in this folder)
// Opens one of this folder's .html files in real headless Chrome and runs fn(page) —
// so the numbers the scripts print are the browser's own layout results, not guesses.
import puppeteer from 'puppeteer-core';
import path from 'node:path';

export async function withPage(file, fn, width = 900) {
  const browser = await puppeteer.launch({
    executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: 'new', ignoreDefaultArgs: ['--hide-scrollbars'] }); // keep real scrollbars
  try {
    const page = await browser.newPage();
    await page.setViewport({ width, height: 700 });
    await page.goto('file://' + path.resolve(file), { waitUntil: 'load' });
    return await fn(page);
  } finally { await browser.close(); }
}

// Width/height (rounded to 3 decimals) of every element matching sel.
export const boxes = (page, sel) => page.$$eval(sel, els => els.map(el => {
  const r = el.getBoundingClientRect(), r3 = n => Math.round(n * 1000) / 1000;
  return { id: el.id || el.dataset.name || el.className, x: r3(r.x), y: r3(r.y), w: r3(r.width), h: r3(r.height) };
}));
