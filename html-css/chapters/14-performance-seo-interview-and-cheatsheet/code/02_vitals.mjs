// Measure LCP, CLS and INP for 02_bad.html vs 02_fixed.html on a simulated network:
// HTML fast, CSS 300 ms, JS 800 ms, hero image 1800 ms (a phone on a slow connection). Then click the button.
import { withSite } from './serve.mjs';

const delays = { '02_styles.css': 300, '02_bad.js': 800, '02_fixed.js': 800, 'media/hills-800.jpg': 1800 };

// Installed before any page script: collect the raw entries with PerformanceObserver (what the web-vitals library does).
const init = () => {
  const v = (window.__v = { lcp: [], shifts: [], events: [] });
  new PerformanceObserver(l => l.getEntries().forEach(e => v.lcp.push({ t: e.startTime, el: e.element?.tagName ?? '?', size: e.size })))
    .observe({ type: 'largest-contentful-paint', buffered: true });
  new PerformanceObserver(l => l.getEntries().forEach(e => v.shifts.push({ t: e.startTime, value: e.value, input: e.hadRecentInput })))
    .observe({ type: 'layout-shift', buffered: true });
  new PerformanceObserver(l => l.getEntries().forEach(e => e.interactionId && v.events.push({ name: e.name, id: e.interactionId, d: e.duration })))
    .observe({ type: 'event', durationThreshold: 16, buffered: true });
};

// CLS = the largest "session window" sum: shifts < 1 s apart, window at most 5 s long, ignoring shifts right after input.
function cls(shifts) {
  let best = 0, cur = 0, start = 0, prev = -Infinity;
  for (const s of shifts.filter(s => !s.input)) {
    if (s.t - prev > 1000 || s.t - start > 5000) { cur = 0; start = s.t; }
    cur += s.value; prev = s.t; best = Math.max(best, cur);
  }
  return best;
}
const rate = (x, good, poor) => x <= good ? 'good' : x <= poor ? 'needs improvement' : 'poor';

for (const file of ['02_bad.html', '02_fixed.html']) {
  await withSite(file, delays, async (page, url) => {
    await page.goto(url, { waitUntil: 'load' });
    await new Promise(r => setTimeout(r, 3500));             // let the late image and banner arrive (input stops LCP)
    await page.click('#save');                               // a real click: pointerdown, pointerup, click
    await new Promise(r => setTimeout(r, 800));
    const v = await page.evaluate(() => window.__v);
    const lcp = v.lcp.at(-1), c = cls(v.shifts);
    const byInteraction = {};                                // INP: per interaction the longest event; then the worst interaction
    for (const e of v.events) byInteraction[e.id] = Math.max(byInteraction[e.id] ?? 0, e.d);
    const inp = Math.max(0, ...Object.values(byInteraction));   // 0 here = no event reached the 16 ms recording threshold
    console.log(`${file}`);
    console.log(`  LCP ${Math.round(lcp.t)} ms  (element <${lcp.el.toLowerCase()}>, candidates: ${v.lcp.map(e => `<${e.el.toLowerCase()}> @${Math.round(e.t)}`).join(', ')})  -> ${rate(lcp.t, 2500, 4000)}`);
    console.log(`  CLS ${c.toFixed(3)}  (shifts: ${v.shifts.map(s => `${s.value.toFixed(3)} @${Math.round(s.t)}ms`).join(', ') || 'none'})  -> ${rate(c, 0.1, 0.25)}`);
    console.log(`  INP ${inp ? Math.round(inp) + ' ms' : '< 16 ms'}  (slow events in the click: ${[...new Set(v.events.map(e => e.name))].join(', ') || 'none'})  -> ${rate(inp, 200, 500)}`);
  }, { init, width: 412, height: 800 });
}
