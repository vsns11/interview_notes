// The native-CSS replacements for Sass features, evaluated by Chrome.
import { withPage } from './measure.mjs';

await withPage('11_native.html', async page => {
  const cs = (id, p) => page.$eval('#' + id, (el, p) => getComputedStyle(el)[p], p);
  console.log('padding: calc(24px / 2)                 ->', await cs('card', 'paddingTop'));
  console.log('border-color: oklch(from --brand l-0.1) ->', await cs('card', 'borderTopColor'));
  console.log('.button background (var(--brand))        ->', await cs('b1', 'backgroundColor'));
  console.log('same rule under .theme-green             ->', await cs('b2', 'backgroundColor'));
  await page.hover('#b1');
  console.log('hover: color-mix(in oklch, brand, black 15%) ->', await cs('b1', 'backgroundColor'));
});
