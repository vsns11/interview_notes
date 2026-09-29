// Default parameters, rest parameters, the arguments object, spread in calls.

// Old way: || fallback (breaks for falsy values like 0 or "")
function oldGreet(name) {
  name = name || "Guest";
  return "Hello, " + name + "!";
}
// Modern way: default parameter (only replaces undefined)
function greet(name = "Guest") {
  return "Hello, " + name + "!";
}
console.log(oldGreet(), "|", greet(), "|", greet("Ana"));
console.log('empty string -> old:', oldGreet(""), "| new:", JSON.stringify(greet("")));
console.log("undefined triggers the default:", greet(undefined), "| null does not:", greet(null));

function orderPizza(type = "Cheese", size = "Medium") {
  return `Ordering a ${size} ${type} pizza.`;
}
console.log(orderPizza());
console.log(orderPizza("Pepperoni"));
console.log(orderPizza(undefined, "Large"));   // skip the first by passing undefined

// Defaults are expressions evaluated AT CALL TIME, left to right, and may use earlier params
let counter = 0;
const nextId = () => ++counter;
function createUser(name, id = nextId(), tag = `${name.toLowerCase()}-${id}`) {
  return { name, id, tag };
}
console.log(createUser("Ana"), createUser("Ben"), createUser("Cy", 99));

// A default re-created on each call (no shared mutable default, unlike Python)
function addItem(item, list = []) { list.push(item); return list; }
console.log(addItem("a"), addItem("b"));

// Timestamp default with a fixed clock for reproducible output
const clock = { now: () => "09:30:00" };
function logMessage(message, timestamp = clock.now()) {
  return `${timestamp}: ${message}`;
}
console.log(logMessage("Server started"), "|", logMessage("Custom", "10:00:00"));

// Required parameter trick: a default that throws
function required(name) { throw new Error(`Missing parameter: ${name}`); }
function connect(url = required("url")) { return "connecting to " + url; }
try { connect(); } catch (e) { console.log(e.message); }

// Rest parameters collect the remaining arguments into a real array
function sum(...numbers) {
  let total = 0;
  for (const n of numbers) total += n;
  return total;
}
console.log("sum():", sum(), "| sum(1,2,3):", sum(1, 2, 3));
function tagAll(tag, ...words) { return words.map(w => `${tag}:${w}`); }
console.log(tagAll("js", "let", "const"));

// The legacy arguments object (array-like, not in arrow functions)
function legacySum() {
  let total = 0;
  for (let i = 0; i < arguments.length; i++) total += arguments[i];
  return { total, isArray: Array.isArray(arguments), length: arguments.length };
}
console.log(legacySum(4, 5, 6));

// Spread syntax in a call expands an array into separate arguments
const nums = [3, 9, 1];
console.log("Math.max(...nums):", Math.max(...nums), "| sum(...nums, 10):", sum(...nums, 10));

// fn.length counts parameters before the first default/rest
console.log("lengths:", orderPizza.length, sum.length, tagAll.length, createUser.length);

// Destructured parameters with defaults: named options
function makeButton({ label = "OK", color = "blue", disabled = false } = {}) {
  return `${label} (${color}${disabled ? ", disabled" : ""})`;
}
console.log(makeButton(), "|", makeButton({ label: "Save", disabled: true }));
