// The callback methods: forEach, map, filter, reduce, find*, some/every, flat/flatMap, and friends.
const orders = [
  { id: 1, customer: 'Ana', total: 120, status: 'paid' },
  { id: 2, customer: 'Ben', total: 35, status: 'pending' },
  { id: 3, customer: 'Ana', total: 60, status: 'paid' },
  { id: 4, customer: 'Cy', total: 250, status: 'refunded' },
];

// forEach: side effects only, returns undefined, cannot break early
orders.forEach((o, i) => console.log(i, o.id, o.customer));

// map: transform every element -> new array of the same length
console.log(orders.map((o) => o.total));
console.log(['1', '2', '3'].map(Number), ['1', '2', '3'].map(parseInt)); // classic trap

// filter: keep elements whose callback returns truthy
const paid = orders.filter((o) => o.status === 'paid');
console.log(paid.map((o) => o.id));

// reduce: fold everything into one value (always pass an initial value)
const revenue = paid.reduce((sum, o) => sum + o.total, 0);
const byCustomer = orders.reduce((acc, o) => { acc[o.customer] = (acc[o.customer] ?? 0) + o.total; return acc; }, {});
console.log(revenue, byCustomer);
try { [].reduce((a, b) => a + b); } catch (e) { console.log('empty reduce without initial ->', e.constructor.name); }
console.log(['a', 'b', 'c'].reduceRight((acc, x) => acc + x, ''));

// Chaining: a small pipeline
const report = orders
  .filter((o) => o.status !== 'refunded')
  .map((o) => ({ ...o, total: o.total * 1.1 }))
  .toSorted((a, b) => b.total - a.total)
  .map((o) => `${o.id}:${o.total.toFixed(2)}`)
  .join(', ');
console.log(report);

// find / findIndex / findLast / findLastIndex
console.log(orders.find((o) => o.total > 100).id, orders.findIndex((o) => o.total > 100));
console.log(orders.findLast((o) => o.customer === 'Ana').id, orders.findLastIndex((o) => o.customer === 'Ana'));
console.log(orders.find((o) => o.total > 1000), orders.findIndex((o) => o.total > 1000));

// some / every (short-circuit)
console.log(orders.some((o) => o.status === 'refunded'), orders.every((o) => o.total > 0), [].every(() => false));

// includes / indexOf / lastIndexOf (=== style, except NaN)
const vals = [1, 2, NaN, 2];
console.log(vals.includes(NaN), vals.indexOf(NaN), vals.indexOf(2), vals.lastIndexOf(2), vals.includes('2'));

// flat / flatMap
const nested = [1, [2, [3, [4]]]];
console.log(nested.flat(), nested.flat(2), nested.flat(Infinity));
const sentences = ['hello world', 'good morning'];
console.log(sentences.map((s) => s.split(' ')), sentences.flatMap((s) => s.split(' ')));
console.log([1, 2, 3, 4].flatMap((n) => (n % 2 ? [] : [n, n * 10])));   // filter + map in one pass

// keys / values / entries iterators, join, toString, toLocaleString
console.log([...['x', 'y'].keys()], [...['x', 'y'].values()], [...['x', 'y'].entries()]);
console.log([1, [2, 3], null, undefined, 4].join('-'), String([1, [2, 3]]), [1234.5, 99].toLocaleString('de-DE'));

// Callback signature: (element, index, array) and an optional thisArg
console.log(['a', 'b'].map((el, idx, arr) => `${el}${idx}/${arr.length}`));

// Early exit: forEach cannot break; use for...of, some, or find
let seen = 0;
for (const o of orders) { seen++; if (o.total > 100) break; }
console.log('stopped after', seen);
