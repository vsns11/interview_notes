// 02_patterns_arrays.mjs - two pointers, sliding window, hashing, stack, prefix sums, intervals
import assert from 'node:assert/strict';

// 1. Two pointers: pair with target sum in a SORTED array. O(n) time, O(1) space
function pairSum(sorted, target) {
  let lo = 0, hi = sorted.length - 1;
  while (lo < hi) {
    const s = sorted[lo] + sorted[hi];
    if (s === target) return [lo, hi];
    if (s < target) lo++; else hi--;
  }
  return null;
}
assert.deepEqual(pairSum([1, 3, 4, 6, 9], 12), [1, 4]);
assert.equal(pairSum([1, 2], 10), null);

// Two pointers on a string: palindrome ignoring non-alphanumerics
function isPalindrome(s) {
  const t = s.toLowerCase().replace(/[^a-z0-9]/g, '');
  for (let i = 0, j = t.length - 1; i < j; i++, j--) if (t[i] !== t[j]) return false;
  return true;
}
assert.equal(isPalindrome('A man, a plan, a canal: Panama'), true);
assert.equal(isPalindrome('race a car'), false);

// 2. Sliding window: longest substring without repeating characters. O(n)
function longestUnique(s) {
  const last = new Map();                  // char -> last index seen
  let start = 0, best = 0;
  for (let end = 0; end < s.length; end++) {
    const ch = s[end];
    if (last.has(ch) && last.get(ch) >= start) start = last.get(ch) + 1;   // shrink
    last.set(ch, end);
    best = Math.max(best, end - start + 1);
  }
  return best;
}
assert.equal(longestUnique('abcabcbb'), 3);
assert.equal(longestUnique('bbbb'), 1);
assert.equal(longestUnique(''), 0);

// Fixed-size window: max sum of k consecutive numbers
function maxSumK(nums, k) {
  let sum = 0;
  for (let i = 0; i < k; i++) sum += nums[i];
  let best = sum;
  for (let i = k; i < nums.length; i++) { sum += nums[i] - nums[i - k]; best = Math.max(best, sum); }
  return best;
}
assert.equal(maxSumK([2, 1, 5, 1, 3, 2], 3), 9);

// 3. Hashing: two-sum on an UNSORTED array. O(n) time, O(n) space
function twoSum(nums, target) {
  const indexOf = new Map();
  for (let i = 0; i < nums.length; i++) {
    const need = target - nums[i];
    if (indexOf.has(need)) return [indexOf.get(need), i];
    indexOf.set(nums[i], i);
  }
  return null;
}
assert.deepEqual(twoSum([2, 7, 11, 15], 9), [0, 1]);
assert.deepEqual(twoSum([3, 3], 6), [0, 1]);

// Hashing: group anagrams (key = sorted letters)
function groupAnagrams(words) {
  const groups = new Map();
  for (const w of words) {
    const key = [...w].sort().join('');
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(w);
  }
  return [...groups.values()];
}
assert.deepEqual(groupAnagrams(['eat', 'tea', 'tan', 'ate', 'nat', 'bat']),
  [['eat', 'tea', 'ate'], ['tan', 'nat'], ['bat']]);

// 4. Stack: balanced brackets
function isBalanced(s) {
  const pairs = { ')': '(', ']': '[', '}': '{' };
  const st = [];
  for (const ch of s) {
    if ('([{'.includes(ch)) st.push(ch);
    else if (ch in pairs) { if (st.pop() !== pairs[ch]) return false; }
  }
  return st.length === 0;
}
assert.equal(isBalanced('{[()()]}'), true);
assert.equal(isBalanced('(]'), false);
assert.equal(isBalanced('(('), false);

// Monotonic stack: days until a warmer temperature. O(n): each index pushed/popped once
function dailyTemperatures(t) {
  const ans = new Array(t.length).fill(0);
  const st = [];                                  // indices, temperatures decreasing
  for (let i = 0; i < t.length; i++) {
    while (st.length && t[i] > t[st.at(-1)]) { const j = st.pop(); ans[j] = i - j; }
    st.push(i);
  }
  return ans;
}
assert.deepEqual(dailyTemperatures([73, 74, 75, 71, 69, 72, 76, 73]), [1, 1, 4, 2, 1, 1, 0, 0]);

// 5. Prefix sums: range sum queries in O(1) after O(n) setup
function prefix(nums) {
  const p = [0];
  for (const x of nums) p.push(p.at(-1) + x);
  return (i, j) => p[j + 1] - p[i];             // sum of nums[i..j] inclusive
}
const rangeSum = prefix([3, 1, 4, 1, 5, 9]);
assert.equal(rangeSum(0, 2), 8);
assert.equal(rangeSum(2, 5), 19);

// Prefix sums + hashing: count subarrays whose sum is k (works with negatives)
function subarraySum(nums, k) {
  const count = new Map([[0, 1]]);
  let sum = 0, total = 0;
  for (const x of nums) {
    sum += x;
    total += count.get(sum - k) ?? 0;
    count.set(sum, (count.get(sum) ?? 0) + 1);
  }
  return total;
}
assert.equal(subarraySum([1, 1, 1], 2), 2);
assert.equal(subarraySum([1, -1, 0], 0), 3);

// 6. Intervals: merge overlapping. O(n log n) for the sort
function mergeIntervals(intervals) {
  const sorted = intervals.toSorted((a, b) => a[0] - b[0]);
  const out = [];
  for (const [s, e] of sorted) {
    const last = out.at(-1);
    if (last && s <= last[1]) last[1] = Math.max(last[1], e);
    else out.push([s, e]);
  }
  return out;
}
assert.deepEqual(mergeIntervals([[8, 10], [1, 3], [2, 6], [15, 18]]), [[1, 6], [8, 10], [15, 18]]);
assert.deepEqual(mergeIntervals([[1, 4], [4, 5]]), [[1, 5]]);
assert.deepEqual(mergeIntervals([]), []);

// Intervals: minimum meeting rooms (sweep line over sorted starts/ends)
function minRooms(meetings) {
  const starts = meetings.map(m => m[0]).sort((a, b) => a - b);
  const ends = meetings.map(m => m[1]).sort((a, b) => a - b);
  let rooms = 0, best = 0, j = 0;
  for (const s of starts) {
    while (ends[j] <= s) { j++; rooms--; }       // free rooms whose meeting ended
    rooms++; best = Math.max(best, rooms);
  }
  return best;
}
assert.equal(minRooms([[0, 30], [5, 10], [15, 20]]), 2);
assert.equal(minRooms([[7, 10], [2, 4]]), 1);

console.log('pairSum', pairSum([1, 3, 4, 6, 9], 12), '| longestUnique("abcabcbb")', longestUnique('abcabcbb'));
console.log('twoSum', twoSum([2, 7, 11, 15], 9), '| groupAnagrams', JSON.stringify(groupAnagrams(['eat', 'tea', 'tan', 'ate', 'nat', 'bat'])));
console.log('dailyTemperatures', dailyTemperatures([73, 74, 75, 71, 69, 72, 76, 73]).join(','));
console.log('subarraySum([1,1,1],2)', subarraySum([1, 1, 1], 2), '| merge', JSON.stringify(mergeIntervals([[8, 10], [1, 3], [2, 6], [15, 18]])), '| rooms', minRooms([[0, 30], [5, 10], [15, 20]]));
console.log('all array-pattern asserts passed');
