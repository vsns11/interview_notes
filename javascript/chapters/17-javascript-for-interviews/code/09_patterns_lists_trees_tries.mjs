// 09_patterns_lists_trees_tries.mjs - linked lists, fast & slow pointers, trees, tries (patterns 13-16)
import assert from 'node:assert/strict';

// ---------- 13. Linked lists ----------
// A node holds a value and a pointer to the next node. JavaScript has no built-in linked list,
// so interview problems give you this shape.
class ListNode { constructor(val, next = null) { this.val = val; this.next = next; } }
const fromArray = (arr) => {
  const dummy = new ListNode(0);                 // dummy head: no special case for the first node
  let tail = dummy;
  for (const v of arr) tail = tail.next = new ListNode(v);
  return dummy.next;
};
const toArray = (head) => { const out = []; for (let n = head; n; n = n.next) out.push(n.val); return out; };

// Reverse in place: flip each arrow while walking. O(n) time, O(1) space
function reverseList(head) {
  let prev = null, cur = head;
  while (cur) {
    const next = cur.next;                       // save before overwriting
    cur.next = prev;
    prev = cur; cur = next;
  }
  return prev;                                   // the old tail is the new head
}
assert.deepEqual(toArray(reverseList(fromArray([1, 2, 3, 4]))), [4, 3, 2, 1]);
assert.equal(reverseList(null), null);

// Remove the k-th node from the end in one pass: `lead` runs k nodes ahead of `trail`
function removeKthFromEnd(head, k) {
  const dummy = new ListNode(0, head);           // handles "remove the head" too
  let lead = dummy, trail = dummy;
  for (let i = 0; i < k; i++) lead = lead.next;
  while (lead.next) { lead = lead.next; trail = trail.next; }
  trail.next = trail.next.next;                  // trail sits just before the victim
  return dummy.next;
}
assert.deepEqual(toArray(removeKthFromEnd(fromArray([1, 2, 3, 4, 5]), 2)), [1, 2, 3, 5]);
assert.deepEqual(toArray(removeKthFromEnd(fromArray([1, 2, 3]), 3)), [2, 3]);

// Intersection of two lists: each pointer walks A then B (or B then A). Both travel
// lenA + lenB, so they arrive at the shared node together (or both at null). O(1) space
function intersection(a, b) {
  let p = a, q = b;
  while (p !== q) { p = p ? p.next : b; q = q ? q.next : a; }
  return p;
}
const shared = fromArray([8, 9]);
const listA = new ListNode(1, new ListNode(2, new ListNode(3, shared)));
const listB = new ListNode(5, shared);
assert.equal(intersection(listA, listB), shared);
assert.equal(intersection(fromArray([1, 2]), fromArray([1, 2])), null);   // equal values, different nodes

// ---------- 14. Fast & slow pointers ----------
// Cycle detection (Floyd): if there is a loop, the fast pointer laps the slow one.
// Then a pointer from the head and one from the meeting point meet at the loop's start.
function cycleStart(head) {
  let slow = head, fast = head;
  while (fast && fast.next) {
    slow = slow.next; fast = fast.next.next;
    if (slow === fast) {
      let p = head;
      while (p !== slow) { p = p.next; slow = slow.next; }
      return p;
    }
  }
  return null;                                   // fast fell off the end: no cycle
}
const loop = fromArray([1, 2, 3, 4, 5]);
loop.next.next.next.next.next = loop.next.next;  // 5 -> 3
assert.equal(cycleStart(loop).val, 3);
assert.equal(cycleStart(fromArray([1, 2, 3])), null);

// Middle node: when fast reaches the end, slow is halfway (second middle for even lengths)
function middle(head) {
  let slow = head, fast = head;
  while (fast && fast.next) { slow = slow.next; fast = fast.next.next; }
  return slow;
}
assert.equal(middle(fromArray([1, 2, 3, 4, 5])).val, 3);
assert.equal(middle(fromArray([1, 2, 3, 4])).val, 3);

// Happy number: the "list" is implicit (n -> sum of squared digits). It either reaches 1
// or loops forever; fast & slow detects the loop without a Set.
function isHappy(n) {
  const next = (x) => { let s = 0; while (x > 0) { const d = x % 10; s += d * d; x = Math.floor(x / 10); } return s; };
  let slow = n, fast = next(n);
  while (fast !== 1 && slow !== fast) { slow = next(slow); fast = next(next(fast)); }
  return fast === 1;
}
assert.equal(isHappy(19), true);                 // 19 -> 82 -> 68 -> 100 -> 1
assert.equal(isHappy(2), false);

// ---------- 15. Trees ----------
class TreeNode { constructor(val, left = null, right = null) { this.val = val; this.left = left; this.right = right; } }
// Build from level order with nulls for gaps: [3, 5, 1, null, 2] etc.
function fromLevels(vals) {
  if (!vals.length || vals[0] == null) return null;
  const root = new TreeNode(vals[0]), queue = [root];
  let i = 1;
  for (let h = 0; h < queue.length && i < vals.length; h++) {
    const node = queue[h];
    if (vals[i] != null) queue.push(node.left = new TreeNode(vals[i])); i++;
    if (i < vals.length && vals[i] != null) queue.push(node.right = new TreeNode(vals[i])); i++;
  }
  return root;
}

// Validate a BST: pass down the allowed (lo, hi) range; checking only parent vs child is not enough
function isValidBST(node, lo = -Infinity, hi = Infinity) {
  if (!node) return true;
  if (node.val <= lo || node.val >= hi) return false;
  return isValidBST(node.left, lo, node.val) && isValidBST(node.right, node.val, hi);
}
assert.equal(isValidBST(fromLevels([5, 3, 7, 2, 4, 6, 8])), true);
assert.equal(isValidBST(fromLevels([5, 1, 7, null, null, 4, 8])), false);   // 4 sits right of 5

