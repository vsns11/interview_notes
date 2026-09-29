// object-fit maths for the photo (read its natural size from the browser) in a 150×150 box.
import { withPage } from './measure.mjs';

const rows = await withPage('08_object_fit.html', page => page.$$eval('img', imgs => imgs.map(i => ({
  cls: i.className, box: `${i.width}×${i.height}`, iw: i.naturalWidth, ih: i.naturalHeight,
  fit: getComputedStyle(i).objectFit, pos: getComputedStyle(i).objectPosition }))));
for (const r of rows) console.log(`${r.cls.padEnd(12)} box ${r.box}  natural ${r.iw}×${r.ih}  object-fit ${r.fit.padEnd(10)} object-position ${r.pos}`);

const { iw, ih } = rows[0], [bw, bh] = [150, 150];
const contain = Math.min(bw / iw, bh / ih), cover = Math.max(bw / iw, bh / ih);
const f = n => +n.toFixed(2);
console.log(`\ncontain: scale = min(150/${iw}, 150/${ih}) = ${contain.toFixed(4)} -> drawn ${f(iw * contain)}×${f(ih * contain)} (bars above and below)`);
console.log(`cover:   scale = max(150/${iw}, 150/${ih}) = ${cover.toFixed(4)} -> drawn ${f(iw * cover)}×${f(ih * cover)}, ${f(iw * cover - bw)}px cropped horizontally`);
console.log(`object-position 90% 50% on cover: image x offset = (150 - ${f(iw * cover)}) × 0.9 = ${f((bw - iw * cover) * 0.9)}px`);
console.log(`scale-down: the smaller of none (scale 1) and contain (${contain.toFixed(4)}) -> same as contain here`);
