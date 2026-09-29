// 10_patterns_graphs_greedy_bits_math.mjs - topological sort & union-find, greedy, sort & search,
// bit manipulation, math & geometry (patterns 17-21)
import assert from 'node:assert/strict';

// ---------- 17. Topological sort (Kahn) & union-find ----------
// Course order: an edge p -> c means "take p before c". Repeatedly take a course with no
// remaining prerequisites (in-degree 0). If some are never freed, there is a cycle. O(V + E)
function courseOrder(n, prereqs) {               // prereqs: [course, mustTakeFirst]
  const adj = Array.from({ length: n }, () => []);
  const indeg = new Array(n).fill(0);
  for (const [c, p] of prereqs) { adj[p].push(c); indeg[c]++; }
  const order = [];
  for (let i = 0; i < n; i++) if (indeg[i] === 0) order.push(i);
  for (let h = 0; h < order.length; h++)         // `order` doubles as the queue
    for (const v of adj[order[h]]) if (--indeg[v] === 0) order.push(v);
  return order.length === n ? order : null;      // null = cycle, impossible schedule
}
assert.deepEqual(courseOrder(4, [[1, 0], [2, 0], [3, 1], [3, 2]]), [0, 1, 2, 3]);
assert.equal(courseOrder(2, [[0, 1], [1, 0]]), null);

// Union-find (disjoint set union): which group is x in? merge two groups.
// Path halving + union by size make both operations almost O(1) (inverse Ackermann).
class UnionFind {
  constructor(n) {
    this.parent = Array.from({ length: n }, (_, i) => i);   // everyone starts alone
    this.size = new Array(n).fill(1);
    this.sets = n;
  }
  find(x) {
    while (this.parent[x] !== x) {
      this.parent[x] = this.parent[this.parent[x]];        // path halving: skip a level
      x = this.parent[x];
    }
    return x;
  }
  union(a, b) {
    let ra = this.find(a), rb = this.find(b);
    if (ra === rb) return false;                           // already connected
    if (this.size[ra] < this.size[rb]) [ra, rb] = [rb, ra];
    this.parent[rb] = ra;                                  // hang the smaller tree under the bigger
    this.size[ra] += this.size[rb];
    this.sets--;
    return true;
  }
}
function countComponents(n, edges) {
  const uf = new UnionFind(n);
  for (const [a, b] of edges) uf.union(a, b);
  return uf.sets;
}
assert.equal(countComponents(5, [[0, 1], [1, 2], [3, 4]]), 2);
assert.equal(countComponents(4, []), 4);

// Redundant connection: the first edge whose ends are already connected closes a cycle
function redundantEdge(edges) {                  // nodes are 1..n
  const uf = new UnionFind(edges.length + 1);
  for (const e of edges) if (!uf.union(e[0], e[1])) return e;
  return null;
}
assert.deepEqual(redundantEdge([[1, 2], [1, 3], [2, 3]]), [2, 3]);
assert.deepEqual(redundantEdge([[1, 2], [2, 3], [3, 4], [1, 4], [1, 5]]), [1, 4]);

// ---------- 18. Greedy ----------
// Jump game: track the farthest index reachable so far; if i is beyond it, we are stuck. O(n)
function canJump(nums) {
  let reach = 0;
  for (let i = 0; i < nums.length; i++) {
    if (i > reach) return false;
    reach = Math.max(reach, i + nums[i]);
  }
  return true;
}
assert.equal(canJump([2, 3, 1, 1, 4]), true);
assert.equal(canJump([3, 2, 1, 0, 4]), false);

// Gas station: if the tank goes negative at i, no station from `start` to i can be the start,
// so restart at i + 1. If total gas >= total cost, the last restart works. O(n)
function gasStation(gas, cost) {
  let total = 0, tank = 0, start = 0;
  for (let i = 0; i < gas.length; i++) {
    const d = gas[i] - cost[i];
    total += d; tank += d;
    if (tank < 0) { start = i + 1; tank = 0; }
  }
  return total >= 0 ? start : -1;
}
assert.equal(gasStation([1, 2, 3, 4, 5], [3, 4, 5, 1, 2]), 3);
assert.equal(gasStation([2, 3, 4], [3, 4, 3]), -1);

