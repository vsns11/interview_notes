// Three layouts that adapt with no @media at all: measure them at three widths.
import { withPage } from './measure.mjs';

for (const width of [360, 480, 1000]) {
  const r = await withPage('07_intrinsic.html', page => page.evaluate(() => {
    const box = s => document.querySelector(s).getBoundingClientRect();
    const side = box('.side'), content = box('.content');
    return { wrapper: box('.wrapper').width,
             sidebar: side.top === content.top ? `beside content (${side.width.toFixed(1)}px + ${content.width.toFixed(1)}px)` : 'wrapped above content',
             grid: getComputedStyle(document.querySelector('.grid')).gridTemplateColumns.split(' ').length };
  }), { width });
  console.log(`${String(width).padStart(4)}px  wrapper ${r.wrapper.toFixed(1)}px | sidebar ${r.sidebar} | grid ${r.grid} column(s)`);
}
