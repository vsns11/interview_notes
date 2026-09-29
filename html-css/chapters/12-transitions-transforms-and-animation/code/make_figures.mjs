// tools_run: skip — regenerates every PNG in ../img (~30 s):  node make_figures.mjs
// Animation frames are made by PAUSING the animations (Web Animations API) and seeking them to exact
// times, so every frame is deterministic — no screen recording, no timing luck.
import { shot, sheet, withPage } from './measure.mjs';
import { seek, startRace } from './anim.mjs';
import fs from 'node:fs';

const save = async (file, out, width) => { fs.writeFileSync(out, await shot(file, { width, height: 100 })); console.log('wrote', out); };
await save('02_curves.html', '../img/02_curves.png', 740);
await save('03_transforms.html', '../img/03_transforms.png', 820);
await save('04_3d.html', '../img/04_3d.png', 760);

// Filmstrip helper: prepare(page) starts/pauses things, then one screenshot per time
async function filmstrip(file, out, times, { width, height, prepare = () => {}, label = t => `t = ${t} ms`, sheetWidth = 1100, fullPage = true, clip } = {}) {
  const frames = await withPage(file, async page => {
    await prepare(page);
    const pngs = [];
    for (const t of times) { await seek(page, t); pngs.push({ png: await page.screenshot({ fullPage, ...(clip ? { clip } : {}) }), label: label(t) }); }
    return pngs;
  }, { width, height, dpr: 2 });
  await sheet(frames, out, { width: sheetWidth });
}

await filmstrip('01_timing.html', '../img/01_timing_frames.png', [0, 200, 400, 600, 800, 1000],
  { width: 350, height: 100, prepare: startRace, sheetWidth: 1120 });

await filmstrip('05_keyframes.html', '../img/05_keyframes_frames.png', [0, 250, 499, 750, 1000],
  { width: 330, height: 100, sheetWidth: 1760 });

await filmstrip('07_starting_style.html', '../img/07_starting_style_frames.png', [0, 100, 200, 300],
  { width: 460, height: 150, fullPage: false, sheetWidth: 980,
    prepare: page => page.evaluate(() => { a.showPopover(); b.showPopover(); document.getAnimations().forEach(x => x.pause()); }),
    label: t => `opening, t = ${t} ms` });

await filmstrip('09_view_transition.html', '../img/09_view_transition_frames.png', [0, 125, 250, 375, 500],
  { width: 360, height: 220, fullPage: false, sheetWidth: 1900,
    prepare: page => page.evaluate(async () => {
      const t = document.startViewTransition(() => document.querySelector('.items').classList.add('list'));
      await t.ready; document.getAnimations().forEach(x => x.pause());
    }) });

// scroll-driven: screenshots of the viewport at three scroll positions
const scrollFrames = await withPage('10_scroll_driven.html', async page => {
  const out = [];
  for (const y of [0, 186, 540]) {
    await page.evaluate(async y => { scrollTo(0, y); await new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r))); }, y);
    out.push({ png: await page.screenshot(), label: `scrollY = ${y}px` });
  }
  return out;
}, { width: 500, height: 400, dpr: 2 });
await sheet(scrollFrames, '../img/10_scroll_driven.png', { width: 1150, scale: 0.7 });
