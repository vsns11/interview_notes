// 04_classics_functions.mjs - debounce, throttle, curry, compose/pipe, memoize, flatten, bind polyfill
import assert from 'node:assert/strict';
import { mock } from 'node:test';

// Fake timers: setTimeout and Date.now only move when we call tick() -> deterministic tests
mock.timers.enable({ apis: ['setTimeout', 'Date'] });
const tick = (ms) => mock.timers.tick(ms);

// ---------- debounce: run ONCE, `wait` ms after the LAST call ----------
function debounce(fn, wait) {
  let timer = null;
  function debounced(...args) {
    clearTimeout(timer);                              // every call restarts the countdown
    timer = setTimeout(() => { timer = null; fn.apply(this, args); }, wait);
  }
  debounced.cancel = () => { clearTimeout(timer); timer = null; };
  return debounced;
}
{
  const calls = [];
  const search = debounce((q) => calls.push(q), 300);
  search('j'); tick(100); search('ja'); tick(100); search('jav'); // user still typing
  tick(299); assert.deepEqual(calls, []);             // 299 ms after last key: nothing yet
  tick(1);   assert.deepEqual(calls, ['jav']);        // fires once, with the LAST args
  search('x'); search.cancel(); tick(1000);
  assert.deepEqual(calls, ['jav']);                   // cancelled
  console.log('debounce calls:', calls);
}

// ---------- throttle: run at most once per `wait` ms (leading + trailing) ----------
function throttle(fn, wait) {
  let last = -Infinity, timer = null, pendingArgs = null, pendingThis = null;
  function throttled(...args) {
    const now = Date.now(), remaining = wait - (now - last);
    if (remaining <= 0) {                             // window open: run now (leading edge)
      clearTimeout(timer); timer = null;
      last = now; fn.apply(this, args);
    } else {                                          // inside window: remember latest call
      pendingArgs = args; pendingThis = this;
      timer ??= setTimeout(() => {                    // trailing edge
        last = Date.now(); timer = null;
        fn.apply(pendingThis, pendingArgs);
      }, remaining);
    }
  }
  return throttled;
}
{
  const calls = [], t0 = Date.now();
  const onScroll = throttle((y) => calls.push(`${y}@${Date.now() - t0}`), 100);
  for (let t = 0; t <= 250; t += 10) { onScroll(t); tick(10); }  // an event every 10 ms
  for (let i = 0; i < 20; i++) tick(10);                          // then 200 quiet ms
  console.log('throttle calls:', calls);
  assert.deepEqual(calls, ['0@0', '90@100', '190@200', '250@300']);
}
mock.timers.reset();

// ---------- curry ----------
function curry(fn) {
  return function curried(...args) {
    if (args.length >= fn.length) return fn.apply(this, args);
    return (...more) => curried.apply(this, [...args, ...more]);
  };
}
const add3 = curry((a, b, c) => a + b + c);
assert.equal(add3(1)(2)(3), 6);
assert.equal(add3(1, 2)(3), 6);
assert.equal(add3(1)(2, 3), 6);
const add1 = add3(1);                                  // partials are reusable
assert.equal(add1(10)(20), 31); assert.equal(add1(0)(0), 1);

// Infinite-arity sum: sum(1)(2)(3)() -> 6
const sum = (a) => (b) => (b === undefined ? a : sum(a + b));
assert.equal(sum(1)(2)(3)(), 6);

// ---------- compose / pipe ----------
const compose = (...fns) => (x) => fns.reduceRight((acc, f) => f(acc), x);   // right-to-left
const pipe = (...fns) => (x) => fns.reduce((acc, f) => f(acc), x);           // left-to-right
const inc = (x) => x + 1, dbl = (x) => x * 2;
assert.equal(compose(inc, dbl)(5), 11);                // inc(dbl(5))
assert.equal(pipe(inc, dbl)(5), 12);                   // dbl(inc(5))
assert.equal(pipe()(7), 7);                            // identity with no functions

