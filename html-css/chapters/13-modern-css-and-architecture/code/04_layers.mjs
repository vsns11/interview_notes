// Cascade layers, measured: who wins each conflict in 04_layers.html, and the layer names Chrome sees.
import { withPage } from './measure.mjs';

await withPage('04_layers.html', async page => {
  const r = await page.evaluate(() => {
    const cs = id => getComputedStyle(document.getElementById(id));
    const names = [];
    const walk = (rules, prefix) => { for (const x of rules) {
      if (x instanceof CSSLayerStatementRule) names.push('statement: ' + x.nameList.join(', '));
      else if (x instanceof CSSLayerBlockRule) { names.push('block:     ' + prefix + x.name); walk(x.cssRules, prefix + x.name + '.'); }
      else if (x instanceof CSSImportRule) names.push('import:    ' + x.href + ' layer(' + x.layerName + ')');
    } };
    walk(document.styleSheets[0].cssRules, '');
    return {
      b1bg: cs('b1').backgroundColor, b1radius: cs('b1').borderTopLeftRadius, b1pad: cs('b1').paddingLeft,
      a1: cs('a1').color, a2: cs('a2').color, a3: cs('a3').color, p1: cs('p1').color,
      b2radius: cs('b2').borderTopLeftRadius, d1: cs('d1').color, names: [...new Set(names)],
    };
  });
  console.log(r.names.join('\n'));
  console.log(`\n#app .btn.btn-primary (vendor, 1,2,0) vs .btn (components, 0,1,0):`);
  console.log(`   background ${r.b1bg} (#2563eb = rgb(37, 99, 235)), radius ${r.b1radius}, padding-left ${r.b1pad} (vendor's, nobody overrode it)`);
  console.log(`.card a (components)                     color ${r.a1}`);
  console.log(`.text-red (utilities) vs .card a         color ${r.a2}`);
  console.log(`.plain-link { color: revert-layer }      color ${r.a3}  <- back to the components value`);
  console.log(`unlayered p vs base body                 color ${r.p1}`);
  console.log(`!important: utilities 0 vs reset 999px   radius ${r.b2radius}  <- earlier layer wins for !important`);
  console.log(`nested layer components.dialog          color ${r.d1}`);
});
