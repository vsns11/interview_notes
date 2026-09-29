// image-set(): the browser downloads only ONE candidate, picked by the screen's pixel density.
import { withPage } from './measure.mjs';

for (const dpr of [1, 2, 3]) {
  const loaded = [];
  await withPage('06_image_set.html', async page => {
    // Watch the network by reloading with a request listener attached
    page.on('requestfinished', r => { if (r.url().endsWith('.jpg')) loaded.push(r.url().split('/').pop()); });
    await page.reload({ waitUntil: 'networkidle0' });
  }, { dpr });
  console.log(`devicePixelRatio ${dpr}: downloaded ${loaded.join(', ')}`);
}
