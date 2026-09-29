import '../../../../tools_dom.mjs';
// requestAnimationFrame: run just before the next paint (~60 times a second on a 60 Hz screen).
// jsdom is created with pretendToBeVisual:true, so it simulates frames at 60 fps.
document.body.innerHTML = '<div id="bar" style="width:0px"></div>';
const bar = document.getElementById('bar');

// 1) Time-based animation: progress comes from the timestamp, not from counting frames
function animate(el, to, duration) {
  return new Promise((resolve) => {
    let start;
    const frames = [];
    function step(now) {                       // now = frame timestamp (ms), same clock as performance.now()
      start ??= now;
      const t = Math.min((now - start) / duration, 1);
      const eased = 1 - (1 - t) ** 3;          // ease-out cubic
      el.style.width = `${Math.round(eased * to)}px`;
      frames.push(el.style.width);
      if (t < 1) requestAnimationFrame(step);
      else resolve(frames);
    }
    requestAnimationFrame(step);
  });
}
const frames = await animate(bar, 300, 200);
console.log(`1) about 12 frames in 200 ms at 60 fps: ${frames.length >= 10 && frames.length <= 14}`);
console.log('   first widths:', frames.slice(0, 4).join(', '), '... last:', frames.at(-1));

// 2) Cancel
const id = requestAnimationFrame(() => console.log('never'));
cancelAnimationFrame(id);
console.log('2) cancelled a frame callback');

// 3) Coalesce many events into one update per frame (e.g. scroll or pointermove)
let scheduled = false, updates = 0, events = 0;
function onPointerMove() {
  events++;
  if (scheduled) return;
  scheduled = true;
  requestAnimationFrame(() => { scheduled = false; updates++; /* read layout, then write styles */ });
}
for (let i = 0; i < 50; i++) onPointerMove();         // 50 events in one burst
await new Promise((r) => setTimeout(r, 40));
console.log(`3) ${events} events -> ${updates} DOM update(s)`);
