// tools_run: skip — regenerates every PNG in ../img (~20 s):  node make_figures.mjs
import { shot, sheet, withPage } from './measure.mjs';
import fs from 'node:fs';

const save = async (file, out, width) => { fs.writeFileSync(out, await shot(file, { width, height: 100 })); console.log('wrote', out); };
await save('01_fonts.html', '../img/01_fonts.png', 820);
await save('03_text.html', '../img/03_text.png', 760);
await save('04_contrast.html', '../img/04_contrast.png', 860);
await save('05_backgrounds.html', '../img/05_backgrounds.png', 900);
await save('06_borders_shadows.html', '../img/06_borders_shadows.png', 900);
await save('07_filters_blend.html', '../img/07_filters_blend.png', 900);
await save('08_object_fit.html', '../img/08_object_fit.png', 900);

// font-display: two moments of the same page load, with every font download delayed by 1.5 s
const frames = [];
for (const at of [300, 3500]) {
  const png = await withPage('02_font_display.html', async page => {
    await page.setRequestInterception(true);
    page.on('request', req => req.url().includes('.woff2') ? setTimeout(() => req.continue(), 1500) : req.continue());
    await page.reload({ waitUntil: 'domcontentloaded' });
    await new Promise(r => setTimeout(r, at));
    return page.screenshot({ fullPage: true });
  }, { width: 330, height: 100, dpr: 2 });
  frames.push({ png, label: at < 1000 ? `t = ${at} ms (font still downloading)` : `t = ${at} ms (font arrived at 1.5 s)` });
}
await sheet(frames, '../img/02_font_display.png', { width: 760 });
