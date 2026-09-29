// Set: a collection of unique values, plus the ES2025 set-algebra methods.
const colors = new Set(['red', 'green', 'blue', 'red']);
console.log(colors, colors.size);

const numbers = new Set();
numbers.add(1).add(2).add(2);                      // add is chainable; duplicates ignored
console.log(numbers.has(1), numbers.has(3), numbers.size, numbers.delete(1), numbers.delete(1), numbers);
numbers.clear();
console.log(numbers.size);

// Iteration: insertion order; values(), keys() (alias) and entries() as [v, v]
const s = new Set([1, 2, 3]);
s.forEach((value) => process.stdout.write(value + ' '));
console.log();
console.log([...s.values()], [...s.keys()], [...s.entries()]);

// Uniqueness uses SameValueZero: NaN is deduplicated, objects by reference
const mixed = new Set([NaN, NaN, 0, -0, '1', 1, { a: 1 }, { a: 1 }]);
console.log(mixed.size, mixed);

// Classic uses
console.log([...new Set([3, 1, 3, 2, 1])]);                                   // dedupe an array
const visited = new Set();
for (const url of ['/a', '/b', '/a', '/c', '/b']) {
  if (visited.has(url)) continue;
  visited.add(url);
}
console.log([...visited]);

// Set algebra - ES2025 methods (Node 22+, all current browsers)
const a = new Set([1, 2, 3, 4]);
const b = new Set([3, 4, 5]);
console.log('union              ', a.union(b));
console.log('intersection       ', a.intersection(b));
console.log('difference a-b     ', a.difference(b));
console.log('symmetricDifference', a.symmetricDifference(b));
console.log('isSubsetOf         ', new Set([3, 4]).isSubsetOf(a), a.isSupersetOf(new Set([1, 2])), a.isDisjointFrom(new Set([9])));

// The pre-ES2025 way (still useful for arrays)
const setA = new Set([1, 2, 3]);
const setB = new Set([2, 3, 4]);
const intersection = new Set([...setA].filter((x) => setB.has(x)));
console.log(intersection);

// Argument can be any "set-like" (has size, has(), keys()), e.g. a Map's keys
const allowed = new Map([['read', true], ['write', false]]);
console.log(new Set(['read', 'delete']).intersection(allowed));

// Performance: has() is O(1) on average vs array includes() O(n)
const big = Array.from({ length: 200_000 }, (_, i) => i);
const bigSet = new Set(big);
let t = performance.now(); for (let i = 0; i < 2000; i++) big.includes(199_999); const arrMs = performance.now() - t;
t = performance.now(); for (let i = 0; i < 2000; i++) bigSet.has(199_999); const setMs = performance.now() - t;
console.log('Set.has faster than Array.includes here:', setMs < arrMs);
