// :has() measured: which elements match, and how the styles change when the state changes.
import { withPage } from './measure.mjs';

await withPage('03_has.html', async page => {
  const snap = () => page.evaluate(() => {
    const cs = (id, p) => getComputedStyle(document.getElementById(id))[p];
    return {
      withImg: cs('withImg', 'display'), noImg: cs('noImg', 'display'),
      f1: cs('f1', 'borderLeftColor'), f2: cs('f2', 'borderLeftColor'),
      h1: cs('h1', 'marginBottom'), h2: cs('h2', 'marginBottom'),
      short: cs('short', 'columnCount'), long: cs('long', 'columnCount'),
      o1: cs('o1', 'fontWeight'), o2: cs('o2', 'fontWeight'),
      htmlOverflow: getComputedStyle(document.documentElement).overflow,
    };
  });
  const a = await snap();
  console.log(`.card with img display=${a.withImg}, without img display=${a.noImg}`);
  console.log(`invalid field border=${a.f1}, valid field border=${a.f2}`);
  console.log(`h3 followed by .subtitle margin-bottom=${a.h1}, lone h3 margin-bottom=${a.h2}`);
  console.log(`3-item list columns=${a.short}, 5-item list columns=${a.long}`);
  console.log(`Monthly (checked) weight=${a.o1}, Yearly weight=${a.o2}`);
  console.log(`<html> overflow with dialog closed: ${a.htmlOverflow}`);

  // Change state the way a user / script would, then look again
  await page.click('#o2 input');
  await page.evaluate(() => {
    document.querySelector('#f1 input').value = 'fixed@example.com';
    document.querySelector('#short').append(document.createElement('li'));
    document.getElementById('dlg').showModal();
  });
  const b = await snap();
  console.log('\nafter: click "Yearly", fix the email, add a 4th <li>, showModal()');
  console.log(`Monthly weight=${b.o1}, Yearly weight=${b.o2}`);
  console.log(`first field border=${b.f1}`);
  console.log(`first list columns=${b.short}`);
  console.log(`<html> overflow with dialog open: ${b.htmlOverflow}`);

  const errs = await page.evaluate(() => ['a:has(:has(b))', 'p:has(::before)', 'li:has(+ li)'].map(s => {
    try { document.querySelector(s); return s + '  -> valid'; } catch { return s + '  -> SyntaxError'; }
  }));
  console.log('\n' + errs.join('\n'));
});
