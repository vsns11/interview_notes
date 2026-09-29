// Run each 1-second animation for real and count how often Chrome had to recalculate style and redo LAYOUT.
// (page.metrics() reads Chrome's own counters.) transform/opacity should need almost none.
import { withPage } from './measure.mjs';

for (const anim of ['move-left', 'grow-width', 'move-transform', 'fade']) {
  await withPage('06_performance.html', async page => {
    const before = await page.metrics();
    await page.evaluate(anim => {
      document.body.style.setProperty('--anim', anim);
      document.body.dataset.anim = '';
      return new Promise(r => document.querySelector('.box').addEventListener('animationend', r));
    }, anim);
    const after = await page.metrics();
    const d = k => after[k] - before[k];
    console.log(`${anim.padEnd(15)} layouts ${String(d('LayoutCount')).padStart(3)}   style recalcs ${String(d('RecalcStyleCount')).padStart(3)}   layout time ${(d('LayoutDuration') * 1000).toFixed(1)}ms`);
  });
}
