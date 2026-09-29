// Do the paragraphs and links line up across the three cards? Compare their top edges.
import { withPage } from './measure.mjs';

const out = await withPage('06_subgrid.html', page => page.evaluate(() => ['plain', 'sub'].map(id => {
  const tops = sel => [...document.querySelectorAll(`#${id} ${sel}`)].map(e => Math.round(e.getBoundingClientRect().top * 10) / 10);
  return `${id.padEnd(6)} <p> tops: ${tops('p').join(', ').padEnd(20)} link tops: ${tops('a').join(', ')}`;
})));
console.log(out.join('\n'));
