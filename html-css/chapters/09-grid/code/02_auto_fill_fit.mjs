// Resolved track lists for auto-fill vs auto-fit, at two container widths.
import { withPage } from './measure.mjs';

await withPage('02_auto_fill_fit.html', async page => {
  for (const width of [700, 400]) {
    console.log(`container ${width}px, gap 10px, minmax(120px, 1fr):`);
    const rows = await page.evaluate(w => [...document.querySelectorAll('.g')].map(g => {
      g.style.width = w + 'px';
      return `  ${g.id.padEnd(6)} ${getComputedStyle(g).gridTemplateColumns}`;
    }), width);
    console.log(rows.join('\n'));
  }
});

// By hand: how many 120px tracks fit? n = floor((width + gap) / (min + gap)); each = (width - (n-1)·gap) / n
for (const w of [700, 400]) {
  const n = Math.floor((w + 10) / (120 + 10));
  console.log(`by hand, ${w}px: n = floor(${w + 10} / 130) = ${n}, track = (${w} - ${(n - 1) * 10}) / ${n} = ${((w - (n - 1) * 10) / n).toFixed(3)}px`);
}
console.log(`by hand, auto-fit with 3 items at 700px: empty tracks and their gaps collapse -> (700 - 20) / 3 = ${(680 / 3).toFixed(3)}px`);
