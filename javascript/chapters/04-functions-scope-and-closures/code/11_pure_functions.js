// Pure vs impure functions.
// Pure: same input -> same output, and no side effects.
const addTax = (price, rate) => +(price * (1 + rate)).toFixed(2);
console.log("pure:", addTax(100, 0.2), addTax(100, 0.2));

// Impure #1: depends on outside mutable state
let rate = 0.2;
const addTaxImpure = (price) => +(price * (1 + rate)).toFixed(2);
console.log("impure before:", addTaxImpure(100));
rate = 0.25;
console.log("impure after someone changed rate:", addTaxImpure(100));

// Impure #2: mutates its argument (a side effect the caller may not expect)
function addItemMutating(cart, item) { cart.push(item); return cart; }
function addItemPure(cart, item) { return [...cart, item]; }
const cart = ["apple"];
const newCart = addItemPure(cart, "pear");
console.log("pure add -> original:", cart, "| new:", newCart);
addItemMutating(cart, "plum");
console.log("mutating add -> original changed:", cart);

// Other side effects: logging, network, DOM, Date.now(), Math.random()
const stamp = () => Date.now();          // impure: different result every call
console.log("Date.now() is impure:", typeof stamp());

// Pure functions are trivially testable
const assert = require("node:assert");
assert.strictEqual(addTax(10, 0.1), 11);
assert.deepStrictEqual(addItemPure([], "x"), ["x"]);
console.log("tests passed");
