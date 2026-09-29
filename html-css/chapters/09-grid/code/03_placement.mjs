// Which grid lines did each item in grid A actually land on? Work it out from the item's rectangle
// and the resolved track sizes, then compare with what the CSS asked for.
import { withPage } from './measure.mjs';

const out = await withPage('03_placement.html', page => page.evaluate(() => {
  const g = document.getElementById('lines'), cs = getComputedStyle(g), gr = g.getBoundingClientRect();
  // positions of every line (start edges) along one axis
  const lines = (prop, gapProp, origin) => {
    const t = cs[prop].split(' ').map(parseFloat), gap = parseFloat(cs[gapProp]), pos = [origin];
    t.forEach((w, i) => pos.push(pos[i] + w + (i < t.length - 1 ? gap : 0)));
    return pos;
  };
  const colL = lines('gridTemplateColumns', 'columnGap', gr.left + 2), rowL = lines('gridTemplateRows', 'rowGap', gr.top + 2);
  const near = (arr, v, gap) => arr.findIndex(p => Math.abs(p - v) < 1 || Math.abs(p - gap - v) < 1) + 1;
  return [...g.querySelectorAll(':scope > div')].map(el => {
    const r = el.getBoundingClientRect();
    return `${el.className}: columns ${near(colL, r.left, 0)} -> ${near(colL, r.right, 8)}, rows ${near(rowL, r.top, 0)} -> ${near(rowL, r.bottom, 8)}`;
  });
}));
console.log(out.join('\n'));
