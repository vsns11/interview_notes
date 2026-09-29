// Read computed values at chosen moments of the keyframe animations (all paused and seeked together).
import { withPage } from './measure.mjs';
import { seek } from './anim.mjs';

const names = { '#dc2626': 'red', '#16a34a': 'green', '#cbd5e1': 'grey (own style)' };
const hex = rgb => '#' + rgb.match(/\d+/g).slice(0, 3).map(n => (+n).toString(16).padStart(2, '0')).join('');
await withPage('05_keyframes.html', async page => {
  for (const t of [0, 250, 499, 750, 1000, 1500]) {
    await seek(page, t);
    const v = await page.evaluate(() => {
      const cs = s => getComputedStyle(document.querySelector(s));
      return { bounce: cs('.bounce').translate, spin: cs('.spinner').rotate, slide: cs('.slide').translate,
               fm: ['none', 'forwards', 'backwards', 'both'].map(m => cs('.fm.' + m).backgroundColor) };
    });
    const c = v.fm.map(x => names[hex(x)] ?? hex(x));
    console.log(`t=${String(t).padStart(4)}ms bounce ${v.bounce.padEnd(9)} spin ${v.spin.padEnd(8)} slide ${v.slide.padEnd(9)} | fill none=${c[0]}, forwards=${c[1]}, backwards=${c[2]}, both=${c[3]}`);
  }
  // Animation events, in real time this time: resume everything and listen
  const events = await page.evaluate(() => new Promise(done => {
    const log = [], el = document.querySelector('.fm.both'), t0 = performance.now();
    for (const type of ['animationstart', 'animationend'])
      el.addEventListener(type, () => log.push(`${type} after ~${Math.round((performance.now() - t0) / 100) * 100}ms`));
    el.style.animation = 'none'; el.offsetWidth; el.style.animation = '';     // restart the animation
    setTimeout(() => done(log), 1300);
  }));
  console.log('events on .fm.both (restarted):', events.join(', '));
});
