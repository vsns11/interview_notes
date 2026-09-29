// Functions are first-class values: higher-order functions and callbacks.
// 1) Passing a function as an argument (a callback)
function repeat(times, action) {
  for (let i = 0; i < times; i++) action(i);
}
repeat(3, (i) => console.log("callback call", i));

// 2) Built-in higher-order array methods
const orders = [
  { id: 1, total: 25, status: "paid" },
  { id: 2, total: 90, status: "pending" },
  { id: 3, total: 40, status: "paid" },
];
const paidTotals = orders.filter((o) => o.status === "paid").map((o) => o.total);
const revenue = paidTotals.reduce((sum, t) => sum + t, 0);
console.log("paid totals:", paidTotals, "| revenue:", revenue);
console.log("sorted by total desc:", [...orders].sort((a, b) => b.total - a.total).map((o) => o.id));

// 3) Returning a function
function greaterThan(limit) { return (n) => n > limit; }
console.log("[5, 15, 25] > 10:", [5, 15, 25].filter(greaterThan(10)));

// 4) Composition: build new functions from small ones
const compose = (...fns) => (x) => fns.reduceRight((acc, fn) => fn(acc), x);
const pipe = (...fns) => (x) => fns.reduce((acc, fn) => fn(acc), x);
const trim = (s) => s.trim(), lower = (s) => s.toLowerCase(), dashes = (s) => s.replace(/\s+/g, "-");
const slugify = pipe(trim, lower, dashes);
console.log("slug:", slugify("  Hello Big World  "), "| compose order:", compose(dashes, lower, trim)("  A B "));

// 5) Currying / partial application
const curriedAdd = (a) => (b) => (c) => a + b + c;
const add10 = curriedAdd(10);
console.log("curried:", curriedAdd(1)(2)(3), add10(5)(5));
function volume(l, w, h) { return l * w * h; }
const baseTen = volume.bind(null, 10);   // partial application with bind (this = null)
console.log("partial:", baseTen(2, 3));

// 6) Synchronous vs asynchronous callbacks
console.log("A: before");
[1].forEach(() => console.log("B: forEach callback runs immediately (synchronous)"));
setTimeout(() => console.log("D: setTimeout callback runs later (asynchronous)"), 0);
console.log("C: after");

// 7) Functions are objects: they can have properties
function hits() { hits.count = (hits.count ?? 0) + 1; }
hits(); hits();
console.log("function property count:", hits.count);
