// Recursion: a function that calls itself on a smaller problem.
function factorial(n) {
  if (n <= 1) return 1;          // base case: stops the recursion
  return n * factorial(n - 1);   // recursive case: smaller input
}
console.log("factorial(5):", factorial(5));

// Trace the call stack
function countdown(n, depth = 0) {
  const pad = "  ".repeat(depth);
  if (n === 0) { console.log(pad + "liftoff (base case)"); return; }
  console.log(pad + "countdown(" + n + ")");
  countdown(n - 1, depth + 1);
  console.log(pad + "back in countdown(" + n + ")");
}
countdown(2);

// Natural fit: nested (tree-shaped) data
const folder = {
  name: "root", size: 1,
  children: [
    { name: "docs", size: 4, children: [{ name: "cv.pdf", size: 2, children: [] }] },
    { name: "pics", size: 8, children: [] },
  ],
};
function totalSize(node) {
  let sum = node.size;
  for (const child of node.children) sum += totalSize(child);
  return sum;
}
console.log("total size:", totalSize(folder));

function flatten(arr) {
  const out = [];
  for (const item of arr) {
    if (Array.isArray(item)) out.push(...flatten(item));
    else out.push(item);
  }
  return out;
}
console.log("flatten:", flatten([1, [2, [3, [4]], 5]]));

// Missing base case -> stack overflow
function forever(n) { return forever(n + 1); }
try { forever(0); } catch (e) { console.log("no base case ->", e.name + ":", e.message); }

// Naive Fibonacci is exponential; memoization or a loop fixes it
let calls = 0;
function fib(n) { calls++; return n < 2 ? n : fib(n - 1) + fib(n - 2); }
fib(25);
console.log("naive fib(25) calls:", calls);
function fibLoop(n) { let a = 0, b = 1; for (let i = 0; i < n; i++) [a, b] = [b, a + b]; return a; }
console.log("fibLoop(25):", fibLoop(25), "| fibLoop(50):", fibLoop(50));
