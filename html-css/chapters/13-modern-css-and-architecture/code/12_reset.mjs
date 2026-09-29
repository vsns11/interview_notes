// Measure the browser's defaults, then add 12_reset.css and measure again (viewport 400px wide).
import { withPage } from './measure.mjs';

const measure = page => page.evaluate(() => {
  const $ = id => document.getElementById(id), cs = id => getComputedStyle($(id));
  return {
    'body margin': getComputedStyle(document.body).margin,
    'h1 margin-block': cs('h1').marginTop + ' ' + cs('h1').marginBottom,
    'h1 font-size': cs('h1').fontSize,
    '#box width (200px + padding + border)': $('box').getBoundingClientRect().width + 'px',
    'img display / rendered width': cs('img').display + ' / ' + $('img').getBoundingClientRect().width + 'px',
    'button font': cs('btn').fontSize + ' ' + cs('btn').fontFamily.split(',')[0],
    'ul list-style / padding-left': cs('ul').listStyleType + ' / ' + cs('ul').paddingLeft,
    'p scrollWidth (overflow?)': $('p').scrollWidth + 'px of ' + $('p').clientWidth + 'px',
  };
});

await withPage('12_reset.html', async page => {
  const before = await measure(page);
  await page.addStyleTag({ path: '12_reset.css' });
  const after = await measure(page);
  console.log('property'.padEnd(40), 'browser default'.padEnd(26), 'with reset');
  for (const k of Object.keys(before)) console.log(k.padEnd(40), before[k].padEnd(26), after[k]);
}, { width: 400 });
