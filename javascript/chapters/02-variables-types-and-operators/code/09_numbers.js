// Numbers: IEEE-754 doubles, special values, Number properties and methods.
console.log("0.1 + 0.2 =", 0.1 + 0.2);
console.log("0.1 + 0.2 === 0.3 ?", 0.1 + 0.2 === 0.3);
console.log("close enough?", Math.abs(0.1 + 0.2 - 0.3) < Number.EPSILON);
console.log("money in cents: 10 + 20 =", (10 + 20) / 100);

console.log("MAX_VALUE        ", Number.MAX_VALUE);
console.log("MIN_VALUE        ", Number.MIN_VALUE);
console.log("MAX_SAFE_INTEGER ", Number.MAX_SAFE_INTEGER);
console.log("MIN_SAFE_INTEGER ", Number.MIN_SAFE_INTEGER);
console.log("EPSILON          ", Number.EPSILON);
console.log("POSITIVE_INFINITY", Number.POSITIVE_INFINITY, "| NEGATIVE_INFINITY", Number.NEGATIVE_INFINITY);
console.log("MAX_VALUE * 2    ", Number.MAX_VALUE * 2);
console.log("2**53 + 1        ", 2 ** 53 + 1, "(not representable!)");
console.log("isSafeInteger(2**53):", Number.isSafeInteger(2 ** 53));

// Literals
console.log("literals:", 1_000_000, 0xff, 0o17, 0b1010, 1.5e3, 2e-3, .5);

// Instance methods (return strings)
const num = 123.456;
console.log("toFixed(2):", num.toFixed(2), "| toFixed(0):", num.toFixed(0));
console.log("toPrecision(4):", num.toPrecision(4), "| toPrecision(2):", num.toPrecision(2));
console.log("(255).toString(2):", (255).toString(2), "| (255).toString(16):", (255).toString(16));
console.log("(1.005).toFixed(2):", (1.005).toFixed(2), "(binary rounding!)");
console.log("toLocaleString:", (1234567.891).toLocaleString("en-US"), "|", (1234567.891).toLocaleString("de-DE"));
console.log("valueOf of wrapper:", new Number(7).valueOf());

// Static methods
console.log("parseInt('123px'):", Number.parseInt("123px"), "| parseInt('px123'):", Number.parseInt("px123"));
console.log("parseInt('ff', 16):", Number.parseInt("ff", 16), "| parseInt('101', 2):", Number.parseInt("101", 2));
console.log("parseFloat('3.14abc'):", Number.parseFloat("3.14abc"));
console.log("Number('123px'):", Number("123px"), "| Number(''):", Number(""), "| Number(' 42 '):", Number(" 42 "));
console.log("Number(null):", Number(null), "| Number(undefined):", Number(undefined), "| Number(true):", Number(true));
console.log("isInteger(5.0):", Number.isInteger(5.0), "| isInteger(5.5):", Number.isInteger(5.5));
console.log("isFinite('5'):", isFinite("5"), "| Number.isFinite('5'):", Number.isFinite("5"));
console.log("isNaN('abc'):", isNaN("abc"), "| Number.isNaN('abc'):", Number.isNaN("abc"), "| Number.isNaN(NaN):", Number.isNaN(NaN));

// BigInt for integers beyond 2**53
const big = 2n ** 64n;
console.log("2n ** 64n =", big, "| typeof:", typeof big);
console.log("9007199254740993n + 1n =", 9007199254740993n + 1n);
console.log("7n / 2n =", 7n / 2n, "(truncates)");
try { 1n + 1; } catch (e) { console.log("1n + 1 ->", e.name + ":", e.message); }
console.log("BigInt(10) + 1n =", BigInt(10) + 1n, "| Number(5n) + 1 =", Number(5n) + 1);
