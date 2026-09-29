// JSON.stringify and JSON.parse: options, lossy cases, and gotchas.
const order = {
  id: 42,
  customer: 'Ravi',
  total: 19.99,
  paid: true,
  coupon: null,
  items: [{ sku: 'A1', qty: 2 }],
  placedAt: new Date('2024-05-01T08:00:00Z'),
};

const text = JSON.stringify(order);
console.log(text);
console.log(typeof text, text.length);

// Pretty-print with an indent (number of spaces or a string)
console.log(JSON.stringify({ a: 1, b: [1, 2] }, null, 2));

// Replacer array = allow-list of keys; replacer function = transform/skip values
console.log(JSON.stringify(order, ['id', 'total']));
console.log(JSON.stringify(order, (key, value) => (key === 'customer' ? undefined : value)).slice(0, 60) + '...');

// What JSON cannot represent
const weird = {
  f() {}, u: undefined, s: Symbol('x'), nan: NaN, inf: Infinity, negZero: -0,
  map: new Map([['k', 1]]), set: new Set([1]), arr: [undefined, () => 1, NaN],
};
console.log(JSON.stringify(weird));
try { JSON.stringify({ big: 10n }); } catch (e) { console.log('BigInt ->', e.constructor.name); }
const loop = { name: 'me' }; loop.self = loop;
try { JSON.stringify(loop); } catch (e) { console.log('circular ->', e.constructor.name); }

// toJSON lets an object control its own serialization (Date has one built in)
const money = { cents: 1999, toJSON() { return (this.cents / 100).toFixed(2); } };
console.log(JSON.stringify({ price: money }));

// Parsing
const parsed = JSON.parse(text);
console.log(parsed.items[0].qty, typeof parsed.placedAt);          // dates come back as strings
const revived = JSON.parse(text, (key, value) => (key === 'placedAt' ? new Date(value) : value));
console.log(revived.placedAt instanceof Date, revived.placedAt.getUTCHours());

// JSON syntax is stricter than JS object literals
for (const bad of ["{'a': 1}", '{a: 1}', '{"a": 1,}', 'undefined', '']) {
  try { JSON.parse(bad); console.log('ok', bad); } catch (e) { console.log('SyntaxError for', JSON.stringify(bad)); }
}
console.log(JSON.parse('"just a string"'), JSON.parse('3.5'), JSON.parse('[true,null]'));

// Deep copy: JSON round-trip vs structuredClone
const src = { when: new Date(0), tags: new Set(['x']), n: 1 };
const viaJson = JSON.parse(JSON.stringify(src));
const viaClone = structuredClone(src);
console.log(viaJson, viaClone);

// Big integers lose precision when parsed as numbers
console.log(JSON.parse('{"id": 9007199254740993}').id);
// ES2026 "JSON.parse source text access": the reviver gets the original text (Node 24 supports it)
const exact = JSON.parse('{"id": 9007199254740993}', (key, value, ctx) =>
  key === 'id' ? BigInt(ctx.source) : value);
console.log(exact.id);
