"use strict"; // strict: a plain function call gets this === undefined
// Arrow functions: shorter syntax, lexical this, no arguments object, not constructors.
const square = n => n * n;                       // one parameter: parentheses optional
const add = (a, b) => a + b;                     // several parameters: parentheses required
const hello = () => "hello";                     // no parameters: empty parentheses required
const greet = (name = "Guest") => "Hello, " + name + "!"; // default parameter
const describe = (user) => {                     // block body: needs an explicit return
  const words = user.name.split(" ");
  return `${words.length} word(s)`;
};
console.log(square(5), add(2, 3), hello(), greet(), greet("Ana"), describe({ name: "Ada Lovelace" }));

// Returning an object literal: wrap it in parentheses
const makePointWrong = (x, y) => { x: x, y };    // parsed as a block with a label -> returns undefined
const makePoint = (x, y) => ({ x, y });
console.log("without ():", makePointWrong(1, 2), "| with ():", makePoint(1, 2));

// Great for callbacks
const prices = [5, 12, 30];
console.log("with tax:", prices.map(p => +(p * 1.2).toFixed(2)), "| over 10:", prices.filter(p => p > 10));

// Lexical this: an arrow uses the `this` of the code around it.
const timer = {
  seconds: 0,
  tickRegular() {
    return [1, 2, 3].map(function () { return this === undefined ? "undefined" : typeof this; });
  },
  tickArrow() {
    return [1, 2, 3].map(() => { this.seconds++; return this.seconds; });
  },
};
console.log("regular callback this:", timer.tickRegular()[0], "| arrow callback:", timer.tickArrow());

// Arrow as an object method: `this` is NOT the object
const counter = {
  count: 5,
  regular() { return this.count; },
  arrow: () => this?.count,                        // `this` comes from the surrounding (file-level) code
};
console.log("method regular:", counter.regular(), "| method arrow:", counter.arrow());

// No own `arguments`, cannot be used with new
function outer() {
  const inner = () => arguments.length;          // uses outer's arguments
  return inner();
}
console.log("arrow sees outer arguments.length:", outer(1, 2, 3));
try { new square(2); } catch (e) { console.log("new on arrow ->", e.name + ":", e.message); }
