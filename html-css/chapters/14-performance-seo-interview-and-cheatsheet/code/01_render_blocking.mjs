// Same page, three <head>s. The stylesheet takes 800 ms to arrive and the script 600 ms.
// When does the first paint (FCP) happen, and which requests did Chrome treat as render-blocking?
import { withSite } from './serve.mjs';

const delays = { '01_styles.css': 800, '01_app.js': 600 };
for (const file of ['01_blocking.html', '01_defer.html', '01_optimized.html']) {
  await withSite(file, delays, async (page, url) => {
    await page.goto(url, { waitUntil: 'load' });
    const r = await page.evaluate(async () => {
      const fcp = await new Promise(res => new PerformanceObserver(l => res(l.getEntriesByName('first-contentful-paint')[0]))
        .observe({ type: 'paint', buffered: true }));
      await new Promise(r => setTimeout(r, 1000));               // let late (non-blocking) requests finish too
      const nav = performance.getEntriesByType('navigation')[0];
      const res = performance.getEntriesByType('resource').filter(e => !e.name.endsWith('favicon.ico'))
        .map(e => `${e.name.split('/').pop()} ${e.renderBlockingStatus} (done at ${Math.round(e.responseEnd)} ms)`).sort();
      return { fcp: fcp.startTime, dcl: nav.domContentLoadedEventEnd, load: nav.loadEventEnd, app: document.documentElement.dataset.appLoaded, res };
    });
    console.log(`${file.padEnd(18)} FCP ${String(Math.round(r.fcp)).padStart(4)} ms   DOMContentLoaded ${String(Math.round(r.dcl)).padStart(4)} ms   load ${String(Math.round(r.load)).padStart(4)} ms   app.js ran at ${r.app} ms`);
    console.log(`${''.padEnd(18)} ${r.res.join('  |  ')}`);
  });
}
