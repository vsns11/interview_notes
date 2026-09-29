// Theme matrix: OS preference (emulated) x the page's theme setting -> the colours Chrome computes.
import { withPage } from './measure.mjs';

console.log('OS pref  setting   page bg              text                 accent (link)        green-brand link');
for (const os of ['light', 'dark']) for (const setting of ['system', 'light', 'dark']) {
  await withPage('13_theming.html', async page => {
    await page.select('#theme', setting);                 // fires "change" like a user would
    const r = await page.evaluate(() => {
      const c = (id, p) => getComputedStyle(document.getElementById(id))[p];
      return [getComputedStyle(document.body).backgroundColor, c('card', 'color'), c('link', 'color'), c('link2', 'color'),
              localStorage.getItem('theme')];
    });
    console.log(`${os.padEnd(8)} ${setting.padEnd(9)} ${r.slice(0, 4).map(x => x.padEnd(20)).join(' ')} saved=${r[4]}`);
  }, { media: [{ name: 'prefers-color-scheme', value: os }] });
}

// Reload after choosing "dark" on a light OS: the inline <head> script restores the choice before first paint.
await withPage('13_theming.html', async page => {
  await page.select('#theme', 'dark');
  await page.reload({ waitUntil: 'load' });
  const r = await page.evaluate(() => [document.documentElement.dataset.theme, getComputedStyle(document.body).backgroundColor, document.getElementById('theme').value]);
  console.log(`\nafter reload: data-theme=${r[0]}  body bg=${r[1]}  <select> shows "${r[2]}"`);
});
