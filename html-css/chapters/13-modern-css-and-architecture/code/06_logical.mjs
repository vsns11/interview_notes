// Logical -> physical mapping, measured: the same CSS in ltr, rtl and vertical-rl.
import { withPage } from './measure.mjs';

await withPage('06_logical.html', async page => {
  const rows = await page.evaluate(() => ['ltr', 'rtl', 'vrl'].map(id => {
    const el = document.getElementById(id), s = getComputedStyle(el);
    const badge = el.querySelector('.badge').getBoundingClientRect(), box = el.getBoundingClientRect();
    const side = px => ['Top', 'Right', 'Bottom', 'Left'].filter(k => s['padding' + k] === px).join(',');
    return { id, width: Math.round(box.width), height: Math.round(box.height),
      padStart: side('36px'), padEnd: side('48px'), border: ['Top','Right','Bottom','Left'].filter(k => s['border' + k + 'Width'] === '6px').join(''),
      badge: (badge.left - box.left < box.right - badge.right ? 'left' : 'right') + '/' + (badge.top - box.top < box.bottom - badge.bottom ? 'top' : 'bottom'),
      marginEnd: ['Top','Right','Bottom','Left'].filter(k => s['margin' + k] === '12px').join('') };
  }));
  console.log('box  size(px)   36px padding  48px padding  6px border  12px margin  badge corner');
  for (const r of rows) console.log(`${r.id.padEnd(4)} ${(r.width + 'x' + r.height).padEnd(10)} ${r.padStart.padEnd(13)} ${r.padEnd.padEnd(13)} ${r.border.padEnd(11)} ${r.marginEnd.padEnd(12)} ${r.badge}`);
});
