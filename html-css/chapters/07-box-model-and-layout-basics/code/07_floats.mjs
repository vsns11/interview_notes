// Heights of the dashed parents: does each one contain its 80px-tall floats?
import { withPage } from './measure.mjs';

const out = await withPage('07_floats.html', page => page.$$eval('.wrap', els => els.map(el =>
  `${el.id}  display: ${getComputedStyle(el).display.padEnd(9)}  parent height ${el.getBoundingClientRect().height}px`)));
console.log(out.join('\n'));
