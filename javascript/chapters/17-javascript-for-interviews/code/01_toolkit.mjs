// 01_toolkit.mjs - the JavaScript DSA toolkit: arrays, Map/Set, sorting, numbers, strings, BigInt
import assert from 'node:assert/strict';

// --- Arrays as stack / queue / 2D grid ---
const stack = [];
stack.push(1); stack.push(2);
console.log('stack pop:', stack.pop(), 'peek:', stack.at(-1));   // O(1) at the end

const q = [1, 2, 3];
console.log('queue shift:', q.shift(), 'left:', q);             // shift is O(n)!

// A real O(1) queue: keep a head index instead of shifting
class Queue {
  #items = []; #head = 0;
  push(x) { this.#items.push(x); }
  shift() {
    if (this.#head === this.#items.length) return undefined;
    const x = this.#items[this.#head++];
    if (this.#head > 1024 && this.#head * 2 > this.#items.length) {   // compact occasionally
      this.#items = this.#items.slice(this.#head); this.#head = 0;
    }
    return x;
  }
  get size() { return this.#items.length - this.#head; }
}
const qq = new Queue(); qq.push('a'); qq.push('b');
console.log('Queue:', qq.shift(), qq.size);

// 2D grid: Array.from, NOT fill with an array (shared rows!)
const bad = new Array(2).fill(new Array(2).fill(0));
bad[0][0] = 9;
console.log('fill(array) shares rows:', JSON.stringify(bad));
const grid = Array.from({ length: 2 }, () => new Array(2).fill(0));
grid[0][0] = 9;
console.log('Array.from rows are separate:', JSON.stringify(grid));

// --- Map / Set ---
const freq = new Map();
for (const ch of 'banana') freq.set(ch, (freq.get(ch) ?? 0) + 1);
console.log('freq:', [...freq]);                                 // insertion order kept
const seen = new Set([3, 1, 3, 2]);
console.log('set:', [...seen], 'has 2?', seen.has(2));
// Objects/arrays as keys compare by reference, so encode compound keys as strings
const visited = new Set();
visited.add(`${1},${2}`);
console.log('visited has "1,2"?', visited.has('1,2'), '| Set of arrays:', new Set([[1, 2]]).has([1, 2]));
// ES2025 Set methods
const a = new Set([1, 2, 3]), b = new Set([2, 3, 4]);
console.log('union', [...a.union(b)], 'intersection', [...a.intersection(b)], 'difference', [...a.difference(b)]);

// --- Sorting ---
console.log('default sort:', [10, 9, 1, 100].sort());          // strings!
console.log('numeric sort:', [10, 9, 1, 100].sort((x, y) => x - y));
const people = [
  { name: 'Ann', age: 30 }, { name: 'Bob', age: 25 },
  { name: 'Cid', age: 30 }, { name: 'Dee', age: 25 },
];
// stable (guaranteed since ES2019): equal ages keep their original order
console.log('by age (stable):', people.toSorted((p, r) => p.age - r.age).map(p => p.name).join(' '));
// multi-key: age desc, then name asc
console.log('age desc, name asc:', people.toSorted((p, r) => r.age - p.age || p.name.localeCompare(r.name)).map(p => p.name).join(' '));
console.log('original untouched:', people.map(p => p.name).join(' '));

// --- Numbers ---
console.log('0.1 + 0.2 =', 0.1 + 0.2, '| === 0.3?', 0.1 + 0.2 === 0.3);
const close = (x, y) => Math.abs(x - y) < Number.EPSILON * Math.max(1, Math.abs(x), Math.abs(y));
console.log('close(0.1+0.2, 0.3):', close(0.1 + 0.2, 0.3));
console.log('MAX_SAFE_INTEGER:', Number.MAX_SAFE_INTEGER, '| 2**53 + 1 =', 2 ** 53 + 1);
console.log('isSafeInteger(2**53):', Number.isSafeInteger(2 ** 53));
console.log('7 / 2 =', 7 / 2, '| Math.trunc:', Math.trunc(7 / 2), '| -7/2 trunc:', Math.trunc(-7 / 2), 'floor:', Math.floor(-7 / 2));
console.log('-7 % 3 =', -7 % 3, '| true modulo:', ((-7 % 3) + 3) % 3);
console.log('mid without overflow worry:', (3 + 8) >> 1, '| but >> breaks above 2**31:', (2 ** 31 + 2) >> 1);
console.log('Infinity sentinels:', Math.min(), Math.max());
console.log('Math.max(...[]) =', Math.max(...[]), '| parseInt("08") =', parseInt('08'), '| Number("") =', Number(''));
console.log('NaN === NaN:', NaN === NaN, '| Number.isNaN(NaN):', Number.isNaN(NaN), '| isNaN("x"):', isNaN('x'), 'Number.isNaN("x"):', Number.isNaN('x'));

// --- Strings: immutable, so build with an array + join ---
const parts = [];
for (let i = 0; i < 5; i++) parts.push(i);
console.log('join:', parts.join('-'));
console.log('char codes:', 'a'.charCodeAt(0), String.fromCharCode(98), '| index of letter:', 'd'.charCodeAt(0) - 97);
console.log('reverse:', [...'hello'].reverse().join(''));
console.log('count letters with array of 26:', (() => { const c = new Array(26).fill(0); for (const ch of 'abca') c[ch.charCodeAt(0) - 97]++; return c.slice(0, 3); })());

// --- BigInt ---
const big = 2n ** 64n;
console.log('2n ** 64n =', big, '| typeof:', typeof big);
const factorial = (n) => (n <= 1n ? 1n : n * factorial(n - 1n));
console.log('25! =', factorial(25n));
console.log('7n / 2n =', 7n / 2n, '| 1n == 1:', 1n == 1, '| 1n === 1:', 1n === 1);
assert.throws(() => 1n + 1, TypeError);                          // no mixing
console.log('mixing BigInt and Number throws TypeError');
console.log('JSON.stringify(1n) throws?', (() => { try { JSON.stringify(1n); return false; } catch { return true; } })());
