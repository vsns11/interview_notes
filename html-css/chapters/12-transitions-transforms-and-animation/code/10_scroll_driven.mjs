// Scroll to several positions and read the progress bar's scale and the reveal box's opacity.
import { withPage } from './measure.mjs';

await withPage('10_scroll_driven.html', async page => {
  console.log('CSS.supports("animation-timeline: scroll()") =', await page.evaluate(() => CSS.supports('animation-timeline: scroll()')));
  const max = await page.evaluate(() => document.documentElement.scrollHeight - innerHeight);
  for (const y of [0, 135, 186, 270, max]) {        // 186px puts #r2 half-way through its 'entry' range
    const r = await page.evaluate(async y => {
      scrollTo(0, y);
      await new Promise(res => requestAnimationFrame(() => requestAnimationFrame(res)));
      const cs = s => getComputedStyle(document.querySelector(s));
      const el = document.querySelector('#r2'), layoutTop = el.offsetTop - scrollY;   // position without the translate
      return `bar scale ${cs('.progress').scale.padEnd(9)} #r2 top ${String(layoutTop).padStart(4)}px (height ${el.offsetHeight}) opacity ${(+cs('#r2').opacity).toFixed(2)} translate ${cs('#r2').translate}`;
    }, y);
    console.log(`scrollY ${String(y).padStart(3)}px (${String(Math.round(y / max * 100)).padStart(3)}% of ${max}px): ${r}`);
  }
}, { width: 500, height: 400 });
