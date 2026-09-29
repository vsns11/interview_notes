// Explicit type conversion — the three target types.
const values = ["42", "", " ", "4px", "0x1f", null, undefined, true, false, [], [7], [1, 2], {}];
const show = (v) => (typeof v === "string" ? JSON.stringify(v) : Array.isArray(v) ? `[${v}]` : v && typeof v === "object" ? "{}" : String(v));
console.log("value".padEnd(10), "Number()".padEnd(10), "String()".padEnd(18), "Boolean()");
for (const v of values) {
  console.log(show(v).padEnd(10), String(Number(v)).padEnd(10), JSON.stringify(String(v)).padEnd(18), Boolean(v));
}
console.log("parseInt vs Number on '08.5kg':", parseInt("08.5kg", 10), Number("08.5kg"));
console.log("String(123) + 1 =", String(123) + 1, "| `${123}`:", `${123}`);
