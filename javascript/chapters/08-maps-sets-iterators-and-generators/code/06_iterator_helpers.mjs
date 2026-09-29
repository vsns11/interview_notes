// ES2025 iterator helpers: lazy map/filter/take/... directly on iterators.
function* naturals() { let n = 1; while (true) yield n++; }

// Lazy chain on an INFINITE sequence - only the needed values are computed
const result = naturals()
  .filter((n) => n % 3 === 0)
  .map((n) => n * n)
  .take(4)
  .toArray();
console.log(result);

// Laziness made visible
let pulled = 0;
const lazy = naturals().map((n) => { pulled++; return n * 10; }).take(3);
console.log('pulled before use:', pulled);
console.log([...lazy], 'pulled after use:', pulled);

// drop, flatMap, reduce, some, every, find, forEach
console.log(naturals().drop(5).take(3).toArray());
console.log(['a b', 'c'].values().flatMap((s) => s.split(' ')).toArray());
console.log(naturals().take(5).reduce((sum, n) => sum + n, 0));
console.log(naturals().some((n) => n > 1000), naturals().take(3).every((n) => n < 5), naturals().find((n) => n * n > 50));
naturals().take(3).forEach((n) => process.stdout.write(`#${n} `));
console.log();

// Works on Map/Set iterators without converting to arrays first
const prices = new Map([['tea', 3], ['cake', 5], ['pie', 7]]);
console.log(prices.entries().filter(([, p]) => p > 4).map(([k]) => k).toArray());
console.log(new Set([1, 2, 3]).values().map((x) => x * 2).toArray());

// Iterator.from wraps any iterator/iterable so the helpers are available
const plain = { i: 0, next() { return this.i < 3 ? { value: this.i++, done: false } : { value: undefined, done: true }; } };
console.log(Iterator.from(plain).map((x) => x + 100).toArray());

// Arrays are NOT iterators: use arr.values() to get the lazy helpers
const arr = [1, 2, 3, 4];
console.log(typeof arr.take, arr.values().take(2).toArray());

// Iterator.concat (ES2026) - feature-detect; not in Node 24
console.log('Iterator.concat available:', typeof Iterator.concat === 'function');