// Candies: each child gets >= 1; a higher rating than a neighbour means more candy.
// Two passes: left-to-right fixes the left neighbour rule, right-to-left the right one. O(n)
function candies(ratings) {
  const n = ratings.length, c = new Array(n).fill(1);
  for (let i = 1; i < n; i++) if (ratings[i] > ratings[i - 1]) c[i] = c[i - 1] + 1;
  for (let i = n - 2; i >= 0; i--) if (ratings[i] > ratings[i + 1]) c[i] = Math.max(c[i], c[i + 1] + 1);
  return c.reduce((a, b) => a + b, 0);
}
assert.equal(candies([1, 0, 2]), 5);             // 2, 1, 2
assert.equal(candies([1, 2, 2]), 4);             // 1, 2, 1
assert.equal(candies([1, 3, 4, 5, 2]), 11);      // 1, 2, 3, 4, 1

// ---------- 19. Sort & search ----------
// Quickselect: partition like quicksort but recurse into ONE side. Average O(n), worst O(n^2)
// (a random pivot makes the worst case very unlikely).
function kthLargest(nums, k) {
  const a = [...nums], target = a.length - k;   // k-th largest = index n-k in ascending order
  let lo = 0, hi = a.length - 1;
  for (;;) {
    const r = lo + Math.floor(Math.random() * (hi - lo + 1));
    [a[r], a[hi]] = [a[hi], a[r]];
    const pivot = a[hi];
    let store = lo;                              // Lomuto partition: smaller values go left
    for (let i = lo; i < hi; i++) if (a[i] < pivot) { [a[i], a[store]] = [a[store], a[i]]; store++; }
    [a[store], a[hi]] = [a[hi], a[store]];       // pivot lands in its final sorted position
    if (store === target) return a[store];
    if (store < target) lo = store + 1; else hi = store - 1;
  }
}
assert.equal(kthLargest([3, 2, 1, 5, 6, 4], 2), 5);
assert.equal(kthLargest([3, 2, 3, 1, 2, 4, 5, 5, 6], 4), 4);
for (let t = 0; t < 200; t++) {                  // compare with sorting on random inputs
  const arr = Array.from({ length: 1 + (t % 30) }, () => Math.floor(Math.random() * 20));
  const k = 1 + (t % arr.length);
  assert.equal(kthLargest(arr, k), arr.toSorted((x, y) => y - x)[k - 1]);
}

// Dutch national flag: sort 0s, 1s, 2s in one pass. [0, lo) = 0s, [lo, mid) = 1s,
// (hi, end] = 2s, [mid, hi] = unknown. O(n) time, O(1) space
function sortColors(a) {
  let lo = 0, mid = 0, hi = a.length - 1;
  while (mid <= hi) {
    if (a[mid] === 0) { [a[lo], a[mid]] = [a[mid], a[lo]]; lo++; mid++; }
    else if (a[mid] === 1) mid++;
    else { [a[mid], a[hi]] = [a[hi], a[mid]]; hi--; }   // don't move mid: new a[mid] is unchecked
  }
  return a;
}
assert.deepEqual(sortColors([2, 0, 2, 1, 1, 0]), [0, 0, 1, 1, 2, 2]);
assert.deepEqual(sortColors([2, 2, 0]), [0, 2, 2]);

