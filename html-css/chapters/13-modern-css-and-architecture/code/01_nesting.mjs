// Nesting, proved by the browser: (1) how Chrome's CSSOM expands each nested rule,
// (2) the computed styles that result, at two viewport widths.
import { withPage } from './measure.mjs';

await withPage('01_nesting.html', async page => {
  const rules = await page.evaluate(() => {
    const out = [];
    const walk = (list, depth) => {
      for (const r of list) {
        const head = r.selectorText ?? (r.conditionText ? '@media ' + r.conditionText : r.constructor.name);
        out.push('  '.repeat(depth) + head);
        if (r.cssRules) walk(r.cssRules, depth + 1);
      }
    };
    walk(document.styleSheets[0].cssRules, 0);
    return out.join('\n');
  });
  console.log('CSSOM tree (selectorText of each rule):\n' + rules);

  const kept = await page.evaluate(() => [...document.styleSheets[0].cssRules[1].cssRules].some(r => r.selectorText?.includes('__body')));
  console.log('\nWas the "&__body" rule kept?', kept);
});

for (const width of [500, 700]) {
  await withPage('01_nesting.html', async page => {
    const s = await page.evaluate(() => {
      const cs = (sel, p) => getComputedStyle(document.querySelector(sel))[p];
      return {
        padding: cs('#c1', 'paddingTop'), border: cs('#c1', 'borderTopColor'), bg: cs('#c1', 'backgroundColor'),
        titleSize: cs('#c1 .title', 'fontSize'), bodyColor: cs('#body', 'color'), link: cs('#l1', 'color'),
      };
    });
    console.log(`\nviewport ${width}px: .card padding ${s.padding}, border ${s.border}, background ${s.bg}`);
    console.log(`  .title font-size ${s.titleSize}   .card__body color ${s.bodyColor} (not red)   .sidebar .link color ${s.link}`);
  }, { width });
}
