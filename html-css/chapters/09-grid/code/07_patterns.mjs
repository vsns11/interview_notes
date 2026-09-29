// The same page at desktop and phone widths: resolved columns of both grids.
import { withPage } from './measure.mjs';

for (const width of [900, 400]) {
  await withPage('07_patterns.html', page => page.evaluate(() => {
    const cols = sel => getComputedStyle(document.querySelector(sel)).gridTemplateColumns;
    return [`holy grail: ${cols('.page')}`, `cards     : ${cols('.cards')}`];
  }), width).then(lines => console.log(`viewport ${width}px\n  ` + lines.join('\n  ')));
}
