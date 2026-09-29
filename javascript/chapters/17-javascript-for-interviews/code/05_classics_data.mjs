// 05_classics_data.mjs - deep clone, deep equal, LRU cache, EventEmitter
import assert from 'node:assert/strict';

// ---------- structuredClone: the built-in deep copy ----------
const original = {
  when: new Date(0), tags: new Set(['a']), meta: new Map([['k', { n: 1 }]]),
  nested: { list: [1, { deep: true }] },
};
original.self = original;                                 // a cycle
const copy = structuredClone(original);
assert.notEqual(copy.nested, original.nested);            // new objects all the way down
assert.equal(copy.self, copy);                            // cycle preserved
assert.ok(copy.when instanceof Date && copy.tags.has('a'));
console.log('structuredClone keeps Date/Set/Map/cycles:', copy.when.toISOString(), [...copy.tags], copy.self === copy);

// ...but it has limits
try { structuredClone({ f() {} }); } catch (e) { console.log('function ->', e.name); }
class Point { constructor(x) { this.x = x; } norm() { return Math.abs(this.x); } }
const pc = structuredClone(new Point(-3));
console.log('class instance -> plain object:', pc instanceof Point, JSON.stringify(pc));
// JSON round-trip is worse: loses Date type, undefined, Map/Set, and throws on cycles
const viaJson = JSON.parse(JSON.stringify({ d: new Date(0), u: undefined, m: new Map([[1, 2]]), n: NaN }));
console.log('JSON clone:', viaJson);

// ---------- manual deep clone (what interviewers ask you to write) ----------
function deepClone(value, seen = new WeakMap()) {
  if (value === null || typeof value !== 'object') return value;   // primitives & functions
  if (seen.has(value)) return seen.get(value);                     // cycles / shared refs
  if (value instanceof Date) return new Date(value);
  if (value instanceof RegExp) return new RegExp(value.source, value.flags);
  if (value instanceof Map) {
    const m = new Map(); seen.set(value, m);
    for (const [k, v] of value) m.set(deepClone(k, seen), deepClone(v, seen));
    return m;
  }
  if (value instanceof Set) {
    const s = new Set(); seen.set(value, s);
    for (const v of value) s.add(deepClone(v, seen));
    return s;
  }
  // arrays and plain/class objects: keep the prototype, copy own keys (incl. symbols)
  const out = Array.isArray(value) ? [] : Object.create(Object.getPrototypeOf(value));
  seen.set(value, out);
  for (const key of Reflect.ownKeys(value)) {
    const desc = Object.getOwnPropertyDescriptor(value, key);
    if (desc.enumerable) out[key] = deepClone(value[key], seen);
  }
  return out;
}
{
  const c = deepClone(original);
  assert.equal(c.self, c);
  assert.deepEqual(c.meta.get('k'), { n: 1 });
  assert.notEqual(c.meta.get('k'), original.meta.get('k'));
  const p = deepClone(new Point(-3));
  assert.ok(p instanceof Point); assert.equal(p.norm(), 3);        // prototype kept, unlike structuredClone
  const shared = { v: 1 }; const two = deepClone({ a: shared, b: shared });
  assert.equal(two.a, two.b);                                       // shared reference stays shared
  console.log('deepClone keeps class:', p instanceof Point, '| shared ref kept:', two.a === two.b);
}

