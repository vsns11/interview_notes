// Check the flex math against real Chrome: lay out 02_grow_shrink.html, read every item's width with
// getBoundingClientRect(), and compare it with what flex_algorithm.mjs predicts.
import { withPage } from './measure.mjs';
import { resolveFlex, round2 } from './flex_algorithm.mjs';

const page = await withPage('02_grow_shrink.html', p => p.evaluate(() => {
  // intrinsic width of an item's content: a hidden, absolutely positioned clone with width: min-content / max-content
  const intrinsic = (el, kind) => {
    const c = el.cloneNode(true);
    c.style.cssText = `position:absolute; visibility:hidden; flex:none; width:${kind}`;
    el.parentElement.append(c);   // same parent, so the same CSS applies; abspos = no effect on layout
    const w = c.getBoundingClientRect().width; c.remove(); return w;
  };
  const out = {};
  for (const fc of document.querySelectorAll('.fc')) {
    const items = [...fc.children].filter(el => el.tagName === 'DIV');
    out[fc.id] = items.map(el => ({ w: el.getBoundingClientRect().width,
      min: intrinsic(el, 'min-content'), max: intrinsic(el, 'max-content') }));
  }
  return out;
}));

const f = (grow, shrink, basis, extra = {}) => ({ grow, shrink, basis, ...extra });
const names = arr => arr.map((x, i) => ({ name: 'ABC'[i], ...x }));
const m = page;                                          // measured data per container id
const cases = [
  ['g1', 600, names([f(1, 1, 100), f(2, 1, 150), f(1, 1, 50)]), {}],
  ['g2', 400, names([f(0, 1, 300, { min: m.g2[0].min }), f(0, 1, 200, { min: m.g2[1].min }), f(0, 2, 100)]), {}],
  ['g3', 600, names([f(1, 1, 100), f(2, 1, 150), f(1, 1, 50)]), { gap: 20 }],
  ['g4', 600, names([f(1, 1, 0, { max: 100 }), f(1, 1, 0), f(1, 1, 0)]), {}],
  ['g5a', 600, names(m.g5a.map(x => f(1, 1, 0, { min: x.min }))), {}],               // flex: 1 -> basis 0
  ['g5b', 600, names(m.g5b.map(x => f(1, 1, x.max, { min: x.min }))), {}],           // flex: auto -> basis = content
  ['g6a', 300, names(m.g6a.map(x => f(1, 1, 0, { min: x.min }))), {}],               // min-width: auto = min-content
  ['g6b', 300, names(m.g6b.map(() => f(1, 1, 0))), {}],                              // min-width: 0
  ['g7', 600, names([f(0.25, 1, 100), f(0.25, 1, 100)]), {}],
];
console.log('case   predicted (spec algorithm)            measured in Chrome                   diff');
for (const [id, container, items, opts] of cases) {
  const pred = Object.values(resolveFlex(container, items, opts)).map(round2);
  const meas = m[id].map(x => round2(x.w));
  const diff = Math.max(...pred.map((p, i) => Math.abs(p - meas[i])));
  console.log(id.padEnd(6), pred.join(' / ').padEnd(37), meas.join(' / ').padEnd(36), round2(diff));
}
console.log('\ncontent sizes used above (min-content / max-content):');
for (const id of ['g5b', 'g6a']) console.log(' ', id, m[id].map(x => `${round2(x.min)} / ${round2(x.max)}`).join('  |  '));