// Merge sort: split, sort halves, merge. O(n log n) always, stable (<= takes the left item first)
function mergeSort(a, cmp = (x, y) => x - y) {
  if (a.length <= 1) return a;
  const mid = a.length >> 1;
  const L = mergeSort(a.slice(0, mid), cmp), R = mergeSort(a.slice(mid), cmp);
  const out = [];
  let i = 0, j = 0;
  while (i < L.length && j < R.length) out.push(cmp(L[i], R[j]) <= 0 ? L[i++] : R[j++]);
  return out.concat(L.slice(i), R.slice(j));
}
assert.deepEqual(mergeSort([5, 2, 9, 1, 5, 6]), [1, 2, 5, 5, 6, 9]);
const people = [['ann', 30], ['bob', 25], ['cy', 30], ['di', 25]];
assert.deepEqual(mergeSort(people, (x, y) => x[1] - y[1]).map(p => p[0]), ['bob', 'di', 'ann', 'cy']);   // stable

// ---------- 20. Bit manipulation ----------
// JavaScript bitwise operators work on 32-bit signed integers; `>>> 0` reinterprets as unsigned.
// Single number: x ^ x = 0 and x ^ 0 = x, so pairs cancel and the lone value remains. O(n), O(1)
const singleNumber = (nums) => nums.reduce((acc, x) => acc ^ x, 0);
assert.equal(singleNumber([4, 1, 2, 1, 2]), 4);
assert.equal(singleNumber([-3, 7, 7]), -3);

// Counting bits for 0..n: i >> 1 drops the last bit (already computed), i & 1 is that bit. O(n)
function countBits(n) {
  const bits = new Array(n + 1).fill(0);
  for (let i = 1; i <= n; i++) bits[i] = bits[i >> 1] + (i & 1);
  return bits;
}
assert.deepEqual(countBits(5), [0, 1, 1, 2, 1, 2]);

// Swap odd and even bits: mask each set, shift towards the other, combine. O(1)
const swapOddEvenBits = (x) => (((x & 0xAAAAAAAA) >>> 1) | ((x & 0x55555555) << 1)) >>> 0;
const slowSwap = (x) => {                        // bit-by-bit reference for the asserts
  let out = 0;
  for (let b = 0; b < 32; b += 2) {
    const even = (x >>> b) & 1, odd = (x >>> (b + 1)) & 1;
    out += even * 2 ** (b + 1) + odd * 2 ** b;
  }
  return out;
};
for (const x of [0, 1, 2, 22, 0x40000000, 0x80000000, 0xFFFFFFFF, 123456789])
  assert.equal(swapOddEvenBits(x), slowSwap(x));
assert.equal(swapOddEvenBits(0b10110), 0b101001);
assert.equal((0x40000000 << 1), -2147483648);    // the trap: << gives a signed result
const isPowerOfTwo = (n) => n > 0 && (n & (n - 1)) === 0;   // clears the lowest set bit
assert.deepEqual([1, 6, 64].map(isPowerOfTwo), [true, false, true]);

