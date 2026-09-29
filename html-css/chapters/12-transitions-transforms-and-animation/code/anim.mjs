// tools_run: skip  (helpers imported by the other scripts in this folder)
// Freeze time: pause every running animation/transition on the page (CSS ones are exposed through the
// Web Animations API as CSSAnimation / CSSTransition objects) and jump all of them to the same moment.
export const seek = (page, ms) => page.evaluate(ms => {
  for (const a of document.getAnimations()) { a.pause(); a.currentTime = ms; }
  return new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)));   // let the frame render
}, ms);

// Start the transitions in 01_timing.html (add .go) and pause them at once, before any time passes.
export const startRace = page => page.evaluate(() => {
  document.body.classList.add('go');
  for (const a of document.getAnimations()) { a.pause(); a.currentTime = 0; }
});
