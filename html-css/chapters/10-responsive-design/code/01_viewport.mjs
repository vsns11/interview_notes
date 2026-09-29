// The same page with and without <meta name="viewport">, opened as a 400px-wide phone.
import { withPage } from './measure.mjs';

for (const file of ['01_no_viewport.html', '01_viewport.html']) {
  const r = await withPage(file, page => page.evaluate(() => ({
    layout: document.documentElement.clientWidth,               // width CSS is laid out in
    visual: Math.round(visualViewport.width),                   // CSS px actually visible on screen
    scale: Math.round(visualViewport.scale * 1000) / 1000,      // zoom the browser applied
    font: getComputedStyle(document.querySelector('.bar')).fontSize,
  })), { width: 400, mobile: true });
  console.log(`${file.padEnd(20)} layout ${r.layout}px, visible ${r.visual}px, scale ${r.scale}, font-size ${r.font}`);
}