// Lowest common ancestor: return whichever target a subtree contains; the first node
// that gets a non-null answer from BOTH sides is the LCA.
function lca(root, p, q) {
  if (!root || root.val === p || root.val === q) return root;
  const left = lca(root.left, p, q), right = lca(root.right, p, q);
  return left && right ? root : left ?? right;
}
const family = fromLevels([3, 5, 1, 6, 2, 0, 8, null, null, 7, 4]);
assert.equal(lca(family, 5, 1).val, 3);
assert.equal(lca(family, 7, 4).val, 2);
assert.equal(lca(family, 5, 4).val, 5);          // a node is its own ancestor

// Serialize / deserialize: preorder with '#' for empty children is enough to rebuild the shape
function serialize(root) {
  const out = [];
  const go = (n) => { if (!n) { out.push('#'); return; } out.push(n.val); go(n.left); go(n.right); };
  go(root);
  return out.join(',');
}
function deserialize(text) {
  const tokens = text.split(',');
  let i = 0;
  const go = () => {
    const t = tokens[i++];
    if (t === '#') return null;
    const node = new TreeNode(Number(t));
    node.left = go(); node.right = go();         // same order as serialize
    return node;
  };
  return go();
}
const encoded = serialize(fromLevels([1, 2, 3, null, null, 4, 5]));
assert.equal(encoded, '1,2,#,#,3,4,#,#,5,#,#');
assert.equal(serialize(deserialize(encoded)), encoded);
assert.equal(serialize(deserialize(serialize(null))), '#');

// ---------- 16. Tries ----------
// A trie (prefix tree) stores words letter by letter; words sharing a prefix share a path.
class Trie {
  #root = { next: new Map(), end: false };
  insert(word) {
    let n = this.#root;
    for (const ch of word) {
      if (!n.next.has(ch)) n.next.set(ch, { next: new Map(), end: false });
      n = n.next.get(ch);
    }
    n.end = true;                                // marks "a word finishes here"
  }
  #walk(prefix) {
    let n = this.#root;
    for (const ch of prefix) { n = n.next.get(ch); if (!n) return null; }
    return n;
  }
  search(word) { return this.#walk(word)?.end === true; }
  startsWith(prefix) { return this.#walk(prefix) !== null; }
  // '.' matches any one letter: branch into every child at that position
  match(pattern) {
    const go = (n, i) => {
      if (i === pattern.length) return n.end;
      if (pattern[i] !== '.') { const nx = n.next.get(pattern[i]); return nx ? go(nx, i + 1) : false; }
      for (const nx of n.next.values()) if (go(nx, i + 1)) return true;
      return false;
    };
    return go(this.#root, 0);
  }
}
const trie = new Trie();
for (const w of ['car', 'card', 'care', 'cat', 'dog']) trie.insert(w);
assert.equal(trie.search('car'), true);
assert.equal(trie.search('ca'), false);          // a prefix, not a stored word
assert.equal(trie.startsWith('ca'), true);
assert.equal(trie.match('c.r.'), true);          // card / care
assert.equal(trie.match('.o.'), true);           // dog
assert.equal(trie.match('c..s'), false);

// Word search on a board: one DFS walks the board AND the trie together, so all words
// are searched at once and a path stops as soon as no word has that prefix.
function findWords(board, words) {
  const root = {};
  for (const w of words) { let n = root; for (const ch of w) n = n[ch] ??= {}; n.$word = w; }
  const grid = board.map(row => [...row]);       // copy: we mark cells while exploring
  const R = grid.length, C = grid[0].length, found = [];
  const dfs = (r, c, node) => {
    const ch = grid[r][c], next = node[ch];
    if (!next) return;                           // no word continues this way: prune
    if (next.$word) { found.push(next.$word); delete next.$word; }   // report each word once
    grid[r][c] = '#';                            // choose: a cell is used once per path
    for (const [dr, dc] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const nr = r + dr, nc = c + dc;
      if (nr >= 0 && nr < R && nc >= 0 && nc < C && grid[nr][nc] !== '#') dfs(nr, nc, next);
    }
    grid[r][c] = ch;                             // un-choose
  };
  for (let r = 0; r < R; r++) for (let c = 0; c < C; c++) dfs(r, c, root);
  return found.sort();
}
const board = ['cats', 'ored', 'waxy'];
const words = ['cat', 'cats', 'car', 'core', 'cow', 'dog', 'red', 'wax', 'tax'];
assert.deepEqual(findWords(board, words), ['car', 'cat', 'cats', 'core', 'cow', 'red', 'wax']);

console.log('reverse', toArray(reverseList(fromArray([1, 2, 3, 4]))).join(','),
  '| remove 2nd from end', toArray(removeKthFromEnd(fromArray([1, 2, 3, 4, 5]), 2)).join(','),
  '| intersection at', intersection(listA, listB).val);
console.log('cycle starts at', cycleStart(loop).val, '| middle of 1..4', middle(fromArray([1, 2, 3, 4])).val,
  '| happy 19?', isHappy(19), '| happy 2?', isHappy(2));
console.log('validBST', isValidBST(fromLevels([5, 3, 7, 2, 4, 6, 8])), isValidBST(fromLevels([5, 1, 7, null, null, 4, 8])),
  '| lca(7,4)', lca(family, 7, 4).val, '| serialized', encoded);
console.log('trie search car', trie.search('car'), '| prefix ca', trie.startsWith('ca'), '| match c.r.', trie.match('c.r.'),
  '| board words', findWords(board, words).join(','));
console.log('all list/tree/trie asserts passed');
