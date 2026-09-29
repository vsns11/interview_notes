// IIFE: Immediately Invoked Function Expression.
(function () {
  const secret = "only visible in here";
  console.log("IIFE ran:", secret);
})();
console.log("secret outside?", typeof secret);

// Arrow IIFE with an argument and a return value
const config = ((env) => {
  const base = { retries: 3 };
  return env === "prod" ? { ...base, debug: false } : { ...base, debug: true };
})("dev");
console.log("config:", config);

// The pre-ES2015 module pattern: an IIFE returning a public API
const Cart = (function () {
  const items = [];                          // private
  function total() { return items.reduce((s, i) => s + i.price, 0); }
  return {
    add(name, price) { items.push({ name, price }); return this; },
    total,
    count: () => items.length,
  };
})();
Cart.add("pen", 2).add("book", 12);
console.log("Cart:", Cart.count(), "items, total", Cart.total(), "| items exposed?", "items" in Cart);

// Async IIFE — used to run await in places without top-level await (Chapter 12)
(async () => {
  const value = await Promise.resolve("async IIFE done");
  console.log(value);
})();

// Why the leading semicolon/parentheses matter: an IIFE must be an EXPRESSION
try {
  eval("function () {}()");
} catch (e) {
  console.log("function statement called directly ->", e.name);
}
