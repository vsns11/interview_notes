// Spread, rest, destructuring, copying, array-likes, and common recipes.
const a = [1, 2], b = [3, 4];
console.log([...a, ...b], [0, ...a, 2.5, ...b], Math.max(...b), [...'hi'], [...new Set([1, 1, 2])]);

// Destructuring by position
const [first, second, third = 'default', ...rest] = ['x', 'y'];
console.log(first, second, third, rest);
const [, , keep] = ['skip', 'skip', 'take'];               // holes skip positions
console.log(keep);
let p = 1, q = 2;
[p, q] = [q, p];                                           // swap without a temp variable
console.log(p, q);
const [[r1c1, r1c2], [r2c1]] = [[1, 2], [3, 4]];           // nested
console.log(r1c1, r1c2, r2c1);
const { 0: head, length } = ['h', 'e', 'y'];               // arrays are objects: object pattern works too
console.log(head, length);

// Function results and parameters
const minMax = (arr) => [Math.min(...arr), Math.max(...arr)];
const [lo, hi] = minMax([5, 3, 9]);
console.log(lo, hi);
const sum = (...nums) => nums.reduce((s, n) => s + n, 0);  // rest parameter
console.log(sum(1, 2, 3), sum(...[10, 20]));

// Copies are shallow
const grid = [[0, 0], [0, 0]];
const shallow = [...grid];
const deep = structuredClone(grid);
shallow[0][0] = 9;
console.log(grid[0][0], deep[0][0]);

// The Array(n).fill([]) trap: every slot is the SAME inner array
const bad = Array(3).fill([]);
bad[0].push('x');
const good = Array.from({ length: 3 }, () => []);
good[0].push('x');
console.log(bad, good);

// Array-likes and iterables -> real arrays
const arrayLike = { 0: 'a', 1: 'b', length: 2 };
console.log(Array.from(arrayLike), Array.prototype.map.call(arrayLike, (s) => s.toUpperCase()));
function oldStyle() { return Array.from(arguments); }
console.log(oldStyle(1, 2, 3));

// Recipes
const range = (start, end, step = 1) => Array.from({ length: Math.ceil((end - start) / step) }, (_, i) => start + i * step);
console.log(range(0, 5), range(10, 0, -3));
const unique = (arr) => [...new Set(arr)];
console.log(unique([3, 1, 3, 2, 1]));
const chunk = (arr, size) => Array.from({ length: Math.ceil(arr.length / size) }, (_, i) => arr.slice(i * size, i * size + size));
console.log(chunk([1, 2, 3, 4, 5], 2));
const zip = (xs, ys) => xs.map((x, i) => [x, ys[i]]);
console.log(zip(['a', 'b'], [1, 2]));
const last = (arr) => arr.at(-1);
console.log(last([1, 2, 3]), last([]));
const arraysEqual = (x, y) => x.length === y.length && x.every((v, i) => Object.is(v, y[i]));
console.log(arraysEqual([1, NaN], [1, NaN]), [1, 2] == '1,2');

// Emptying an array: new array vs length = 0
const shared = [1, 2, 3]; const alias = shared;
shared.length = 0;
console.log(alias);
