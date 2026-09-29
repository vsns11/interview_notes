// Measure the real vertical gaps in 02_margin_collapse.html with getBoundingClientRect.
import { withPage } from './measure.mjs';

await withPage('02_margin_collapse.html', page => page.evaluate(() => {
  const r = id => document.getElementById(id).getBoundingClientRect();
  const gap = (a, b) => r(b).top - r(a).bottom;          // space between two boxes
  const inset = id => { const p = document.getElementById(id);   // child top minus parent top
    return p.firstElementChild.getBoundingClientRect().top - p.getBoundingClientRect().top; };
  return [
    ['A siblings 30 / 20          ', gap('a1', 'a2')],
    ['B child top inside parent   ', inset('b1')],
    ['C same with flow-root parent', inset('c1')],
    ['D across empty block 20/20  ', gap('d1', 'd2')],
    ['E 30 / -10                  ', gap('e1', 'e2')],
    ['F flex column 30 / 20       ', gap('f1', 'f2')],
  ];
})).then(rows => rows.forEach(([k, v]) => console.log(k, '->', v + 'px')));
