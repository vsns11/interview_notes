// The same page with the OS setting "reduce motion" off and on: which animations does each user get?
import { withPage } from './measure.mjs';

for (const value of ['no-preference', 'reduce']) {
  const r = await withPage('08_reduced_motion.html', page => page.evaluate(() => {
    const cs = s => getComputedStyle(document.querySelector(s));
    return `card animation: ${cs('.card').animationName.padEnd(8)} spinner: ${cs('.spinner').animationName} ${cs('.spinner').animationDuration}` +
           `   scroll-behavior: ${cs('html').scrollBehavior}   running animations: ${document.getAnimations().length}`;
  }), { media: [{ name: 'prefers-reduced-motion', value }] });
  console.log(`prefers-reduced-motion: ${value.padEnd(13)} -> ${r}`);
}
