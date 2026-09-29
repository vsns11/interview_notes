// Bitwise operators work on 32-bit integers.
let c = 5, d = 2;               // 5 = 101, 2 = 010
const bin = (v) => (v >>> 0).toString(2).padStart(3, "0");
console.log("c & d  =", c & d, bin(c & d));
console.log("c | d  =", c | d, bin(c | d));
console.log("c ^ d  =", c ^ d, bin(c ^ d));
console.log("~d     =", ~d);
console.log("c << 1 =", c << 1);
console.log("c >> 1 =", c >> 1, "| -5 >> 1 =", -5 >> 1, "(keeps sign)");
console.log("c >>> 1 =", c >>> 1, "| -5 >>> 1 =", -5 >>> 1, "(fills with 0)");
// Real use: flags packed into one number
const READ = 1, WRITE = 2, EXEC = 4;
let perms = READ | WRITE;
console.log("can write?", (perms & WRITE) !== 0, "| can exec?", (perms & EXEC) !== 0);

// Assignment operators modify a variable in place.
let e = 10;
const log = [];
e += 5;  log.push(e);  // 15
e -= 3;  log.push(e);  // 12
e *= 2;  log.push(e);  // 24
e /= 4;  log.push(e);  // 6
e %= 4;  log.push(e);  // 2
e **= 3; log.push(e);  // 8
e <<= 1; log.push(e);  // 16
e >>= 2; log.push(e);  // 4
e &= 6;  log.push(e);  // 4
e |= 1;  log.push(e);  // 5
e ^= 3;  log.push(e);  // 6
console.log("e after each step:", log.join(" -> "));
// Chained assignment evaluates right to left and returns the assigned value
let p, q;
p = q = 7;
console.log("p, q:", p, q);
