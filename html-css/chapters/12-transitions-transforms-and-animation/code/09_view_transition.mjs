// Start the view transition, freeze it, and inspect the pseudo-element tree the browser built.
import { withPage } from './measure.mjs';

await withPage('09_view_transition.html', async page => {
  const anims = await page.evaluate(async () => {
    const t = document.startViewTransition(() => document.querySelector('.items').classList.add('list'));
    await t.ready;                                                  // pseudo-elements exist, animations created
    const all = document.getAnimations();
    all.forEach(a => a.pause());
    return all.map(a => `${a.effect.pseudoElement} ${a.animationName ?? ''} ${a.effect.getTiming().duration}ms`).sort();
  });
  console.log(`${anims.length} animations while the transition runs:`);
  const shown = anims.filter(a => a.includes('item-1') || a.includes('root'));
  console.log('  ' + shown.join('\n  ') + `\n  … and the same for item-2 and item-3`);

  for (const t of [0, 250, 500]) {
    const pos = await page.evaluate(t => {
      document.getAnimations().forEach(a => { a.currentTime = t; });
      const g = getComputedStyle(document.documentElement, '::view-transition-group(item-2)');
      return `width ${parseFloat(g.width).toFixed(1)}px, height ${parseFloat(g.height).toFixed(1)}px, transform ${g.transform}`;
    }, t);
    console.log(`t=${String(t).padStart(3)}ms ::view-transition-group(item-2): ${pos}`);
  }
});
