// Sample real pixels: does interpolating in oklch avoid the grey middle of an sRGB blue->yellow gradient?
// And a hard stop really has no blend.
import { withPage } from './measure.mjs';

await withPage('05_backgrounds.html', async page => {
  const png = await page.screenshot({ encoding: 'base64' });
  const px = await page.evaluate(async (png, boxes) => {
    const img = new Image(); img.src = 'data:image/png;base64,' + png; await img.decode();
    const c = Object.assign(document.createElement('canvas'), { width: img.width, height: img.height }).getContext('2d');
    c.drawImage(img, 0, 0);
    return boxes.map(([sel, fx]) => {
      const r = document.querySelector(sel).getBoundingClientRect();
      const [R, G, B] = c.getImageData(Math.round(r.x + r.width * fx), Math.round(r.y + r.height / 2), 1, 1).data;
      return `${sel.padEnd(7)} at ${String(fx * 100).padStart(3)}%: rgb(${R}, ${G}, ${B})`;
    });
  }, png, [['.srgb', 0.5], ['.oklch', 0.5], ['.hard', 0.49], ['.hard', 0.51]]);
  console.log(px.join('\n'));
});
