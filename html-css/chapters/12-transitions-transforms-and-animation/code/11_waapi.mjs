// element.animate(): the same engine as CSS animations, controlled from JavaScript.
import { withPage } from './measure.mjs';

await withPage('11_waapi.html', page => page.evaluate(async () => {
  const log = [];
  const box = document.getElementById('box');
  const anim = box.animate(
    [{ translate: '0 0', opacity: 1 }, { translate: '200px 0', opacity: 0.5 }],   // keyframes
    { duration: 400, easing: 'ease-out', fill: 'forwards', iterations: 1 });     // timing options
  log.push(`created: playState=${anim.playState}, listed by document.getAnimations(): ${document.getAnimations().includes(anim)}`);
  anim.pause(); anim.currentTime = 200;                                           // scrub to the middle
  log.push(`paused at 200ms: translate=${getComputedStyle(box).translate}, opacity=${(+getComputedStyle(box).opacity).toFixed(3)}`);
  anim.playbackRate = 2; anim.play();                                             // double speed from here
  await anim.finished;                                                            // a Promise
  log.push(`finished: currentTime=${anim.currentTime}ms (the remaining 200ms took ~100ms of real time at 2x), playState=${anim.playState}`);
  log.push(`fill: forwards keeps the end: translate=${getComputedStyle(box).translate}`);
  anim.reverse(); await anim.finished;
  log.push(`after reverse(): translate=${getComputedStyle(box).translate}  (back at the start; fill: forwards doesn't hold the START value)`);
  return log.join('\n');
})).then(console.log);
