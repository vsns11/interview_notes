// @scope measured in Chrome: donut holes, proximity, and prelude-less inline scopes.
import { withPage } from './measure.mjs';

await withPage('05_scope.html', async page => {
  const r = await page.evaluate(() => {
    const cs = id => getComputedStyle(document.getElementById(id));
    return {
      supports: CSS.supports('selector(:scope)') && typeof CSSScopeRule === 'function',
      cardBg: cs('card').backgroundColor, cardImg: cs('cardImg').borderTopWidth, contentImg: cs('contentImg').borderTopWidth,
      cardP: cs('cardP').color + ' ' + cs('cardP').fontWeight, contentP: cs('contentP').color + ' ' + cs('contentP').fontWeight,
      a1: cs('a1').color, a2: cs('a2').color, a3: cs('a3').color,
      inB: cs('inB').color, outB: cs('outB').color,
      scopes: [...document.styleSheets[0].cssRules].filter(x => x instanceof CSSScopeRule).map(x => `start=${x.start} end=${x.end}`),
    };
  });
  console.log('CSSScopeRule supported:', r.supports, '\n' + r.scopes.join('\n'));
  console.log(`\n.card background ${r.cardBg}`);
  console.log(`card's own img border ${r.cardImg}   img inside .content border ${r.contentImg}`);
  console.log(`card's own p  ${r.cardP}`);
  console.log(`p in .content ${r.contentP}`);
  console.log(`\nlinks: light ${r.a1}   dark ${r.a2}   light-inside-dark ${r.a3}`);
  console.log(`(amber = rgb(251, 191, 36), blue = rgb(29, 78, 216))`);
  console.log(`\ninline @scope: <b> inside ${r.inB}, <b> outside ${r.outB}`);
});
