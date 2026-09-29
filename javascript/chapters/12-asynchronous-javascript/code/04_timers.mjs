import { setTimeout as sleep } from 'node:timers/promises';

// 1) Extra arguments are passed to the callback; the return value is a handle
const t = setTimeout((a, b) => console.log('never runs', a, b), 50, 'x', 'y');
clearTimeout(t);
console.log('cleared a timer before it fired');

// 2) setInterval until a condition, then clearInterval
let ticks = 0;
const id = setInterval(() => {
  ticks += 1;
  console.log('tick', ticks);
  if (ticks === 3) clearInterval(id);
}, 10);
await sleep(60);

// 3) The delay is a MINIMUM: a busy main thread delays the callback
const t0 = performance.now();
setTimeout(() => console.log(`asked 10 ms, got >= 40 ms: ${performance.now() - t0 >= 40}`), 10);
while (performance.now() - t0 < 40) {}
await sleep(20);

// 4) Recursive setTimeout: the gap is measured from the END of each run,
//    so slow work never piles up (unlike setInterval)
let n = 0;
function poll() {
  n += 1;
  if (n < 3) setTimeout(poll, 5);
  else console.log('recursive setTimeout ran', n, 'times');
}
poll();
await sleep(40);

// 5) Too-large delays overflow: > 2^31-1 ms (~24.8 days) becomes 1 ms
const big = setTimeout(() => console.log('huge delay fired immediately!'), 2 ** 31);
await sleep(20);
clearTimeout(big);

// 6) Promise-based sleep
const s = performance.now();
await sleep(15);
console.log(`awaited sleep(15): >= 15 ms: ${performance.now() - s >= 14.5}`);
