// How many items fit per line, and how wide are they? Measured at two container widths.
import { withPage } from './measure.mjs';

await withPage('05_wrap_gap.html', async page => {
  for (const width of [640, 400]) {
    const lines = await page.evaluate(w => {
      const fc = document.getElementById('cards'); fc.style.width = w + 'px';
      const rows = new Map();                             // group items by their top edge = their line
      for (const el of fc.children) {
        const r = el.getBoundingClientRect();
        rows.set(r.top, [...(rows.get(r.top) ?? []), Math.round(r.width * 100) / 100]);
      }
      return [...rows.values()];
    }, width);
    console.log(`container ${width}px (flex: 1 1 180px, gap 12px):`);
    lines.forEach((l, i) => console.log(`  line ${i + 1}: ${l.length} item(s), widths ${l.join(', ')}`));
  }
  const squeezed = await page.$$eval('.shrinky > div', els => els.map(e => e.getBoundingClientRect().width));
  console.log(`nowrap + flex: 0 1 150px in 640px: ${squeezed.join(', ')}`);
});
