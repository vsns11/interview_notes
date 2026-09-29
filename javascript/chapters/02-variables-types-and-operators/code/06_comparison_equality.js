// Comparison operators, == vs ===, Object.is.
const x = 5, y = "5";
console.log("x == y  :", x == y);
console.log("x === y :", x === y);
console.log("x != y  :", x != y);
console.log("x !== y :", x !== y);
console.log("x > 2, x < 10, x >= 5, x <= 5:", x > 2, x < 10, x >= 5, x <= 5);

console.log("--- loose equality surprises ---");
const pairs = [
  [0, ""], [0, "0"], ["", "0"], [false, "0"], [false, ""], [null, undefined],
  [null, 0], [undefined, 0], [NaN, NaN], [[1], 1], [[], ""], [[1, 2], "1,2"],
];
for (const [l, r] of pairs) {
  const show = (v) => (typeof v === "string" ? JSON.stringify(v) : Array.isArray(v) ? `[${v}]` : String(v));
  console.log(`${show(l).padEnd(9)} == ${show(r).padEnd(9)} -> ${String(l == r).padEnd(5)} | === -> ${l === r}`);
}
console.log("--- relational comparisons ---");
console.log('"10" < "9"  :', "10" < "9", "(both strings: compared character by character)");
console.log('"10" < 9    :', "10" < 9, "(one number: string converted to number)");
console.log('"apple" < "banana":', "apple" < "banana", '| "Z" < "a":', "Z" < "a");
console.log("null >= 0   :", null >= 0, "| null == 0:", null == 0);
console.log("NaN < 1, NaN >= 1:", NaN < 1, NaN >= 1);
console.log("--- Object.is ---");
console.log("NaN === NaN:", NaN === NaN, "| Object.is(NaN, NaN):", Object.is(NaN, NaN));
console.log("0 === -0:", 0 === -0, "| Object.is(0, -0):", Object.is(0, -0));
