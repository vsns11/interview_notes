// Verify the patterns in 06_patterns.html do what they claim, using real layout numbers.
import { withPage } from './measure.mjs';

const out = await withPage('06_patterns.html', page => page.evaluate(() => {
  const r = el => el.getBoundingClientRect();
  const $ = s => document.querySelector(s), $$ = s => [...document.querySelectorAll(s)];
  const nav = r($('.nav')), btn = r($('.nav .push'));
  const dlg = r($('.dialog')), box = r($('.center'));
  const page = r($('.page')), foot = r($('.page footer'));
  return [
    `navbar : gap nav-right-edge minus button-right-edge = ${nav.right - btn.right}px (the 12px padding)`,
    `centre : space left/right ${dlg.left - box.left} / ${box.right - dlg.right}, top/bottom ${dlg.top - box.top} / ${box.bottom - dlg.bottom}`,
    `cards  : heights ${$$('.card').map(c => r(c).height).join(', ')}; button bottoms ${$$('.card button').map(b => r(b).bottom).join(', ')}`,
    `footer : page bottom ${page.bottom}, footer bottom ${foot.bottom} (only the 1px border below it)`,
    `media  : avatar width ${r($('.avatar')).width}px (flex: none, never shrinks)`,
    `input  : input ${r($('#q')).width}px + button ${r($('.group button')).width}px = form ${r($('.group')).width}px`,
  ];
}));
console.log(out.join('\n'));
