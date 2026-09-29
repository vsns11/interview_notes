// Transforms change where a box is PAINTED, not its layout box. And their order matters.
import { withPage } from './measure.mjs';

await withPage('03_transforms.html', page => page.evaluate(() => {
  const out = [];
  const r1 = n => Math.round(n * 10) / 10;
  for (const el of document.querySelectorAll('.box')) {
    const cs = getComputedStyle(el), r = el.getBoundingClientRect(), stage = el.parentElement.getBoundingClientRect();
    out.push(`${el.textContent} ${el.className.split(' ')[1]}: layout ${el.offsetLeft},${el.offsetTop} ${el.offsetWidth}x${el.offsetHeight}` +
             ` | painted bbox ${r1(r.x - stage.x - 1)},${r1(r.y - stage.y - 1)} ${r1(r.width)}x${r1(r.height)} | transform ${cs.transform}`);
  }
  const h = getComputedStyle(document.querySelector('.t8'));
  out.push(`H individual properties: translate ${h.translate}, rotate ${h.rotate}, scale ${h.scale} (transform stays ${h.transform})`);
  return out.join('\n');
})).then(console.log);
