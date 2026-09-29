// Open both popovers, then close them, and look at what is running and what is displayed.
import { withPage } from './measure.mjs';

await withPage('07_starting_style.html', async page => {
  const state = () => page.evaluate(() => ['a', 'b'].map(id => {
    const el = document.getElementById(id), cs = getComputedStyle(el);
    const running = el.getAnimations().map(a => a.transitionProperty).join('+') || 'none';
    return `${id.toUpperCase()}: display ${cs.display.padEnd(5)} opacity ${(+cs.opacity).toFixed(2)}  transitions running: ${running}`;
  }).join('   |   '));
  // Freeze any transitions 100ms in (deterministic), or jump them to the end.
  const at100 = () => page.evaluate(() => document.getAnimations().forEach(x => { x.pause(); x.currentTime = 100; }));
  const finish = () => page.evaluate(() => document.getAnimations().forEach(x => x.finish()));

  await page.evaluate(() => { a.showPopover(); b.showPopover(); });
  await at100();  console.log('opening, 100ms:', await state());
  await finish(); console.log('opened        :', await state());
  await page.evaluate(() => { a.hidePopover(); b.hidePopover(); });
  await at100();  console.log('closing, 100ms:', await state());
  await finish(); console.log('closed        :', await state());
});
