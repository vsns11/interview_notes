// Defining, calling, parameters, return values.
function greet() {                     // declaration: no parameters, no return
  console.log("Hello, world!");
}
greet();                               // call (invoke) it
greet();                               // ...as often as you like

function greetPerson(name) {           // one parameter
  console.log("Hello, " + name + "!");
}
greetPerson("Alice");                  // "Alice" is the argument

function square(number) {              // return sends a value back to the caller
  return number * number;
}
const result = square(4);
console.log("square(4) =", result, "| square(square(2)) =", square(square(2)));

function noReturn() {}
console.log("function without return gives:", noReturn());

function early(n) {
  if (n < 0) return "negative";        // return also ends the function immediately
  return "non-negative";
  console.log("never runs");           // unreachable
}
console.log(early(-1), early(3));

// Missing arguments are undefined; extra arguments are ignored (but available in `arguments`).
function describe(a, b) { return `a=${a}, b=${b}`; }
console.log(describe(1), "|", describe(1, 2, 3));

// Functions are values: store them, pass them, check their name/length.
const isEven = function (number) {     // function expression
  return number % 2 === 0;
};
console.log("isEven(4):", isEven(4), "| isEven(5):", isEven(5));
console.log("typeof isEven:", typeof isEven, "| name:", isEven.name, "| length:", describe.length);

// Referencing vs calling
console.log("square without () is the function itself:", typeof square, "| with ():", square(3));

// Arguments are passed by value — but an object "value" is a reference.
function tryToChange(num, obj) {
  num = 100;          // changes only the local copy
  obj.changed = true; // changes the shared object
  obj = { replaced: true }; // rebinding the local name doesn't affect the caller
}
let n = 1; const o = {};
tryToChange(n, o);
console.log("after call: n =", n, "| o =", o);
