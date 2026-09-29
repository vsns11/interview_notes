// Blend-mode arithmetic, checked on real pixels: multiply = a*b, screen = 1-(1-a)(1-b), difference = |a-b|.
import { withPage } from './measure.mjs';

await withPage('07_filters_blend.html', async page => {
  // Three test strips: #ef4444 blended over rgb(100,150,235) (a sky blue). Page coordinates = full-page screenshot pixels.
  const r = await page.evaluate(async () => {
    document.body.insertAdjacentHTML('beforeend', `<div id="t" style="position:relative;width:60px;height:20px;background:rgb(100,150,235)">
      <div style="position:absolute;inset:0;background:#ef4444;mix-blend-mode:multiply"></div></div>
      <div id="u" style="position:relative;width:60px;height:20px;background:rgb(100,150,235)">
      <div style="position:absolute;inset:0;background:#ef4444;mix-blend-mode:screen"></div></div>
      <div id="v" style="position:relative;width:60px;height:20px;background:rgb(100,150,235)">
      <div style="position:absolute;inset:0;background:#ef4444;mix-blend-mode:difference"></div></div>`);
    return ['t', 'u', 'v'].map(id => { const b = document.getElementById(id).getBoundingClientRect(); return [b.x + 30, b.y + 10]; });
  });
  const png = await page.screenshot({ encoding: 'base64', fullPage: true });
  const got = await page.evaluate(async (png, pts) => {
    const img = new Image(); img.src = 'data:image/png;base64,' + png; await img.decode();
    const c = Object.assign(document.createElement('canvas'), { width: img.width, height: img.height }).getContext('2d');
    c.drawImage(img, 0, 0);
    return pts.map(([x, y]) => [...c.getImageData(Math.round(x), Math.round(y), 1, 1).data].slice(0, 3));
  }, png, r);
  const a = [239, 68, 68], b = [100, 150, 235];
  const f = { multiply: (x, y) => x * y / 255, screen: (x, y) => 255 - (255 - x) * (255 - y) / 255, difference: (x, y) => Math.abs(x - y) };
  Object.entries(f).forEach(([mode, fn], i) =>
    console.log(`${mode.padEnd(10)} by hand rgb(${a.map((x, k) => Math.round(fn(x, b[k]))).join(', ')})  browser rgb(${got[i].join(', ')})`));
});
