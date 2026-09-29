// Booleans, truthy and falsy values.
const falsy = [["false", false], ["0", 0], ["-0", -0], ["0n", 0n], ['""', ""], ["null", null], ["undefined", undefined], ["NaN", NaN]];
console.log("falsy:", falsy.map(([label, v]) => `${label}->${Boolean(v)}`).join("  "));
const truthy = ["0", "false", " ", [], {}, -1, Infinity, function () {}, new Boolean(false)];
console.log("truthy:", truthy.map((v) => Boolean(v)).every(Boolean));

if (false || 0 || "" || null || undefined || NaN) {
  console.log("never printed");
} else {
  console.log("All values are falsy.");
}
// Converting to boolean
console.log('Boolean("hi"):', Boolean("hi"), '| !!"":', !!"", "| !!42:", !!42);
// Boolean wrapper objects: always truthy, even when wrapping false!
const wrapped = new Boolean(false);
console.log("typeof wrapped:", typeof wrapped, "| valueOf():", wrapped.valueOf());
console.log("if (new Boolean(false)) runs?", wrapped ? "yes (object is truthy)" : "no");
console.log("new Boolean(true).valueOf():", new Boolean(true).valueOf(), "| toString():", new Boolean(true).toString());
// Comparisons produce booleans
const age = 20;
const isAdult = age >= 18;
console.log("isAdult:", isAdult, typeof isAdult);
// Arithmetic on booleans converts them to 1 / 0
console.log("true + true =", true + true, "| [true, false, true].filter(Boolean).length =", [true, false, true].filter(Boolean).length);
