// 06_promise_polyfills.mjs - Promise.all / allSettled / race / any polyfills, promisify, pool, retry
import assert from 'node:assert/strict';

const sleep = (ms, value, fail = false) =>
  new Promise((res, rej) => setTimeout(() => (fail ? rej(new Error(value)) : res(value)), ms));

// Promise.all: resolve with ALL values in INPUT order, reject on the FIRST rejection
function promiseAll(iterable) {
  return new Promise((resolve, reject) => {
    const items = [...iterable];                       // accept any iterable
    const results = new Array(items.length);
    let remaining = items.length;
    if (remaining === 0) return resolve(results);      // all([]) resolves immediately
    items.forEach((item, i) => {
      Promise.resolve(item).then((v) => {              // wrap non-promises too
        results[i] = v;                                // by index, not push -> keeps order
        if (--remaining === 0) resolve(results);
      }, reject);                                      // first rejection wins; later ones ignored
    });
  });
}

// Promise.allSettled: never rejects; describes each outcome
function promiseAllSettled(iterable) {
  return promiseAll([...iterable].map((item) =>
    Promise.resolve(item).then(
      (value) => ({ status: 'fulfilled', value }),
      (reason) => ({ status: 'rejected', reason }))));
}

// Promise.race: settle like the FIRST input to settle (fulfil OR reject)
function promiseRace(iterable) {
  return new Promise((resolve, reject) => {
    for (const item of iterable) Promise.resolve(item).then(resolve, reject);
  });                                                  // race([]) stays pending forever
}

// Promise.any: first FULFILMENT; rejects with AggregateError only if all reject
function promiseAny(iterable) {
  return new Promise((resolve, reject) => {
    const items = [...iterable], errors = new Array(items.length);
    let remaining = items.length;
    if (remaining === 0) return reject(new AggregateError([], 'All promises were rejected'));
    items.forEach((item, i) => Promise.resolve(item).then(resolve, (e) => {
      errors[i] = e;
      if (--remaining === 0) reject(new AggregateError(errors, 'All promises were rejected'));
    }));
  });
}

// ---- tests: compare each polyfill with the built-in ----
const t0 = Date.now();
const all = await promiseAll([sleep(30, 'slow'), sleep(25, 'fast'), 42]);
const took = Date.now() - t0;                           // parallel: ~30 ms, not 30 + 25
assert.deepEqual(all, ['slow', 'fast', 42]);            // input order, not finish order
assert.deepEqual(all, await Promise.all([sleep(30, 'slow'), sleep(10, 'fast'), 42]));
assert.deepEqual(await promiseAll([]), []);
await assert.rejects(promiseAll([sleep(20, 'ok'), sleep(5, 'bad', true)]), /bad/);
console.log('all:', all, '| parallel (under 55 ms)?', took < 55);

const settled = await promiseAllSettled([1, Promise.reject(new Error('x')), sleep(5, 'y')]);
assert.deepEqual(settled.map((s) => s.status), ['fulfilled', 'rejected', 'fulfilled']);
const builtIn = await Promise.allSettled([1, Promise.reject(new Error('x')), sleep(5, 'y')]);
assert.deepEqual(settled.map((s) => s.value), builtIn.map((s) => s.value));
console.log('allSettled:', settled.map((s) => s.status === 'fulfilled' ? s.value : `rejected(${s.reason.message})`));

assert.equal(await promiseRace([sleep(20, 'a'), sleep(5, 'b')]), 'b');
await assert.rejects(promiseRace([sleep(20, 'a'), sleep(5, 'b', true)]), /b/);
console.log('race:', await promiseRace([sleep(20, 'a'), sleep(5, 'b')]));

assert.equal(await promiseAny([sleep(5, 'e1', true), sleep(15, 'win')]), 'win');
try { await promiseAny([Promise.reject(new Error('p')), Promise.reject(new Error('q'))]); }
catch (e) { console.log('any all-rejected:', e.constructor.name, e.errors.map((x) => x.message)); }

// Timeout wrapper built from race
const withTimeout = (p, ms) => promiseRace([p, sleep(ms, `timeout after ${ms}ms`, true)]);
await assert.rejects(withTimeout(sleep(50, 'late'), 10), /timeout after 10ms/);

// promisify: turn an error-first callback API into a promise API
const promisify = (fn) => (...args) => new Promise((resolve, reject) =>
  fn(...args, (err, value) => (err ? reject(err) : resolve(value))));
const legacyDivide = (a, b, cb) => setTimeout(() => (b === 0 ? cb(new Error('div by 0')) : cb(null, a / b)), 1);
const divide = promisify(legacyDivide);
assert.equal(await divide(6, 3), 2);
await assert.rejects(divide(1, 0), /div by 0/);

// Concurrency limit (promise pool): run tasks with at most `limit` in flight, keep result order
async function pool(tasks, limit) {
  const results = new Array(tasks.length);
  let next = 0, active = 0, peak = 0;
  async function worker() {
    while (next < tasks.length) {
      const i = next++;                                 // safe: JS is single-threaded between awaits
      active++; peak = Math.max(peak, active);
      results[i] = await tasks[i]();
      active--;
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, tasks.length) }, worker));
  return { results, peak };
}
const tasks = [30, 10, 20, 5, 15].map((ms, i) => () => sleep(ms, `t${i}`));
const { results, peak } = await pool(tasks, 2);
assert.deepEqual(results, ['t0', 't1', 't2', 't3', 't4']);
assert.equal(peak, 2);
console.log('pool results:', results, 'peak concurrency:', peak);

// Retry with a fixed number of attempts
async function retry(fn, attempts) {
  let lastErr;
  for (let i = 1; i <= attempts; i++) {
    try { return await fn(i); } catch (e) { lastErr = e; }
  }
  throw lastErr;
}
assert.equal(await retry(async (i) => { if (i < 3) throw new Error(`fail ${i}`); return `ok on ${i}`; }, 5), 'ok on 3');
await assert.rejects(retry(async (i) => { throw new Error(`fail ${i}`); }, 2), /fail 2/);
console.log('all promise asserts passed');
