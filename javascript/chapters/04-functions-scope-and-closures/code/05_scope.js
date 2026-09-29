// Global, function and block scope; lexical scoping and shadowing.
const vm = require("node:vm");

var globalVar = "global var";
let globalLet = "global let";
const globalConst = "global const";

function readGlobals() {
  return [globalVar, globalLet, globalConst].join(" | ");
}
console.log("inside function:", readGlobals());

function testScope() {
  if (true) {
    var functionScoped = "var: visible in the whole function";
    let blockLet = "let: only in this block";
    const blockConst = "const: only in this block";
    console.log("inside block:", blockLet, "/", blockConst);
  }
  console.log("after block:", functionScoped);
  try { blockLet; } catch (e) { console.log("after block, let ->", e.name + ":", e.message); }
}
testScope();
try { functionScoped; } catch (e) { console.log("outside function, var ->", e.name); }

// Loop counters: let stays in the loop, var leaks
for (var vi = 0; vi < 3; vi++) {}
for (let li = 0; li < 3; li++) {}
console.log("var loop counter after loop:", vi, "| let counter exists?", typeof li);

// Shadowing: an inner declaration hides an outer one with the same name
const color = "red";
function paint() {
  const color = "blue";            // shadows the outer color inside paint
  return color;
}
console.log("shadowed:", paint(), "| outer still:", color);

// Lexical scope: what matters is WHERE a function is written, not where it is called
const level = "global";
function showLevel() { return level; }
function caller() {
  const level = "caller";
  return showLevel();               // showLevel looks up `level` where it was DEFINED
}
console.log("lexical scope result:", caller());

// Scope chain: inner -> outer -> ... -> global
const a = 1;
function outer() {
  const b = 2;
  function inner() {
    const c = 3;
    return a + b + c;               // c local, b from outer, a from global
  }
  return inner();
}
console.log("scope chain sum:", outer());

// Classic scripts vs modules: top-level var becomes a property of the global object only in classic scripts.
const sandbox = {};
vm.createContext(sandbox);
vm.runInContext('var fromVar = 1; let fromLet = 2; const fromConst = 3;', sandbox);
console.log("classic script globals -> var on global object:", "fromVar" in sandbox,
  "| let:", "fromLet" in sandbox, "| const:", "fromConst" in sandbox);
console.log("this CommonJS file's top-level var on globalThis?", "globalVar" in globalThis);
