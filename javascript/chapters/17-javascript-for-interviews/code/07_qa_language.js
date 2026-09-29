// 07_qa_language.js - real outputs for the "tricky questions" in section 17.4.
// Deliberately a CommonJS .js file in SLOPPY mode (no 'use strict') so that legacy
// behaviours (accidental globals, `this` defaulting to globalThis) can be shown.
const show = (label, fn) => {
  let out;
  try { out = fn(); } catch (e) { out = `${e.name}: ${e.message}`; }
  console.log(label.padEnd(34), typeof out === 'string' ? JSON.stringify(out) : out);
};

console.log('--- coercion and equality ---');
show("Q1  1 + '2'", () => 1 + '2');
show("Q1  '5' - 2", () => '5' - 2);
show("Q1  '3' * '4'", () => '3' * '4');
show("Q1  true + 1", () => true + 1);
show("Q1  [] + []", () => [] + []);
show("Q1  [] + {}", () => [] + {});
show("Q1  [1,2] + [3]", () => [1, 2] + [3]);
show("Q2  null == undefined", () => null == undefined);
show("Q2  null == 0", () => null == 0);
show("Q2  null >= 0", () => null >= 0);
show("Q2  '' == 0", () => '' == 0);
show("Q2  '0' == false", () => '0' == false);
show("Q3  [] == ![]", () => [] == ![]);
show("Q4  typeof null", () => typeof null);
show("Q5  NaN === NaN", () => NaN === NaN);
show("Q5  [NaN].includes(NaN)", () => [NaN].includes(NaN));
show("Q5  [NaN].indexOf(NaN)", () => [NaN].indexOf(NaN));
show("Q5  typeof NaN", () => typeof NaN);
show("Q6  Object.is(-0, 0) / -0 === 0", () => `${Object.is(-0, 0)} / ${-0 === 0}`);
show("Q7  typeof [] / function / class", () => `${typeof []} / ${typeof function () {}} / ${typeof class {}}`);
show("Q7  typeof undeclaredVar", () => typeof undeclaredVar);
show("Q8  Boolean([]), Boolean('0')", () => `${Boolean([])}, ${Boolean('0')}`);
show("Q8  !!new Boolean(false)", () => !!new Boolean(false));
show("Q9  'b' + 'a' + +'a' + 'a'", () => 'b' + 'a' + +'a' + 'a');
show("Q10 ['1','7','11'].map(parseInt)", () => ['1', '7', '11'].map(parseInt));
show("Q10 ['1','7','11'].map(Number)", () => ['1', '7', '11'].map(Number));
show("Q11 [10, 1, 2].sort()", () => [10, 1, 2].sort());
show("Q12 0 || 'x' / 0 ?? 'x'", () => `${0 || 'x'} / ${0 ?? 'x'}`);
show("Q13 {} === {}", () => ({}) === ({}));
show("Q14 0.1 + 0.2 === 0.3", () => 0.1 + 0.2 === 0.3);
show("Q14 Math.max() < Math.min()", () => Math.max() < Math.min());

console.log('--- scope, hoisting, closures ---');
show('Q16 var read before declaration', () => { const r = typeof hoisted; var hoisted = 1; return r; });
show('Q16 let read before declaration', () => { tdz; let tdz = 1; });
show('Q16 typeof in the TDZ', () => { typeof inTdz; let inTdz = 1; });
show('Q17 declaration hoisted', () => { return early(); function early() { return 'works'; } });
show('Q17 expression not hoisted', () => { const r = late(); var late = function () { return 'x'; }; return r; });
show('Q20 let a = b = 5', () => { (function () { let a = b = 5; })(); return `outside: typeof a=${typeof a}, typeof b=${typeof b}`; });
delete globalThis.b;
show('Q21 var in block leaks', () => { if (true) { var v = 1; let l = 2; } return `v=${v}, typeof l=${typeof l}`; });
show('Q19 counter via closure', () => {
  const makeCounter = () => { let n = 0; return () => ++n; };
  const c1 = makeCounter(), c2 = makeCounter();
  c1(); c1();
  return `c1: ${c1()}, c2: ${c2()}`;
});

