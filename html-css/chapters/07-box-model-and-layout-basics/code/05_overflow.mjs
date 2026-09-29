// For each overflow value: how big is the content vs. the box, and can it be scrolled?
import { withPage } from './measure.mjs';

const rows = await withPage('05_overflow.html', page => page.$$eval('.box', els => els.map(el => {
  el.scrollTop = 20;                                   // try to scroll programmatically
  return `${getComputedStyle(el).overflowY.padEnd(8)} clientHeight ${el.clientHeight}  scrollHeight ${el.scrollHeight}  ` +
         `scrollTop after setting 20 -> ${el.scrollTop}`;
})));
console.log(rows.join('\n'));
