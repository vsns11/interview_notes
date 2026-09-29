// Same declarations (width:180px; height:40px; padding:10px; margin:8px), different display values.
import { withPage } from './measure.mjs';

const rows = await withPage('03_display.html', page => page.$$eval('.x', els => els.map(el => {
  const r = el.getBoundingClientRect();
  return `${getComputedStyle(el).display.padEnd(13)} rendered ${String(Math.round(r.width)).padStart(3)} x ${String(Math.round(r.height)).padStart(2)}  ` +
         `height of its <p>: ${Math.round(el.parentElement.getBoundingClientRect().height)}`;
})));
console.log(rows.join('\n'));
