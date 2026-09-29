// 08_qa_async_and_memory.mjs - real outputs for the event-loop, strict-mode and memory questions (17.4)
import v8 from 'node:v8';
import vm from 'node:vm';

// Collect a puzzle's log lines, wait for every queue to drain, then print them on one line
async function puzzle(label, body) {
  const lines = [];
  body((x) => lines.push(x));
  await new Promise((r) => setTimeout(r, 30));
  console.log(label.padEnd(30), lines.join(' '));
}

// Q35 the classic
await puzzle('Q35 classic', (log) => {
  log('1');
  setTimeout(() => log('2'), 0);
  Promise.resolve().then(() => log('3'));
  log('4');
});

// Q36 async/await
await puzzle('Q36 async/await', (log) => {
  async function a() { log('a1'); await b(); log('a2'); }
  async function b() { log('b'); }
  log('s1');
  setTimeout(() => log('t'), 0);
  a();
  new Promise((res) => { log('p'); res(); }).then(() => log('then'));
  log('s2');
});

// Q37 Node extras: the same code as in 07_qa_language.js, but inside an ES module after an await
await puzzle('Q37 ESM (after await)', (log) => {
  Promise.resolve().then(() => log('then'));
  process.nextTick(() => log('nextTick'));
  setImmediate(() => log('immediate'));
  log('sync');
});

// Q38 executor runs synchronously, and a promise settles only once
await puzzle('Q38 executor + settle once', (log) => {
  const p = new Promise((resolve, reject) => {
    log('executor');
    resolve('first'); resolve('second'); reject(new Error('ignored'));
  });
  p.then((v) => log(v), (e) => log('rejected?'));
  log('after');
});

// Q39 try/catch can't catch an error thrown later in a callback
{
  const caught = [];
  const handler = (err) => caught.push(`uncaught: ${err.message}`);
  process.once('uncaughtException', handler);
  try { setTimeout(() => { throw new Error('boom'); }, 0); } catch { caught.push('try/catch caught it'); }
  await new Promise((r) => setTimeout(r, 10));
  // the async version: await inside try DOES catch a rejection
  try { await Promise.reject(new Error('rejected')); } catch (e) { caught.push(`await caught: ${e.message}`); }
  console.log('Q39 errors in callbacks'.padEnd(30), caught.join(' | '));
}

// Q40 await inside forEach does not make forEach wait
{
  const log = [];
  const wait = (ms) => new Promise((r) => setTimeout(r, ms));
  [20, 10].forEach(async (ms) => { await wait(ms); log.push(ms); });
  log.push('forEach returned');
  for (const ms of [20, 10]) { await wait(ms); log.push(`for-of ${ms}`); }
  console.log('Q40 await in forEach'.padEnd(30), log.join(' | '));
}

// Q41 sequential vs parallel awaits
{
  const wait = (ms, v) => new Promise((r) => setTimeout(() => r(v), ms));
  let t = Date.now();
  await wait(40); await wait(40);
  const seq = Date.now() - t;
  t = Date.now();
  await Promise.all([wait(40), wait(40)]);
  const par = Date.now() - t;
  console.log('Q41 sequential vs parallel'.padEnd(30), `sequential >= 80ms: ${seq >= 79}, parallel < 80ms: ${par < 80}`);
}

// Q22 strict mode (ES modules are always strict)
{
  const obj = { name: 'Ada', get() { return this.name; } };
  const lost = obj.get;
  let r1, r2;
  try { lost(); } catch (e) { r1 = `${e.name}: ${e.message}`; }
  try { vm.runInThisContext('"use strict"; undeclaredThing = 1'); } catch (e) { r2 = `${e.name}: ${e.message}`; }
  console.log('Q22/Q23 strict: lost this'.padEnd(30), r1);
  console.log('Q22 strict: undeclared assign'.padEnd(30), r2);
}

// Q51-Q53 memory: a Map keeps keys alive, a WeakMap does not
v8.setFlagsFromString('--expose-gc');
const gc = vm.runInNewContext('gc');
const mb = () => process.memoryUsage().heapUsed / 2 ** 20;
async function measure(cache) {
  gc(); const before = mb();
  for (let i = 0; i < 2000; i++) {
    const request = { id: i };                            // short-lived object
    cache.set(request, new Array(1000).fill(i));          // ~8 KB of metadata per request
  }
  await new Promise((r) => setTimeout(r, 0));
  gc(); return mb() - before;
}
const strongGrowth = await measure(new Map());
const weakGrowth = await measure(new WeakMap());
console.log('Q52 Map vs WeakMap cache'.padEnd(30), `Map retains > 10 MB: ${strongGrowth > 10}, WeakMap retains < 2 MB: ${weakGrowth < 2}`);

let big = { payload: new Array(1e5).fill(1) };
const ref = new WeakRef(big);
console.log('Q53 WeakRef before'.padEnd(30), ref.deref() !== undefined);
big = null;                                              // drop the only strong reference
await new Promise((r) => setTimeout(r, 0));              // deref() keeps it alive until the current job ends
gc();
console.log('Q53 WeakRef after gc'.padEnd(30), ref.deref() === undefined ? 'collected' : 'still alive');
