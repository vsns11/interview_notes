// Shadows and outlines are painted OUTSIDE the box but take no layout space; borders do.
import { withPage } from './measure.mjs';

await withPage('06_borders_shadows.html', page => page.evaluate(() => {
  const out = [];
  for (const sel of ['.styles', '.outline', '.ring', '.elev3']) {
    const el = document.querySelector(sel), cs = getComputedStyle(el);
    out.push(`${sel.padEnd(9)} layout box ${el.offsetWidth}x${el.offsetHeight}  border-left ${cs.borderLeftWidth.padEnd(4)} outline ${(cs.outlineStyle === 'none' ? 'none' : cs.outlineWidth + ' +' + cs.outlineOffset).padEnd(9)}  shadow ${cs.boxShadow.slice(0, 44)}`);
  }
  const e = getComputedStyle(document.querySelector('.ellipse'));
  out.push(`.ellipse  border-top-left-radius: ${e.borderTopLeftRadius}`);
  return out.join('\n');
})).then(console.log);
