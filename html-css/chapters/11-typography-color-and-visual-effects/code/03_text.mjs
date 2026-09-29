// Read back what the browser computed for a few text properties, and prove two claims:
// (1) a unitless line-height is inherited as a FACTOR, a length (em/px) as a fixed pixel value;
// (2) tabular-nums makes every digit the same width.
import { withPage } from './measure.mjs';

await withPage('03_text.html', page => page.evaluate(() => {
  const out = [];
  // (1) parent 16px with line-height 1.5 vs 1.5em; child 32px
  document.body.insertAdjacentHTML('beforeend', `
    <div id="a" style="font-size:16px;line-height:1.5"><span id="a2" style="display:block;font-size:32px">x</span></div>
    <div id="b" style="font-size:16px;line-height:1.5em"><span id="b2" style="display:block;font-size:32px">x</span></div>`);
  const lh = id => getComputedStyle(document.getElementById(id)).lineHeight;
  const h = id => document.getElementById(id).getBoundingClientRect().height;
  out.push(`line-height: 1.5   -> child (32px) line box ${h('a2')}px`);
  out.push(`line-height: 1.5em -> child (32px) computed ${lh('b2')}, line box ${h('b2')}px  (text taller than its line!)`);
  // (2) digit widths
  for (const v of ['proportional-nums', 'tabular-nums']) {
    const s = document.createElement('span'); s.style.cssText = `font: 15px system-ui; font-variant-numeric: ${v}`;
    document.body.append(s);
    const w = ch => { s.textContent = ch; return s.getBoundingClientRect().width.toFixed(2); };
    out.push(`${v.padEnd(17)} width of "1" ${w('1')}px, of "8" ${w('8')}px`);
  }
  const cs = s => getComputedStyle(document.querySelector(s));
  out.push(`.caps letter-spacing computed: ${cs('.caps').letterSpacing} (0.08em of 12px)`);
  out.push(`.balance text-wrap: ${cs('.balance').textWrap || cs('.balance').textWrapStyle}`);
  return out;
})).then(lines => console.log(lines.join('\n')));
