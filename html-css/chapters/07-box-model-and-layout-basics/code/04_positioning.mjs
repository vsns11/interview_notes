// Where did each positioned box end up? Offsets measured in real Chrome.
import { withPage } from './measure.mjs';

const out = await withPage('04_positioning.html', async page => {
  const r = sel => page.$eval(sel, el => el.getBoundingClientRect().toJSON());
  const slot = await r('.slot'), rel = await r('#rel'), cont = await r('#cont'), abs = await r('#abs');
  const sc = await r('#scroller'), sh = await r('#sh');
  const lines = [
    `relative : moved ${rel.x - slot.x}px right, ${rel.y - slot.y}px down from its normal spot`,
    // the container has a 2px border; absolute offsets are measured from the PADDING box (inside the border)
    `absolute : ${cont.right - 2 - abs.right}px from container's right padding edge, ${abs.y - (cont.y + 2)}px from its top`,
    `sticky   : heading top - scroller top = ${sh.y - (sc.y + 2)}px after scrolling 90px (stays pinned)`,
  ];
  const before = await r('#fix');
  await page.setViewport({ width: 900, height: 300 });               // shrink viewport and scroll the page
  await page.evaluate(() => { document.body.style.height = '2000px'; scrollTo(0, 500); });
  const after = await r('#fix');
  lines.push(`fixed    : before scroll right/bottom gap = ${900 - before.right}/${700 - before.bottom}px, ` +
             `after scrolling 500px = ${900 - after.right}/${300 - after.bottom}px (glued to the viewport)`);
  return lines;
});
console.log(out.join('\n'));
