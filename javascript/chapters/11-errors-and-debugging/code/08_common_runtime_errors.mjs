// A gallery of everyday runtime errors and their V8 messages (Node / Chrome / Edge wording).
const gallery = [
  ['call a non-function', () => { const user = {}; user.getName(); }],
  ['property of undefined', () => { const res = {}; return res.data.items; }],
  ['property of null', () => { const el = null; return el.textContent; }],
  ['set property on undefined', () => { let cfg; cfg.debug = true; }],
  ['undeclared variable', () => totl * 2],
  ['TDZ (use before let)', () => { const f = () => x; f(); let x = 1; }],
  ['reassign const', () => { const n = 1; n = 2; }],
  ['not iterable', () => { const [a] = {}; }],
  ['spread non-iterable', () => [...123]],
  ['JSON.parse(undefined)', () => JSON.parse(undefined)],
  ['JSON.parse(HTML)', () => JSON.parse('<!DOCTYPE html>')],
  ['class without new', () => { class Cart {} Cart(); }],
  ['reduce empty array', () => [].reduce((a, b) => a + b)],
  ['BigInt + Number', () => 1n + 1],
  ['bad array length', () => new Array(2 ** 32)],
  ['toFixed digits', () => (1.5).toFixed(101)],
  ['infinite recursion', () => { const f = () => f(); f(); }],
  ['circular JSON', () => { const a = {}; a.self = a; JSON.stringify(a); }],
  ['frozen object (strict)', () => { const o = Object.freeze({ a: 1 }); o.a = 2; }],
  ['in on a primitive', () => 'x' in 'hello'],
];
for (const [what, fn] of gallery) {
  try { fn(); console.log(`${what.padEnd(26)} (no error)`); }
  catch (e) { console.log(`${what.padEnd(26)} ${e.name}: ${e.message.split('\n')[0]}`); }
}
