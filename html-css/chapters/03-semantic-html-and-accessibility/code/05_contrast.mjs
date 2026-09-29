// Check colour pairs against the WCAG 2.2 contrast thresholds.
import { contrast } from './contrast_lib.mjs';

const pairs = [
  ['#000000', '#ffffff', 'black on white'],
  ['#767676', '#ffffff', 'mid grey on white'],
  ['#777777', '#ffffff', 'one step lighter'],
  ['#999999', '#ffffff', 'light grey placeholder'],
  ['#ffffff', '#3b82f6', 'white on blue-500'],
  ['#ffffff', '#1d4ed8', 'white on blue-700'],
  ['#b91c1c', '#ffffff', 'error red on white'],
  ['#ffffff', '#f59e0b', 'white on amber'],
];
console.log('pair                     ratio   normal text   large text / UI');
for (const [fg, bg, label] of pairs) {
  const r = contrast(fg, bg);
  console.log(label.padEnd(24), (r.toFixed(2) + ':1').padEnd(7), (r >= 4.5 ? 'pass AA' : 'FAIL').padEnd(13),
              r >= 3 ? 'pass AA' : 'FAIL', r >= 7 ? '| AAA ok' : '');
}
