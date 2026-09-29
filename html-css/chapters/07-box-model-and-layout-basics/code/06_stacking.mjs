// Which element is on top at a given point? document.elementFromPoint answers exactly that.
import { withPage } from './measure.mjs';

const out = await withPage('06_stacking.html', page => page.evaluate(() => {
  const topAt = (id, dx, dy) => {              // point inside element #id, offset from its top-left
    const r = document.getElementById(id).getBoundingClientRect();
    const el = document.elementFromPoint(r.x + dx, r.y + dy);
    return el.id || el.className;
  };
  return [
    `demo 1  where red, green and blue overlap -> ${topAt('b1', 5, 5)}`,   // (95,95) lies inside all three cards
    `demo 2  where child (999) and sibling (2) overlap -> ${topAt('c2', 90, 20)}`,
    `demo 3  inside the yellow child, no isolation -> ${topAt('u1', 10, 8)}`,
    `demo 3  inside the yellow child, isolation: isolate -> ${topAt('u2', 10, 8)}`,
  ];
}));
console.log(out.join('\n'));
