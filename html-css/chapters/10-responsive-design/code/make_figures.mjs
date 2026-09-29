// tools_run: skip — regenerates every PNG in ../img (takes ~30 s):  node make_figures.mjs
import { shot, sheet } from './measure.mjs';

const at = (file, width, extra = {}) => shot(file, { width, height: 120, ...extra });

// 10.2 the viewport meta tag, both pages on an emulated 400px phone (what the user sees, not full page)
await sheet([
  { png: await at('01_no_viewport.html', 400, { mobile: true, fullPage: false, height: 260 }), label: 'no viewport meta: laid out at 980px, shrunk' },
  { png: await at('01_viewport.html', 400, { mobile: true, fullPage: false, height: 260 }), label: 'width=device-width, initial-scale=1' },
], '../img/01_viewport.png', { width: 860 });

// 10.4 one mobile-first page at three widths
const widths = [400, 800, 1200];
const mf = [];
for (const w of widths) mf.push({ png: await at('02_mobile_first.html', w), label: `viewport ${w}px`, cssWidth: w * 0.5 });
await sheet(mf, '../img/02_mobile_first.png', { width: 1260 });

// 10.5 user preferences: light vs dark
await sheet([
  { png: await at('03_preferences.html', 420), label: 'prefers-color-scheme: light' },
  { png: await at('03_preferences.html', 420, { media: [{ name: 'prefers-color-scheme', value: 'dark' }] }), label: 'prefers-color-scheme: dark' },
], '../img/03_preferences.png', { width: 900 });

// 10.6 container queries at two viewport widths
await sheet([
  { png: await at('04_container_queries.html', 1000), label: 'viewport 1000px', cssWidth: 800 },
  { png: await at('04_container_queries.html', 700), label: 'viewport 700px', cssWidth: 560 },
], '../img/04_container_queries.png', { width: 840 });

// 10.7 fluid type at three widths
const ft = [];
for (const w of [320, 760, 1200]) ft.push({ png: await at('05_fluid_type.html', w), label: `viewport ${w}px`, cssWidth: w * 0.55 });
await sheet(ft, '../img/05_fluid_type.png', { width: 1360 });

// 10.9 intrinsic layouts
await sheet([
  { png: await at('07_intrinsic.html', 360), label: 'viewport 360px' },
  { png: await at('07_intrinsic.html', 1000), label: 'viewport 1000px', cssWidth: 600 },
], '../img/07_intrinsic.png', { width: 1020 });

// 10.10 overflow bugs at 320px
await sheet([
  { png: await at('08_broken.html', 320), label: '08_broken.html at 320px' },
  { png: await at('08_fixed.html', 320), label: '08_fixed.html at 320px' },
], '../img/08_overflow.png', { width: 1000 });
