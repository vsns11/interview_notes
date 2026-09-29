// WeakMap, WeakSet, WeakRef, FinalizationRegistry: holding objects without keeping them alive.

// 1) WeakMap as a side-table: attach data to objects you don't own, without leaks
const metadata = new WeakMap();
function track(el) { metadata.set(el, { clicks: 0, firstSeen: 'now' }); }
const button = { tag: 'button' };               // imagine a DOM element
track(button);
metadata.get(button).clicks++;
console.log(metadata.get(button), metadata.has(button), metadata.has({ tag: 'button' }));

// 2) WeakMap as a memo cache keyed by object
const areaCache = new WeakMap();
let computed = 0;
function area(shape) {
  if (!areaCache.has(shape)) { computed++; areaCache.set(shape, shape.w * shape.h); }
  return areaCache.get(shape);
}
const box = { w: 3, h: 4 };
console.log(area(box), area(box), 'computed', computed, 'time(s)');

// 3) WeakSet to "tag" objects (e.g., already processed / branded)
const processed = new WeakSet();
function processOnce(job) {
  if (processed.has(job)) return 'skipped';
  processed.add(job); return 'processed';
}
const job = { id: 7 };
console.log(processOnce(job), processOnce(job));

// Restrictions: keys must be objects (or non-registered symbols), no size, no iteration
try { new WeakMap().set('str', 1); } catch (e) { console.log('primitive key ->', e.constructor.name); }
const wm = new WeakMap();
console.log('size' in wm, typeof wm[Symbol.iterator], typeof wm.keys);
const sym = Symbol('local');
console.log(new WeakMap([[sym, 'symbol key ok (ES2023)']]).get(sym));
try { new WeakSet().add(Symbol.for('registered')); } catch (e) { console.log('registered symbol ->', e.constructor.name); }

// 4) WeakRef: a reference that does not prevent garbage collection
let bigObject = { payload: 'x'.repeat(10) };
const ref = new WeakRef(bigObject);
console.log(ref.deref()?.payload.length);        // still reachable -> object returned
bigObject = null;                                // after GC, deref() MAY return undefined (timing is unspecified)

// 5) FinalizationRegistry: get a callback some time after an object is collected (never guaranteed)
const registry = new FinalizationRegistry((heldValue) => console.log('cleaned up', heldValue));
registry.register({ temp: true }, 'temp-object');
console.log('registered; the callback may run later or never - do not rely on it');