console.log('--- this, prototypes, classes ---');
const user = {
  name: 'Ada',
  regular() { return this?.name; },
  arrow: () => typeof this,                 // `this` of the module scope (module.exports object)
  delayed() { return [function () { return this === globalThis; }(), (() => this.name)()]; },
};
show('Q23 method call', () => user.regular());
show('Q23 extracted method (sloppy)', () => { const f = user.regular; return f(); });
show('Q24 arrow in object literal', () => user.arrow());
show('Q25 inner function vs arrow', () => user.delayed());
show('Q26 call / apply / bind', () => {
  function intro(greeting, mark) { return `${greeting} ${this.name}${mark}`; }
  const p = { name: 'Lin' };
  return [intro.call(p, 'Hi', '!'), intro.apply(p, ['Yo', '?']), intro.bind(p, 'Hey')('.')].join(' | ');
});
show('Q27 bind twice', () => { function f() { return this.v; } return f.bind({ v: 1 }).bind({ v: 2 })(); });
show('Q28 new returns object?', () => { function F() { this.a = 1; return { b: 2 }; } return JSON.stringify(new F()); });
show('Q28 new returns primitive?', () => { function F() { this.a = 1; return 5; } return JSON.stringify(new F()); });
function Animal(name) { this.name = name; }
Animal.prototype.speak = function () { return `${this.name} makes a sound`; };
const rex = new Animal('Rex');
show('Q29 proto links', () => `${Object.getPrototypeOf(rex) === Animal.prototype} ${Animal.prototype.constructor === Animal} ${Object.getPrototypeOf(Animal.prototype) === Object.prototype}`);
show('Q29 own vs inherited', () => `${Object.hasOwn(rex, 'name')} ${Object.hasOwn(rex, 'speak')} ${'speak' in rex}`);
show('Q30 instanceof after proto swap', () => { const o = {}; Object.setPrototypeOf(o, Animal.prototype); return o instanceof Animal; });
show('Q31 class before declaration', () => { new Later(); class Later {} });
show('Q31 class without new', () => { class K {} return K(); });
show('Q32 Object.create(null)', () => { const d = Object.create(null); return `${typeof d.toString} ${'toString' in {}}`; });
show('Q33 #private vs closure', () => {
  class Wallet { #cents = 0; add(c) { this.#cents += c; return this.#cents; } }
  const w = new Wallet(); w.add(5);
  return `${w.add(5)} ${w.cents} ${Object.keys(w).length} ${JSON.stringify(w)}`;
});
show('Q34 this before super()', () => { class A {} class B extends A { constructor() { this.x = 1; super(); } } return new B(); });

console.log('--- objects, arrays, copies ---');
show('Q43 spread copy is shallow', () => { const a = { n: { v: 1 } }; const b = { ...a }; b.n.v = 2; return a.n.v; });
show('Q44 const + mutate', () => { const o = { v: 1 }; o.v = 2; return o.v; });
show('Q44 freeze is shallow', () => { const o = Object.freeze({ inner: { v: 1 } }); o.inner.v = 2; o.x = 1; return `${o.inner.v} ${o.x}`; });
show('Q45 object keys become strings', () => { const o = {}; o[{}] = 'a'; o[{ id: 2 }] = 'b'; o[1] = 'c'; return JSON.stringify(Object.keys(o)) + ' ' + o['[object Object]']; });
show('Q46 array holes', () => { const a = [1, 2, 3]; delete a[1]; return `${a.length} ${JSON.stringify(a)} ${1 in a}`; });
show('Q46 length truncates', () => { const a = [1, 2, 3]; a.length = 1; return JSON.stringify(a); });
show('Q46 length of sparse', () => { const a = []; a[5] = 'x'; return a.length; });
show('Q47 reassign param', () => { const o = { v: 1 }; (function (p) { p = { v: 99 }; })(o); (function (p) { p.v = 2; })(o); return o.v; });
show('Q49 JSON drops', () => JSON.stringify({ u: undefined, f() {}, s: Symbol('x'), n: NaN, d: new Date(0) }));
show('Q49 JSON array holes', () => JSON.stringify([undefined, () => 1]));

show('Q50 for...in vs for...of', () => {
  const arr = ['a', 'b']; arr.extra = 'x';
  const ins = [], ofs = [];
  for (const k in arr) ins.push(k);
  for (const v of arr) ofs.push(v);
  return `in: ${ins.join(',')} | of: ${ofs.join(',')}`;
});

// Q18 closures in loops needs timers
const out = { var: [], let: [], fixed: [] };
for (var i = 0; i < 3; i++) setTimeout(() => out.var.push(i), 0);
for (let j = 0; j < 3; j++) setTimeout(() => out.let.push(j), 0);
for (var k = 0; k < 3; k++) ((k2) => setTimeout(() => out.fixed.push(k2), 0))(k);
setTimeout(() => console.log('Q18 closures in loops'.padEnd(34), JSON.stringify(out)), 5);

// Q37 in a CommonJS main script: nextTick queue first, then promise microtasks, then check phase
const q37 = [];
setImmediate(() => q37.push('immediate'));
Promise.resolve().then(() => q37.push('then'));
process.nextTick(() => q37.push('nextTick'));
q37.push('sync');
setTimeout(() => console.log('Q37 CommonJS main'.padEnd(34), q37.join(' ')), 10);
