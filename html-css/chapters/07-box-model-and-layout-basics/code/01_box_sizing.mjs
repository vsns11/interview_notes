// Ask real Chrome how wide the two demo boxes in 01_box_model.html are.
// Both have: width: 200px; padding: 20px; border: 6px — only box-sizing differs.
import { withPage } from './measure.mjs';

const rows = await withPage('01_box_model.html', page => page.$$eval('.demo', els => els.map(el => {
  const cs = getComputedStyle(el);
  const content = el.clientWidth - 2 * parseFloat(cs.paddingLeft);   // clientWidth = content + padding
  return `${cs.boxSizing.padEnd(11)}  width property ${cs.width}  content ${content}px  ` +
         `on screen (border box) ${el.getBoundingClientRect().width}px`;
})));
console.log(rows.join('\n'));
