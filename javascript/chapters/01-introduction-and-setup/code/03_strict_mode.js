// Sloppy (non-strict) mode vs strict mode. This file is a classic CommonJS script,
// so it starts in sloppy mode; functions that begin with "use strict" run in strict mode.

function sloppyTypo() {
  countr = 1;               // typo: meant "counter". Sloppy mode silently creates a global!
  return typeof globalThis.countr;
}
console.log("sloppy: accidental global created ->", sloppyTypo());

function strictTypo() {
  "use strict";
  try {
    countr2 = 1;            // strict mode refuses to invent a global
  } catch (e) {
    return `${e.name}: ${e.message}`;
  }
}
console.log("strict:", strictTypo());

function sloppyThis() { return this === globalThis; }
function strictThis() { "use strict"; return this; }
console.log("plain call, sloppy this is globalThis ->", sloppyThis());
console.log("plain call, strict this ->", strictThis());

function strictReadOnly() {
  "use strict";
  const frozen = Object.freeze({ x: 1 });
  try { frozen.x = 2; } catch (e) { return e.constructor.name; }
}
function sloppyReadOnly() {
  const frozen = Object.freeze({ x: 1 });
  frozen.x = 2;             // silently ignored
  return frozen.x;
}
console.log("write to frozen object: sloppy keeps", sloppyReadOnly(), "| strict throws", strictReadOnly());

// Duplicate parameter names are a SyntaxError in strict code:
try {
  new Function('"use strict"; return function (a, a) {}');
} catch (e) {
  console.log("duplicate params in strict code ->", e.name);
}