// ---------- 21. Math & geometry ----------
// Spiral order: peel the matrix layer by layer with four shrinking boundaries. O(rows*cols)
function spiral(m) {
  const out = [];
  let top = 0, bottom = m.length - 1, left = 0, right = m[0].length - 1;
  while (top <= bottom && left <= right) {
    for (let c = left; c <= right; c++) out.push(m[top][c]);
    top++;
    for (let r = top; r <= bottom; r++) out.push(m[r][right]);
    right--;
    if (top <= bottom) { for (let c = right; c >= left; c--) out.push(m[bottom][c]); bottom--; }
    if (left <= right) { for (let r = bottom; r >= top; r--) out.push(m[r][left]); left++; }
  }
  return out;
}
assert.deepEqual(spiral([[1, 2, 3], [4, 5, 6], [7, 8, 9]]), [1, 2, 3, 6, 9, 8, 7, 4, 5]);
assert.deepEqual(spiral([[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]), [1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7]);
assert.deepEqual(spiral([[1], [2], [3]]), [1, 2, 3]);

// Reverse an integer, returning 0 if the result leaves the 32-bit signed range.
// JS numbers are doubles, exact to 2^53, so the intermediate value cannot overflow here;
// we check the range once at the end.
function reverseInt(x) {
  const sign = x < 0 ? -1 : 1;
  let n = Math.abs(x), r = 0;
  while (n > 0) { r = r * 10 + (n % 10); n = Math.floor(n / 10); }
  r *= sign;
  return r < -(2 ** 31) || r > 2 ** 31 - 1 ? 0 : r;
}
assert.equal(reverseInt(123), 321);
assert.equal(reverseInt(-120), -21);
assert.equal(reverseInt(1534236469), 0);         // 9646324351 > 2^31 - 1

// Max points on one line: for each anchor point, group the others by direction.
// A direction is (dx, dy) divided by their gcd with a fixed sign, as a string key:
// exact integers, unlike float slopes, and vertical lines need no special case. O(n^2)
function maxPointsOnLine(points) {
  if (points.length <= 2) return points.length;
  const gcd = (a, b) => (b === 0 ? Math.abs(a) : gcd(b, a % b));
  let best = 0;
  for (let i = 0; i < points.length; i++) {
    const byDir = new Map();
    let same = 1, most = 0;                       // `same` counts the anchor and its duplicates
    for (let j = i + 1; j < points.length; j++) {
      let dx = points[j][0] - points[i][0], dy = points[j][1] - points[i][1];
      if (dx === 0 && dy === 0) { same++; continue; }
      const g = gcd(dx, dy);
      dx /= g; dy /= g;
      if (dx < 0 || (dx === 0 && dy < 0)) { dx = -dx; dy = -dy; }   // (1,2) and (-1,-2) are one line
      const key = `${dx}/${dy}`;
      const c = (byDir.get(key) ?? 0) + 1;
      byDir.set(key, c);
      most = Math.max(most, c);
    }
    best = Math.max(best, most + same);
  }
  return best;
}
assert.equal(maxPointsOnLine([[1, 1], [2, 2], [3, 3]]), 3);
assert.equal(maxPointsOnLine([[1, 1], [3, 2], [5, 3], [4, 1], [2, 3], [1, 4]]), 4);
assert.equal(maxPointsOnLine([[0, 0], [0, 0], [1, 1], [2, 5]]), 3);
assert.equal(maxPointsOnLine([[2, 1], [2, 5], [2, -3], [0, 0]]), 3);   // vertical line x = 2
// Why not float slopes? Two different directions can round to the same double:
assert.equal(94911150 / 94911151 === 94911151 / 94911152, true);
assert.equal(maxPointsOnLine([[0, 0], [94911151, 94911150], [94911152, 94911151]]), 2);   // not collinear

console.log('course order', courseOrder(4, [[1, 0], [2, 0], [3, 1], [3, 2]]).join(','), '| cyclic', courseOrder(2, [[0, 1], [1, 0]]),
  '| components', countComponents(5, [[0, 1], [1, 2], [3, 4]]), '| redundant', JSON.stringify(redundantEdge([[1, 2], [1, 3], [2, 3]])));
console.log('canJump', canJump([2, 3, 1, 1, 4]), canJump([3, 2, 1, 0, 4]), '| gas start', gasStation([1, 2, 3, 4, 5], [3, 4, 5, 1, 2]),
  '| candies', candies([1, 0, 2]), candies([1, 3, 4, 5, 2]));
console.log('2nd largest', kthLargest([3, 2, 1, 5, 6, 4], 2), '| colors', sortColors([2, 0, 2, 1, 1, 0]).join(','),
  '| mergeSort stable', mergeSort(people, (x, y) => x[1] - y[1]).map(p => p[0]).join(','));
console.log('single', singleNumber([4, 1, 2, 1, 2]), '| countBits(5)', countBits(5).join(','),
  '| swap bits 22 ->', swapOddEvenBits(22), '| swap 0x40000000 ->', swapOddEvenBits(0x40000000));
console.log('spiral', spiral([[1, 2, 3], [4, 5, 6], [7, 8, 9]]).join(','), '| reverse', reverseInt(-120), reverseInt(1534236469),
  '| max collinear', maxPointsOnLine([[1, 1], [3, 2], [5, 3], [4, 1], [2, 3], [1, 4]]));
console.log('all graph/greedy/sort/bit/math asserts passed');
