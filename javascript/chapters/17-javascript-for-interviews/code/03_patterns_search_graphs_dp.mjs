// 03_patterns_search_graphs_dp.mjs - heap (+ two heaps, k-way merge), binary search, BFS, DFS, DP with memo, backtracking
import assert from 'node:assert/strict';

// 7. Heap: JavaScript has no built-in priority queue, so write a small binary MinHeap.
//    Array layout: children of i are 2i+1 and 2i+2, parent is (i-1)>>1.
export class MinHeap {
  #a = [];
  #less;
  constructor(less = (x, y) => x < y) { this.#less = less; }   // custom ordering
  get size() { return this.#a.length; }
  peek() { return this.#a[0]; }
  push(x) {                                    // O(log n): add at end, sift up
    const a = this.#a; a.push(x);
    let i = a.length - 1;
    while (i > 0) {
      const p = (i - 1) >> 1;
      if (!this.#less(a[i], a[p])) break;
      [a[i], a[p]] = [a[p], a[i]]; i = p;
    }
  }
  pop() {                                      // O(log n): move last to root, sift down
    const a = this.#a;
    if (a.length === 0) return undefined;
    const top = a[0], last = a.pop();
    if (a.length) {
      a[0] = last;
      let i = 0;
      for (;;) {
        const l = 2 * i + 1, r = l + 1;
        let m = i;
        if (l < a.length && this.#less(a[l], a[m])) m = l;
        if (r < a.length && this.#less(a[r], a[m])) m = r;
        if (m === i) break;
        [a[i], a[m]] = [a[m], a[i]]; i = m;
      }
    }
    return top;
  }
}
const h = new MinHeap();
for (const x of [5, 3, 8, 1, 9, 2]) h.push(x);
const drained = [];
while (h.size) drained.push(h.pop());
assert.deepEqual(drained, [1, 2, 3, 5, 8, 9]);

// k largest elements: keep a min-heap of size k. O(n log k)
function kLargest(nums, k) {
  const heap = new MinHeap();
  for (const x of nums) { heap.push(x); if (heap.size > k) heap.pop(); }
  const out = []; while (heap.size) out.push(heap.pop());
  return out.reverse();
}
assert.deepEqual(kLargest([3, 2, 1, 5, 6, 4], 2), [6, 5]);

// Heap with a comparator: Dijkstra shortest paths on a weighted graph
function dijkstra(graph, src) {
  const dist = new Map([[src, 0]]);
  const pq = new MinHeap((x, y) => x[0] < y[0]);   // [distance, node]
  pq.push([0, src]);
  while (pq.size) {
    const [d, u] = pq.pop();
    if (d > dist.get(u)) continue;                  // stale entry
    for (const [v, w] of graph[u] ?? []) {
      const nd = d + w;
      if (nd < (dist.get(v) ?? Infinity)) { dist.set(v, nd); pq.push([nd, v]); }
    }
  }
  return Object.fromEntries(dist);
}
const roads = { A: [['B', 4], ['C', 1]], C: [['B', 2], ['D', 7]], B: [['D', 1]] };
assert.deepEqual(dijkstra(roads, 'A'), { A: 0, C: 1, B: 3, D: 4 });

// Two heaps: running median of a stream. `low` is a max-heap (smaller half), `high` a min-heap
// (larger half); keep low.size === high.size or one more. add O(log n), median O(1)
class MedianFinder {
  #low = new MinHeap((x, y) => x > y);
  #high = new MinHeap();
  add(x) {
    if (this.#low.size === 0 || x <= this.#low.peek()) this.#low.push(x); else this.#high.push(x);
    if (this.#low.size > this.#high.size + 1) this.#high.push(this.#low.pop());   // rebalance
    else if (this.#high.size > this.#low.size) this.#low.push(this.#high.pop());
  }
  median() {
    return this.#low.size > this.#high.size ? this.#low.peek() : (this.#low.peek() + this.#high.peek()) / 2;
  }
}
const mf = new MedianFinder();
const medians = [5, 15, 1, 3].map(x => { mf.add(x); return mf.median(); });
assert.deepEqual(medians, [5, 10, 5, 4]);

// k-way merge: the heap holds one "front" item per list. O(N log k) for N items in k lists
function mergeKSorted(lists) {
  const pq = new MinHeap((x, y) => x[0] < y[0]);          // [value, list index, position]
  lists.forEach((l, i) => { if (l.length) pq.push([l[0], i, 0]); });
  const out = [];
  while (pq.size) {
    const [v, i, j] = pq.pop();
    out.push(v);
    if (j + 1 < lists[i].length) pq.push([lists[i][j + 1], i, j + 1]);   // refill from the same list
  }
  return out;
}
const merged = mergeKSorted([[1, 4, 7], [2, 5, 8], [0, 3, 6, 9], []]);
assert.deepEqual(merged, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]);

// 8. Binary search: first index whose value is >= target (lower bound). O(log n)
function lowerBound(a, target) {
  let lo = 0, hi = a.length;                        // search in [lo, hi)
  while (lo < hi) {
    const mid = lo + Math.floor((hi - lo) / 2);
    if (a[mid] < target) lo = mid + 1; else hi = mid;
  }
  return lo;
}
assert.equal(lowerBound([1, 3, 3, 5, 8], 3), 1);
assert.equal(lowerBound([1, 3, 3, 5, 8], 4), 3);
assert.equal(lowerBound([1, 3, 3, 5, 8], 99), 5);

// Binary search on the ANSWER: smallest eating speed to finish piles within h hours
function minEatingSpeed(piles, hours) {
  let lo = 1, hi = Math.max(...piles);
  const ok = (k) => piles.reduce((t, p) => t + Math.ceil(p / k), 0) <= hours;
  while (lo < hi) { const mid = (lo + hi) >> 1; if (ok(mid)) hi = mid; else lo = mid + 1; }
  return lo;
}
assert.equal(minEatingSpeed([3, 6, 7, 11], 8), 4);

// 9. BFS: shortest path (fewest steps) in a grid; '#' is a wall. O(rows*cols)
function shortestPath(grid, [sr, sc], [tr, tc]) {
  const R = grid.length, C = grid[0].length;
  const dist = Array.from({ length: R }, () => new Array(C).fill(-1));
  const queue = [[sr, sc]]; dist[sr][sc] = 0;
  for (let head = 0; head < queue.length; head++) {       // index instead of shift()
    const [r, c] = queue[head];
    if (r === tr && c === tc) return dist[r][c];
    for (const [dr, dc] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const nr = r + dr, nc = c + dc;
      if (nr >= 0 && nr < R && nc >= 0 && nc < C && grid[nr][nc] !== '#' && dist[nr][nc] === -1) {
        dist[nr][nc] = dist[r][c] + 1; queue.push([nr, nc]);
      }
    }
  }
  return -1;
}
const maze = ['..#.', '..#.', '....'];
assert.equal(shortestPath(maze, [0, 0], [0, 3]), 7);
assert.equal(shortestPath(['.#', '#.'], [0, 0], [1, 1]), -1);

// 10. DFS: count islands (connected '1' cells). Iterative DFS avoids stack overflow on huge grids
function numIslands(rows) {
  const g = rows.map(r => [...r]);
  let count = 0;
  for (let r = 0; r < g.length; r++) for (let c = 0; c < g[0].length; c++) {
    if (g[r][c] !== '1') continue;
    count++;
    const st = [[r, c]]; g[r][c] = '0';
    while (st.length) {
      const [x, y] = st.pop();
      for (const [nx, ny] of [[x + 1, y], [x - 1, y], [x, y + 1], [x, y - 1]]) {
        if (g[nx]?.[ny] === '1') { g[nx][ny] = '0'; st.push([nx, ny]); }
      }
    }
  }
  return count;
}
assert.equal(numIslands(['11000', '11000', '00100', '00011']), 3);

// DFS on a directed graph: topological order (Kahn's algorithm is the BFS twin); detects cycles
function topoSort(n, edges) {
  const adj = Array.from({ length: n }, () => []);
  for (const [u, v] of edges) adj[u].push(v);
  const state = new Array(n).fill(0);               // 0 new, 1 visiting, 2 done
  const order = [];
  const visit = (u) => {
    if (state[u] === 1) throw new Error('cycle');
    if (state[u] === 2) return;
    state[u] = 1;
    for (const v of adj[u]) visit(v);
    state[u] = 2; order.push(u);
  };
  for (let u = 0; u < n; u++) visit(u);
  return order.reverse();
}
assert.deepEqual(topoSort(4, [[0, 1], [0, 2], [1, 3], [2, 3]]), [0, 2, 1, 3]);
assert.throws(() => topoSort(2, [[0, 1], [1, 0]]), /cycle/);

// 11. DP with memoization: climbing stairs (1 or 2 steps) and coin change
function climb(n, memo = new Map()) {
  if (n <= 1) return 1;
  if (memo.has(n)) return memo.get(n);
  const v = climb(n - 1, memo) + climb(n - 2, memo);
  memo.set(n, v);
  return v;
}
assert.equal(climb(10), 89);
assert.equal(climb(70), 308061521170129);                  // exact: below 2**53
assert.equal(Number.isSafeInteger(climb(80)), false);      // F(81) > 2**53: use BigInt

function coinChange(coins, amount) {                      // bottom-up table
  const dp = new Array(amount + 1).fill(Infinity); dp[0] = 0;
  for (let a = 1; a <= amount; a++)
    for (const c of coins) if (c <= a) dp[a] = Math.min(dp[a], dp[a - c] + 1);
  return dp[amount] === Infinity ? -1 : dp[amount];
}
assert.equal(coinChange([1, 2, 5], 11), 3);
assert.equal(coinChange([2], 3), -1);

// Longest common subsequence: classic 2D DP
function lcs(a, b) {
  const dp = Array.from({ length: a.length + 1 }, () => new Array(b.length + 1).fill(0));
  for (let i = 1; i <= a.length; i++) for (let j = 1; j <= b.length; j++)
    dp[i][j] = a[i - 1] === b[j - 1] ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
  return dp[a.length][b.length];
}
assert.equal(lcs('abcde', 'ace'), 3);

// 12. Backtracking: all subsets and permutations
function subsets(nums) {
  const out = [], cur = [];
  const go = (i) => {
    if (i === nums.length) { out.push([...cur]); return; }   // copy!
    cur.push(nums[i]); go(i + 1); cur.pop();                 // choose, explore, un-choose
    go(i + 1);
  };
  go(0);
  return out;
}
assert.deepEqual(subsets([1, 2]), [[1, 2], [1], [2], []]);

function permutations(nums) {
  const out = [], used = new Array(nums.length).fill(false), cur = [];
  const go = () => {
    if (cur.length === nums.length) { out.push([...cur]); return; }
    for (let i = 0; i < nums.length; i++) {
      if (used[i]) continue;
      used[i] = true; cur.push(nums[i]); go(); cur.pop(); used[i] = false;
    }
  };
  go();
  return out;
}
assert.equal(permutations([1, 2, 3]).length, 6);
assert.deepEqual(permutations([1, 2, 3])[1], [1, 3, 2]);

// N-Queens count (backtracking with pruning via Sets)
function nQueens(n) {
  const cols = new Set(), d1 = new Set(), d2 = new Set();
  let count = 0;
  const place = (r) => {
    if (r === n) { count++; return; }
    for (let c = 0; c < n; c++) {
      if (cols.has(c) || d1.has(r - c) || d2.has(r + c)) continue;
      cols.add(c); d1.add(r - c); d2.add(r + c);
      place(r + 1);
      cols.delete(c); d1.delete(r - c); d2.delete(r + c);
    }
  };
  place(0);
  return count;
}
assert.equal(nQueens(8), 92);

console.log('heap drain', drained.join(','), '| kLargest', kLargest([3, 2, 1, 5, 6, 4], 2), '| dijkstra', JSON.stringify(dijkstra(roads, 'A')));
console.log('running medians', medians.join(','), '| k-way merge', merged.join(','));
console.log('lowerBound 4 ->', lowerBound([1, 3, 3, 5, 8], 4), '| minEatingSpeed', minEatingSpeed([3, 6, 7, 11], 8));
console.log('maze steps', shortestPath(maze, [0, 0], [0, 3]), '| islands', numIslands(['11000', '11000', '00100', '00011']), '| topo', topoSort(4, [[0, 1], [0, 2], [1, 3], [2, 3]]).join(','));
console.log('climb(70)', climb(70), '| climb(80) safe?', Number.isSafeInteger(climb(80)), '| coinChange 11', coinChange([1, 2, 5], 11), '| lcs', lcs('abcde', 'ace'));
console.log('subsets', JSON.stringify(subsets([1, 2])), '| perms of 3:', permutations([1, 2, 3]).length, '| 8-queens', nQueens(8));
console.log('all search/graph/DP asserts passed');
