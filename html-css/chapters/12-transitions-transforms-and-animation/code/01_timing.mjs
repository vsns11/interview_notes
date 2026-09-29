// Prove what each timing function does: pause the transitions at 0, 200, … 1000 ms and read the
// ball's computed translate — then compare with our own cubic-bezier solver (the maths the browser does).
import { withPage } from './measure.mjs';
import { seek, startRace } from './anim.mjs';

// cubic-bezier(x1, y1, x2, y2): a curve from (0,0) to (1,1). x = time fraction, y = progress.
// Given time t, find the curve parameter s with x(s) = t (bisection), then return y(s).
function cubicBezier(x1, y1, x2, y2) {
  const bez = (a, b, s) => 3 * a * s * (1 - s) ** 2 + 3 * b * s * s * (1 - s) + s ** 3;
  return t => { let lo = 0, hi = 1, s = t;
    for (let i = 0; i < 60; i++) { s = (lo + hi) / 2; if (bez(x1, x2, s) < t) lo = s; else hi = s; }
    return bez(y1, y2, s); };
}
const model = {
  linear: t => t,
  ease: cubicBezier(0.25, 0.1, 0.25, 1),
  'ease-in': cubicBezier(0.42, 0, 1, 1),
  'ease-out': cubicBezier(0, 0, 0.58, 1),
  'ease-in-out': cubicBezier(0.42, 0, 0.58, 1),
  'back-out': cubicBezier(0.34, 1.56, 0.64, 1),
  'steps(4)': t => Math.floor(t * 4) / 4,          // jump-end (the default): jumps at the END of each step
  // linear(0, 0.36 30%, 1 45%, …): straight lines between the listed (time, progress) points
  'linear(bounce)': t => {
    const pts = [[0, 0], [0.3, 0.36], [0.45, 1], [0.55, 0.8], [0.65, 1], [0.75, 0.95], [1, 1]];
    for (let i = 1; i < pts.length; i++) { const [t0, p0] = pts[i - 1], [t1, p1] = pts[i];
      if (t <= t1) return p0 + (p1 - p0) * (t - t0) / (t1 - t0); }
    return 1; },
};

const times = [0, 200, 400, 600, 800, 1000];
const table = await withPage('01_timing.html', async page => {
  await startRace(page);
  const rows = {};
  for (const t of times) {
    await seek(page, t);
    const vals = await page.$$eval('.lane', lanes => lanes.map(l => [l.querySelector('span').textContent,
      parseFloat(getComputedStyle(l.querySelector('.ball')).translate) || 0]));
    for (const [name, px] of vals) (rows[name] ??= []).push(px / 160);   // progress = distance / 160px
  }
  return rows;
}, { width: 420 });

console.log('progress (0 = start, 1 = end) at t =   ' + times.map(t => `${t}ms`.padStart(8)).join(''));
for (const [name, got] of Object.entries(table)) {
  console.log(`${name.padEnd(15)} browser    ` + got.map(v => v.toFixed(3).padStart(8)).join(''));
  if (model[name]) console.log(`${''.padEnd(15)} our maths  ` + times.map(t => model[name](t / 1000).toFixed(3).padStart(8)).join(''));
}