// ---------- deep equal ----------
function deepEqual(a, b, seen = new WeakMap()) {
  if (Object.is(a, b)) return true;                          // handles NaN and -0 vs +0
  if (typeof a !== 'object' || typeof b !== 'object' || a === null || b === null) return false;
  if (Object.getPrototypeOf(a) !== Object.getPrototypeOf(b)) return false;
  if (seen.get(a) === b) return true;                        // already comparing this pair (cycles)
  seen.set(a, b);
  if (a instanceof Date) return a.getTime() === b.getTime();
  if (a instanceof RegExp) return String(a) === String(b);
  if (a instanceof Map) {
    if (a.size !== b.size) return false;
    for (const [k, v] of a) if (!b.has(k) || !deepEqual(v, b.get(k), seen)) return false;
    return true;
  }
  if (a instanceof Set) {
    if (a.size !== b.size) return false;
    for (const v of a) if (!b.has(v)) return false;          // primitive members only (simplification)
    return true;
  }
  const ka = Reflect.ownKeys(a), kb = Reflect.ownKeys(b);
  if (ka.length !== kb.length) return false;
  return ka.every((k) => Object.hasOwn(b, k) && deepEqual(a[k], b[k], seen));
}
assert.ok(deepEqual({ a: [1, { b: NaN }] }, { a: [1, { b: NaN }] }));
assert.ok(!deepEqual({ a: 1 }, { a: 1, b: undefined }));   // extra key matters
assert.ok(!deepEqual([1, 2], { 0: 1, 1: 2 }));              // array vs object
assert.ok(deepEqual(new Map([[1, { x: 1 }]]), new Map([[1, { x: 1 }]])));
assert.ok(!deepEqual(0, -0) && deepEqual(new Date(5), new Date(5)));
{ const x = { v: 1 }; x.me = x; const y = { v: 1 }; y.me = y; assert.ok(deepEqual(x, y)); }
console.log('deepEqual NaN:', deepEqual({ b: NaN }, { b: NaN }), '| {a:1} vs {a:1,b:undefined}:', deepEqual({ a: 1 }, { a: 1, b: undefined }));

// ---------- LRU cache: Map remembers insertion order -> O(1) get/put ----------
class LRUCache {
  #cap; #map = new Map();
  constructor(capacity) { this.#cap = capacity; }
  get(key) {
    if (!this.#map.has(key)) return -1;
    const v = this.#map.get(key);
    this.#map.delete(key); this.#map.set(key, v);           // move to "most recent" end
    return v;
  }
  put(key, value) {
    this.#map.delete(key);
    this.#map.set(key, value);
    if (this.#map.size > this.#cap) {
      const oldest = this.#map.keys().next().value;          // first key = least recent
      this.#map.delete(oldest);
    }
  }
  keys() { return [...this.#map.keys()]; }
}
{
  const lru = new LRUCache(2);
  lru.put('a', 1); lru.put('b', 2);
  assert.equal(lru.get('a'), 1);          // a is now most recent
  lru.put('c', 3);                        // evicts b
  assert.equal(lru.get('b'), -1);
  assert.deepEqual(lru.keys(), ['a', 'c']);
  lru.put('a', 10);                       // update moves a to the end
  assert.deepEqual(lru.keys(), ['c', 'a']);
  console.log('LRU keys (oldest first):', lru.keys());
}

// ---------- EventEmitter ----------
class EventEmitter {
  #listeners = new Map();                                    // event -> array of fns
  on(event, fn) {
    if (!this.#listeners.has(event)) this.#listeners.set(event, []);
    this.#listeners.get(event).push(fn);
    return () => this.off(event, fn);                        // handy unsubscribe
  }
  off(event, fn) {
    const list = this.#listeners.get(event);
    if (!list) return this;
    const i = list.findIndex((l) => l === fn || l.original === fn);
    if (i !== -1) list.splice(i, 1);
    return this;
  }
  once(event, fn) {
    const wrapper = (...args) => { this.off(event, wrapper); fn.apply(this, args); };
    wrapper.original = fn;                                    // so off(event, fn) still works
    return this.on(event, wrapper);
  }
  emit(event, ...args) {
    const list = this.#listeners.get(event);
    if (!list || list.length === 0) {
      if (event === 'error') throw args[0];                  // same rule as Node
      return false;
    }
    for (const fn of [...list]) fn.apply(this, args);        // copy: listeners may unsubscribe
    return true;
  }
  listenerCount(event) { return this.#listeners.get(event)?.length ?? 0; }
}
{
  const bus = new EventEmitter(), log = [];
  const unsub = bus.on('msg', (m) => log.push(`A:${m}`));
  bus.once('msg', (m) => log.push(`once:${m}`));
  bus.on('msg', (m) => log.push(`B:${m}`));
  bus.emit('msg', 1);
  unsub();
  bus.emit('msg', 2);
  assert.deepEqual(log, ['A:1', 'once:1', 'B:1', 'B:2']);
  assert.equal(bus.emit('nobody'), false);
  assert.throws(() => bus.emit('error', new Error('boom')), /boom/);
  const fn = () => {}; bus.once('x', fn); bus.off('x', fn);
  assert.equal(bus.listenerCount('x'), 0);
  console.log('EventEmitter log:', log);
}
console.log('all data-classic asserts passed');
