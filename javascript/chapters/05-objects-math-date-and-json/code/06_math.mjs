// The Math namespace: constants, rounding, min/max, powers, trig, random numbers.
console.log(typeof Math, Object.prototype.toString.call(Math));
try { new Math(); } catch (e) { console.log('new Math() ->', e.constructor.name); }

// Constants
console.log(Math.PI, Math.E, Math.SQRT2, Math.SQRT1_2, Math.LN2, Math.LN10, Math.LOG2E, Math.LOG10E);

// Area of a circle of radius 5
const r = 5;
console.log('area', Math.PI * r ** 2, Math.PI * Math.pow(r, 2));

// Rounding family: how each treats positives and negatives
const rows = [3.56, 3.5, -3.5, -3.56, 2.4].map((x) => ({
  x, round: Math.round(x), floor: Math.floor(x), ceil: Math.ceil(x), trunc: Math.trunc(x),
}));
console.table(rows);

// Rounding to N decimals; toFixed returns a STRING and is affected by binary floats
console.log(Math.round(1.2345 * 100) / 100, (1.005).toFixed(2), (1.2345).toFixed(2));

// min / max, with arrays via spread; empty input edge cases
const temps = [18, 25, 21, 30, 17];
console.log(Math.max(10, 20, 30, 40, 50), Math.min(...temps), Math.max(...temps));
console.log(Math.max(), Math.min(), Math.max(1, 'x'));

// Clamp a value into a range (no built-in clamp)
const clamp = (v, lo, hi) => Math.min(Math.max(v, lo), hi);
console.log(clamp(120, 0, 100), clamp(-5, 0, 100), clamp(42, 0, 100));

// Powers, roots, logs, sign, abs
console.log(Math.abs(-7), Math.sign(-7), Math.sqrt(81), Math.cbrt(27), Math.hypot(3, 4));
console.log(Math.pow(2, 10), 2 ** 10, Math.exp(1), Math.log(Math.E), Math.log10(1000), Math.log2(8));

// Trig works in RADIANS
const toRad = (deg) => (deg * Math.PI) / 180;
console.log(Math.sin(toRad(30)).toFixed(4), Math.cos(toRad(60)).toFixed(4), Math.sin(Math.PI));
console.log('angle of (1,1):', (Math.atan2(1, 1) * 180) / Math.PI, 'deg');

// Random numbers: [0, 1). Integer in [min, max] inclusive:
const randInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const counts = { 1: 0, 2: 0, 3: 0 };
for (let i = 0; i < 30000; i++) counts[randInt(1, 3)]++;
console.log('all in range:', Object.keys(counts).join(','), 'roughly even:',
  Object.values(counts).every((c) => Math.abs(c - 10000) < 600));
const x = Math.random();
console.log('0 <= x < 1:', x >= 0 && x < 1);
// For tokens/passwords use crypto, not Math.random:
console.log('crypto uuid length:', crypto.randomUUID().length);