// ---------- memoize ----------
function memoize(fn, keyOf = (...args) => JSON.stringify(args)) {
  const cache = new Map();
  return function (...args) {
    const key = keyOf(...args);
    if (cache.has(key)) return cache.get(key);         // has(), so cached undefined still counts
    const value = fn.apply(this, args);
    cache.set(key, value);
    return value;
  };
}
{
  let runs = 0;
  const slowSquare = memoize((n) => { runs++; return n * n; });
  slowSquare(4); slowSquare(4); slowSquare(5);
  assert.equal(runs, 2);
  // memoizing a recursive function: the recursive calls must go through the memoized version
  const fib = memoize((n) => (n < 2 ? n : fib(n - 1) + fib(n - 2)));
  assert.equal(fib(90), 2880067194370816000);           // fast; (note: above 2**53, so not exact)
  console.log('memoize runs:', runs, '| fib(50):', fib(50));
}

// ---------- flatten (like Array.prototype.flat(depth)) ----------
function flatten(arr, depth = Infinity) {
  const out = [];
  for (const x of arr) {
    if (Array.isArray(x) && depth > 0) out.push(...flatten(x, depth - 1));
    else out.push(x);
  }
  return out;
}
// iterative version with an explicit stack (no recursion depth limit)
function flattenDeep(arr) {
  const stack = [...arr], out = [];
  while (stack.length) {
    const x = stack.pop();
    if (Array.isArray(x)) stack.push(...x); else out.push(x);
  }
  return out.reverse();
}
const nested = [1, [2, [3, [4]], 5]];
assert.deepEqual(flatten(nested), [1, 2, 3, 4, 5]);
assert.deepEqual(flatten(nested, 1), [1, 2, [3, [4]], 5]);
assert.deepEqual(flatten(nested, 1), nested.flat(1));   // matches the built-in
assert.deepEqual(flattenDeep(nested), nested.flat(Infinity));

// Flatten an OBJECT into dotted keys (a frequent variant)
function flattenObject(obj, prefix = '', out = {}) {
  for (const [k, v] of Object.entries(obj)) {
    const key = prefix ? `${prefix}.${k}` : k;
    if (v && typeof v === 'object' && !Array.isArray(v)) flattenObject(v, key, out);
    else out[key] = v;
  }
  return out;
}
assert.deepEqual(flattenObject({ a: 1, b: { c: 2, d: { e: 3 } } }), { a: 1, 'b.c': 2, 'b.d.e': 3 });

// ---------- bind polyfill ----------
Function.prototype.myBind = function (thisArg, ...preset) {
  const target = this;
  if (typeof target !== 'function') throw new TypeError('myBind must be called on a function');
  function bound(...later) {
    // called with `new`? then ignore thisArg, like the real bind
    if (new.target) return new target(...preset, ...later);
    return target.apply(thisArg, [...preset, ...later]);
  }
  return bound;
};
{
  const user = { name: 'Ada' };
  function greet(greeting, punct) { return `${greeting}, ${this.name}${punct}`; }
  const hi = greet.myBind(user, 'Hi');
  assert.equal(hi('!'), 'Hi, Ada!');
  assert.equal(hi.call({ name: 'Bob' }, '?'), 'Hi, Ada?');   // bound this can't be overridden
  function Point(x, y) { this.x = x; this.y = y; }
  const P = Point.myBind(null, 1);
  const p = new P(2);
  assert.deepEqual({ ...p }, { x: 1, y: 2 });
  assert.ok(p instanceof Point);
  console.log('myBind:', hi('!'), '| new bound ->', JSON.stringify(p));
}

// call/apply polyfill idea: temporarily put the function on the object
Function.prototype.myCall = function (thisArg, ...args) {
  const ctx = thisArg == null ? globalThis : Object(thisArg);
  const key = Symbol('fn');                              // unique, can't clash
  ctx[key] = this;
  try { return ctx[key](...args); } finally { delete ctx[key]; }
};
assert.equal(function () { return this.v; }.myCall({ v: 42 }), 42);

console.log('curry', add3(1)(2)(3), '| sum()()', sum(1)(2)(3)(), '| compose', compose(inc, dbl)(5), '| pipe', pipe(inc, dbl)(5));
console.log('flatten', JSON.stringify(flatten(nested, 1)), '| flattenObject', JSON.stringify(flattenObject({ a: 1, b: { c: 2, d: { e: 3 } } })));
console.log('all function-classic asserts passed');
