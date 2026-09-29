// Contrast ratios by the WCAG formula — then the same numbers from colours the BROWSER computed.
import { ratio, hex, grade } from './contrast.mjs';
import { withPage } from './measure.mjs';

const pairs = [['#000000', '#ffffff'], ['#767676', '#ffffff'], ['#777777', '#ffffff'], ['#1d4ed8', '#ffffff'],
               ['#ffffff', '#3b82f6'], ['#e2e8f0', '#0f172a'], ['#fcd34d', '#ffffff']];
for (const [fg, bg] of pairs) {
  const r = ratio(hex(fg), hex(bg));
  console.log(`${fg} on ${bg}: ${r.toFixed(2)}:1  ${grade(r)}`);
}

// color-mix() and relative colours are resolved by the browser; read the result and grade it.
const rows = await withPage('04_contrast.html', page => page.$$eval('.sw', els => els.map(el => {
  const cs = getComputedStyle(el); return [el.dataset.css, cs.color, cs.backgroundColor, el.querySelector('small').textContent];
})));
for (const [css, fg, bg, r] of rows) console.log(`${css.padEnd(44)} -> ${fg} on ${bg}  = ${r}`);
