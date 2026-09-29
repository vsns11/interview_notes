// :is() / :where() behaviour measured in Chrome, plus specificity from Chapter 6's calculator.
import { withPage } from './measure.mjs';
import { specificity } from '../../06-css-fundamentals/code/specificity_lib.mjs';

await withPage('02_is_where.html', async page => {
  const r = await page.evaluate(() => {
    const cs = id => getComputedStyle(document.getElementById(id));
    return {
      h: cs('h').color, weight: cs('promo').fontWeight, deco: cs('promo').textDecorationLine,
      a: cs('a').color, b: cs('b').color,
      rules: [...document.styleSheets[0].cssRules].map(r => r.selectorText).filter(Boolean),
    };
  });
  console.log('h2 inside article color     ', r.h, '(rebeccapurple = rgb(102, 51, 153))');
  console.log('.promo font-weight          ', r.weight, '  <- forgiving :is() kept the rule');
  console.log('.promo text-decoration-line ', r.deco, '  <- plain list with one bad selector was dropped');
  console.log(':where(.content) a vs a     ', r.a, '(teal = rgb(0, 128, 128))');
  console.log(':is(#nav,.content) b vs .content b.hot', r.b, '(gray = rgb(128, 128, 128))');
  console.log('\nRules the browser kept:', r.rules.join('  |  '));
});

console.log('\nSpecificity (A,B,C):');
for (const s of [':is(article, section, aside) :is(h1, h2, h3)', ':where(.content) a', 'a', ':is(#nav, .content) b', '.content b.hot'])
  console.log('  ' + s.padEnd(46), '(' + specificity(s).join(',') + ')');
