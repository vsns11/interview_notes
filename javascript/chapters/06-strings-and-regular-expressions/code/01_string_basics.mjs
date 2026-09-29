// Creating strings, escapes, template literals, indexing, immutability, primitives vs String objects.
const single = 'It works';
const double = "It works too";
const back = `Also works`;
console.log(single, '|', double, '|', back);

// Quotes inside strings: use the other kind, or escape
console.log("She said 'hi'", '|', 'He said "bye"', '|', 'It\'s escaped', '|', `mix 'both' "kinds"`);

// Escape sequences
console.log('Tab:\t|  Backslash: \\  Unicode: \u00e9 \u{1F600}  Hex: \x41');
console.log('Line 1\nLine 2');
console.log(JSON.stringify('a\r\nb\0c'), 'a\r\nb'.length);

// Template literals: interpolation, any expression, multi-line
const item = 'coffee', price = 3.5, qty = 2;
console.log(`${qty} x ${item} = $${(price * qty).toFixed(2)} ${qty > 1 ? '(bulk)' : ''}`);
const multi = `Dear user,
  your order has shipped.`;
console.log(multi);

// Tagged template: a function receives the literal parts and the values separately
function highlight(strings, ...values) {
  return strings.reduce((out, s, i) => out + s + (i < values.length ? `[${values[i]}]` : ''), '');
}
console.log(highlight`Hello ${'Ana'}, you have ${3} messages`);
console.log(String.raw`C:\new\table`, '|', 'C:\new\table'.length, String.raw`C:\new\table`.length);

// Length, indexing, at(), immutability
const greeting = 'Hello, World!';
console.log(greeting.length, greeting[0], greeting[7], greeting.at(-1), greeting[50], greeting.charAt(50) === '');
const s = 'cat';
try { s[0] = 'b'; } catch (e) { console.log('assigning s[0] ->', e.constructor.name); }
console.log(s, 'b' + s.slice(1));

// Concatenation and coercion
console.log('The number is ' + 5, '|', '5' + 3, '5' - 3, '5' * '2', 1 + 2 + '3', '1' + 2 + 3);
console.log(String(42), (42).toString(), `${null} ${undefined}`, String([1, 2]), String({}));

// Primitive vs String object
const obj = new String('Hello');
const prim = 'Hello';
console.log(typeof obj, typeof prim, obj === prim, obj == prim, obj.valueOf() === prim);
console.log(new String('a') === new String('a'), typeof String(123));
console.log(prim.toUpperCase(), 'methods work on primitives via temporary wrapper objects');

// Comparing strings: code-unit order vs locale order
console.log('apple' < 'banana', 'Zebra' < 'apple', 'a'.localeCompare('B'), ['é', 'z', 'e'].sort(), ['é', 'z', 'e'].sort((a, b) => a.localeCompare(b)));
