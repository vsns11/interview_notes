// Which media queries match at 400 / 800 / 1200px, and what the grid resolves to.
// Then: resize ONE page and watch matchMedia's change events fire (how JS reacts to breakpoints).
import { withPage } from './measure.mjs';

for (const width of [400, 800, 1200]) {
  await withPage('02_mobile_first.html', page => page.evaluate(() => {
    const q = s => (matchMedia(s).matches ? 'yes' : 'no ');
    const cols = getComputedStyle(document.querySelector('.cards')).gridTemplateColumns;
    return `(width >= 40em) ${q('(width >= 40em)')}  (width >= 64em) ${q('(width >= 64em)')}  cards: ${cols}`;
  }), { width }).then(line => console.log(`${String(width).padStart(4)}px  ${line}`));
}

await withPage('02_mobile_first.html', async page => {
  await page.evaluate(() => {
    window.log = [];
    const mq = matchMedia('(width >= 64em)');
    mq.addEventListener('change', e => log.push(`change -> ${e.matches ? 'desktop' : 'not desktop'} at ${innerWidth}px`));
  });
  for (const width of [900, 1100, 1300, 700]) {
    await page.setViewport({ width, height: 700 });
    await page.evaluate(() => new Promise(r => requestAnimationFrame(() => setTimeout(r)))); // let the event fire
  }
  console.log('matchMedia events:', await page.evaluate(() => log));
}, { width: 400 });
