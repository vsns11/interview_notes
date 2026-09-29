// for...of: loop over the VALUES of an iterable (array, string, Map, Set, ...)
const numbers = [10, 20, 30];
for (const n of numbers) console.log("number", n);

for (const ch of "Hi!") console.log("char", ch);

// Unicode: for...of walks by code point, indexing walks by UTF-16 unit
const word = "a😀b";
console.log("length:", word.length, "| for...of count:", [...word].length);
for (const ch of word) console.log("code point:", ch);

// Need the index too? entries() gives [index, value] pairs
const tasks = ["write", "test", "ship"];
for (const [i, task] of tasks.entries()) console.log(`${i + 1}. ${task}`);

// Map and Set are iterable (Chapter 8)
const stock = new Map([["apples", 5], ["pears", 0]]);
for (const [item, qty] of stock) console.log(item, "=", qty);
for (const tag of new Set(["js", "css", "js"])) console.log("tag", tag);

// Plain objects are NOT iterable — use Object.keys / values / entries
const scores = { ana: 90, ben: 72 };
try { for (const x of scores) {} } catch (e) { console.log("for...of on object ->", e.name + ":", e.message); }
for (const [name, score] of Object.entries(scores)) console.log(name, score);

// for...in vs for...of on the same array
const arr = ["x", "y"];
const ins = [], ofs = [];
for (const k in arr) ins.push(k);
for (const v of arr) ofs.push(v);
console.log("for...in ->", ins, "| for...of ->", ofs);

// The loop variable is a copy of the value: reassigning it doesn't change the array
const nums = [1, 2, 3];
for (let n of nums) n *= 10;
console.log("unchanged:", nums);
