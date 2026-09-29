// getComputedStyle(grid).gridTemplateColumns returns the RESOLVED track sizes in px — the browser's answer.
import { withPage } from './measure.mjs';

const rows = await withPage('01_tracks.html', page => page.$$eval('.g', gs => gs.map(g => {
  // read the declared value from the stylesheet rule, and the resolved one from getComputedStyle
  const rule = [...document.styleSheets[0].cssRules].find(r => r.selectorText === '#' + g.id);
  return `${g.id}  ${rule.style.gridTemplateColumns.padEnd(52)} -> ${getComputedStyle(g).gridTemplateColumns}`;
})));
console.log('container content width: 700px\n' + rows.join('\n'));

// The fr maths by hand for t1: free space = 700 - 200 (fixed) - 2 × 20 (column gaps) = 460, split into 3 fr.
const fr = (700 - 200 - 2 * 20) / 3;
console.log(`\nby hand, t1: 1fr = 460 / 3 = ${fr.toFixed(3)}px, 2fr = ${(2 * fr).toFixed(3)}px ` +
            `(Chrome stores 1/64 px: ${Math.round(fr * 64) / 64}, ${Math.round(2 * fr * 64) / 64})`);
