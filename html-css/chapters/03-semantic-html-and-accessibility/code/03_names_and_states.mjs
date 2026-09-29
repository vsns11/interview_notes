// Accessible names, descriptions and states from aria.html and alt-text.html, as Chrome exposes them.
import { openPage, printTree, fullTree } from './a11y_lib.mjs';

let { browser, page } = await openPage('aria.html');
console.log('--- aria.html');
printTree(await fullTree(page));

await page.click('#toggle');
await page.click('#add');
const after = await page.evaluate(() => ({
  expanded: document.getElementById('toggle').getAttribute('aria-expanded'),
  panelHidden: document.getElementById('panel').hidden,
  status: document.getElementById('live').textContent,
}));
console.log('\nafter clicking "Shipping details" and "Add to cart":', after);
await browser.close();

({ browser, page } = await openPage('alt-text.html'));
console.log('\n--- alt-text.html (images only)');
const snap = await fullTree(page);
(function walk(n, parent) {
  if (n.role === 'image') console.log(`image "${n.name}"` + (parent?.role === 'link' ? `  (inside link "${parent.name}")` : ''));
  for (const c of n.children ?? []) walk(c, n);
})(snap);
console.log('decorative <img alt=""> in tree?', JSON.stringify(snap).includes('"role":"image","name":""'));
await browser.close();
