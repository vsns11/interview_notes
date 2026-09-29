// Simulate a slow network: every font download takes 1.5 s. What is on screen at 0.3 s and at 3.5 s?
// Chrome's periods: block -> invisible up to 3 s, then fallback; swap -> ~0 s block; optional -> 100 ms, then never swaps.
import { withPage } from './measure.mjs';

async function slowFonts(page, delay = 1500) {
  await page.setRequestInterception(true);
  page.on('request', req => req.url().includes('.woff2')
    ? setTimeout(() => req.continue(), delay) : req.continue());
}
const state = page => page.evaluate(() => [...document.fonts].map(f => `${f.family}=${f.status}`).join('  '));

await withPage('02_font_display.html', async page => {
  await slowFonts(page);
  await page.reload({ waitUntil: 'domcontentloaded' });
  const t0 = Date.now();
  await new Promise(r => setTimeout(r, 300));
  console.log(`t≈${Date.now() - t0}ms  ${await state(page)}`);
  await new Promise(r => setTimeout(r, 3200));
  console.log(`t≈${Date.now() - t0}ms  ${await state(page)}`);
  // Which font is each paragraph actually drawn with? (DevTools protocol: platform fonts used for a node)
  const cdp = await page.createCDPSession();
  await cdp.send('DOM.enable'); await cdp.send('CSS.enable');
  const { root } = await cdp.send('DOM.getDocument');
  for (const sel of ['.block', '.swap', '.optional']) {
    const { nodeId } = await cdp.send('DOM.querySelector', { nodeId: root.nodeId, selector: sel });
    const { fonts } = await cdp.send('CSS.getPlatformFontsForNode', { nodeId });
    console.log(`  ${sel.padEnd(10)} drawn with: ${fonts.map(f => f.familyName + (f.isCustomFont ? ' (web font)' : '')).join(', ')}`);
  }
});
