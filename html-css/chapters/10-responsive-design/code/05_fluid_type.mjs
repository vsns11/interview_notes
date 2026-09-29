// clamp(MIN, PREFERRED, MAX): the browser's font sizes vs. the straight-line formula, at many widths.
import { withPage } from './measure.mjs';

// Our formula for body text: 16px at 320px viewport, 24px at 1200px.
const slope = (24 - 16) / (1200 - 320);                  // px of font per px of viewport
const intercept = 16 - slope * 320;                       // font size at a 0px viewport
console.log(`slope = ${(slope * 100).toFixed(3)}vw, intercept = ${intercept.toFixed(3)}px = ${(intercept / 16).toFixed(3)}rem`);
const byHand = w => Math.min(24, Math.max(16, intercept + slope * w));

for (const width of [280, 320, 600, 900, 1200, 1600]) {
  const [h1, p] = await withPage('05_fluid_type.html', page => page.evaluate(() =>
    ['h1', 'p'].map(s => parseFloat(getComputedStyle(document.querySelector(s)).fontSize))), { width });
  console.log(`${String(width).padStart(5)}px  p ${p.toFixed(2).padStart(5)}px (by hand ${byHand(width).toFixed(2)})   h1 ${h1.toFixed(2)}px`);
}

// Why the rem part matters: a user who set their browser's default text size to 20px.
for (const [label, css] of [['rem + vw (ours)', 'clamp(1rem, 0.818rem + 0.909vw, 1.5rem)'], ['vw only (bad)', '1.6vw']]) {
  const size = await withPage('05_fluid_type.html', async page => {
    const cdp = await page.createCDPSession();
    await cdp.send('Page.setFontSizes', { fontSizes: { standard: 20 } });   // browser setting "font size: 20"
    return page.evaluate(css => { const p = document.querySelector('p'); p.style.fontSize = css;
      return getComputedStyle(p).fontSize; }, css);
  }, { width: 1000 });
  console.log(`default font 20px, viewport 1000px, ${label.padEnd(16)} -> ${size}`);
}
