// The 7 primitive types + objects, and what typeof reports.
const samples = [
  ["number", 42], ["number (float)", 3.14], ["NaN", NaN], ["bigint", 10n],
  ["string", "hi"], ["boolean", true], ["undefined", undefined], ["null", null],
  ["symbol", Symbol("id")], ["object", { a: 1 }], ["array", [1, 2]],
  ["function", function () {}], ["date", new Date(0)],
];
for (const [label, value] of samples) {
  console.log(label.padEnd(15), "typeof ->", typeof value);
}
console.log("Array.isArray([1,2]) ->", Array.isArray([1, 2]));
console.log("value === null      ->", null === null);
// Dynamic typing: the variable has no type; the value does.
let box = 5;      console.log("box:", typeof box);
box = "five";     console.log("box:", typeof box);
