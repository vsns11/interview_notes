// Real-world helpers: retry with backoff, a concurrency limit, and
// avoiding out-of-order responses ("last request wins").
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// 1) Retry with exponential backoff
async function retry(fn, { tries = 4, base = 5 } = {}) {
  for (let attempt = 1; ; attempt++) {
    try {
      return await fn(attempt);
    } catch (err) {
      if (attempt >= tries) throw new Error(`gave up after ${attempt} tries`, { cause: err });
      const wait = base * 2 ** (attempt - 1);   // 5, 10, 20 ... (add random jitter in production)
      console.log(`  attempt ${attempt} failed (${err.message}); retrying in ${wait} ms`);
      await sleep(wait);
    }
  }
}
const flaky = async (attempt) => { if (attempt < 3) throw new Error('503'); return 'payload'; };
console.log('retry result:', await retry(flaky));

// 2) Run many tasks but at most `limit` at once
async function mapLimit(items, limit, fn) {
  const results = new Array(items.length);
  let next = 0, running = 0, peak = 0;
  async function worker() {
    while (next < items.length) {
      const i = next++;
      running++; peak = Math.max(peak, running);
      results[i] = await fn(items[i]);
      running--;
    }
  }
  await Promise.all(Array.from({ length: limit }, worker));
  return { results, peak };
}
const { results, peak } = await mapLimit([1, 2, 3, 4, 5, 6, 7], 3, async (x) => { await sleep(5 + (x % 3) * 3); return x * x; });
console.log('mapLimit    :', results, 'peak concurrency =', peak);

// 3) Race condition: an older, slower response arrives last and overwrites newer data
const search = async (q) => { await sleep(q === 'ja' ? 30 : 10); return `results for "${q}"`; };
let shown = '';
await Promise.all(['ja', 'jav'].map(async (q) => { shown = await search(q); }));
console.log('naive shows :', shown, '<- stale!');

let latest = 0;
await Promise.all(['ja', 'jav'].map(async (q) => {
  const ticket = ++latest;
  const r = await search(q);
  if (ticket === latest) shown = r;                 // ignore answers to old requests
}));
console.log('guarded     :', shown);
