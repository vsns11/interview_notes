// tools_run: skip — regenerates ../img/02_filmstrip.png (~15 s):  node make_figures.mjs
// Filmstrip of 02_bad.html vs 02_fixed.html loading on the simulated slow network used by 02_vitals.mjs.
// A frame is requested at each time in `times`; the label shows when Chrome actually delivered it
// (a page that hasn't painted yet can't be captured until its first paint).
import { withSite } from './serve.mjs';
import { sheet } from '../../13-modern-css-and-architecture/code/measure.mjs';

const delays = { '02_styles.css': 300, '02_bad.js': 800, '02_fixed.js': 800, 'media/hills-800.jpg': 1800 };
const times = [400, 1000, 2000, 3000];
const frames = [];
for (const file of ['02_bad.html', '02_fixed.html']) {
  await withSite(file, delays, async (page, url) => {
    const t0 = Date.now();
    page.goto(url).catch(() => {});
    for (const t of times) {
      await new Promise(r => setTimeout(r, Math.max(0, t0 + t - Date.now())));
      const png = Buffer.from(await page.screenshot({ clip: { x: 0, y: 0, width: 412, height: 620 } }));
      frames.push({ png, label: `${file.slice(3, -5)}, ${Date.now() - t0} ms`, cssWidth: 206 });   // time the frame was captured
    }
    await new Promise(r => setTimeout(r, 300));
  }, { width: 412, height: 800 });
}
await sheet(frames, '../img/02_filmstrip.png', { width: 960 });
