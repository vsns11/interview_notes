// A tiny automated responsive test: at each width, is there a horizontal scrollbar, and who causes it?
// WCAG 2.2 SC 1.4.10 (Reflow) asks that content work at 320 CSS px wide without scrolling sideways.
import { withPage } from './measure.mjs';

const check = page => page.evaluate(() => {
  const vw = document.documentElement.clientWidth;
  const culprits = [...document.body.querySelectorAll('*')]
    .filter(el => el.getBoundingClientRect().right > vw + 0.5 || el.scrollWidth > el.clientWidth + 1 && getComputedStyle(el).overflowX === 'visible')
    .map(el => el.tagName.toLowerCase() + (el.className ? '.' + el.className : ''));
  return { overflow: document.documentElement.scrollWidth > vw, scrollWidth: document.documentElement.scrollWidth, culprits };
});

for (const file of ['02_mobile_first.html', '08_broken.html', '08_fixed.html']) {
  for (const width of [320, 768, 1280]) {
    const r = await withPage(file, check, { width });
    console.log(`${file.padEnd(21)} ${String(width).padStart(4)}px  ${r.overflow ? `OVERFLOW (page is ${r.scrollWidth}px wide): ${r.culprits.join(', ')}` : 'ok'}`);
  }
}
