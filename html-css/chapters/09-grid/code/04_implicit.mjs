// Show where auto-placement put each item, and the resolved explicit + implicit track sizes.
import { withPage } from './measure.mjs';

const out = await withPage('04_implicit.html', page => page.$$eval('.g', gs => gs.map(g => {
  const cs = getComputedStyle(g), gr = g.getBoundingClientRect();
  // grid cell of each item = which column/row start line its top-left corner sits on
  const starts = (list, gap, origin) => { const s = [origin]; list.split(' ').map(parseFloat)
    .forEach((w, i) => s.push(s[i] + w + parseFloat(gap))); return s; };
  const cx = starts(cs.gridTemplateColumns, cs.columnGap, gr.left + 2), ry = starts(cs.gridTemplateRows, cs.rowGap, gr.top + 2);
  const cell = el => { const r = el.getBoundingClientRect();
    return `${el.textContent.split(' ')[0]}@r${ry.findIndex(y => Math.abs(y - r.top) < 1) + 1}c${cx.findIndex(x => Math.abs(x - r.left) < 1) + 1}`; };
  return `${g.id.padEnd(8)} columns: ${cs.gridTemplateColumns.padEnd(42)} rows: ${cs.gridTemplateRows}\n` +
         `         placement: ${[...g.children].map(cell).join(' ')}`;
})));
console.log(out.join('\n'));
