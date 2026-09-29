// cheatsheet_checks.mjs - asserts every concrete value / behaviour quoted on the Chapter 18 cheat sheets.
// Run: node cheatsheet_checks.mjs   (Node 24; the DOM sheet uses jsdom via tools_dom.mjs)
import '../../../../tools_dom.mjs';
import assert from 'node:assert/strict';
import { EventEmitter, once } from 'node:events';
import path from 'node:path';
import { Readable, Transform } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import { setTimeout as sleep } from 'node:timers/promises';
import { parseArgs } from 'node:util';

const results = [];
async function sheet(name, fn) {
  try { await fn(); results.push(name); console.log('OK  ', name); }
  catch (e) { console.log('FAIL', name, '-', e.message); process.exitCode = 1; }
}

// ---------- 18.1 Syntax, variables and types ----------
await sheet('18.1 syntax & types', () => {
  assert.equal(typeof 42, 'number');
  assert.equal(typeof 10n, 'bigint');
  assert.equal(typeof 'hi', 'string');
  assert.equal(typeof true, 'boolean');
  assert.equal(typeof undefined, 'undefined');
  assert.equal(typeof null, 'object');           // the famous quirk
  assert.equal(typeof Symbol('id'), 'symbol');
  assert.equal(typeof [], 'object');
  assert.equal(typeof function () {}, 'function');
  assert.equal(typeof class {}, 'function');
  assert.equal(typeof NaN, 'number');
  assert.equal(typeof notDeclaredAnywhere, 'undefined');
  assert.equal(Array.isArray([1, 2]), true);
  // TDZ
  assert.throws(() => { tdz; let tdz = 1; }, ReferenceError);
  // const: no reassignment, but contents can change
  const obj = { n: 1 }; obj.n = 2; assert.equal(obj.n, 2);
  assert.throws(() => { const c = 1; c = 2; }, TypeError);
  // var is function-scoped and hoisted as undefined
  assert.equal((function () { const before = v; var v = 5; return before; })(), undefined);
  // primitives by value, objects by reference
  let a = 1; let b = a; b++; assert.equal(a, 1);
  const o1 = { x: 1 }; const o2 = o1; o2.x = 9; assert.equal(o1.x, 9);
  assert.equal({} === {}, false);
});

// ---------- 18.2 Operators & coercion ----------
await sheet('18.2 operators & coercion', () => {
  assert.equal(1 + '2', '12');
  assert.equal('5' - 2, 3);
  assert.equal('3' * '4', 12);
  assert.equal(true + 1, 2);
  assert.equal([] + [], '');
  assert.equal([] + {}, '[object Object]');
  assert.equal([1, 2] + [3], '1,23');
  assert.equal(7 / 2, 3.5);
  assert.equal(-7 % 3, -1);                      // remainder keeps the left sign
  assert.equal(((-7 % 3) + 3) % 3, 2);           // true modulo
  assert.equal(2 ** 10, 1024);
  let n = 5; assert.equal(n++, 5); assert.equal(n, 6); assert.equal(++n, 7);
  // == table
  assert.equal(0 == '', true);
  assert.equal(0 == '0', true);
  assert.equal('' == '0', false);
  assert.equal(false == '0', true);
  assert.equal(null == undefined, true);
  assert.equal(null == 0, false);
  assert.equal(null >= 0, true);
  assert.equal(NaN == NaN, false);
  assert.equal([1] == 1, true);
  assert.equal([] == ![], true);
  assert.equal('2' == true, false);
  assert.equal(1n == 1, true);
  assert.equal('10' < '9', true);
  assert.equal('10' < 9, false);
  assert.equal('Z' < 'a', true);
  assert.equal(Object.is(NaN, NaN), true);
  assert.equal(Object.is(0, -0), false);
  assert.equal(0 === -0, true);
  // conversions table
  const num = (v) => Number(v);
  assert.equal(num(''), 0); assert.equal(num(' '), 0); assert.equal(num('42'), 42);
  assert.ok(Number.isNaN(num('4px'))); assert.equal(num('0x1f'), 31);
  assert.equal(num(null), 0); assert.ok(Number.isNaN(num(undefined)));
  assert.equal(num(true), 1); assert.equal(num([]), 0); assert.equal(num([7]), 7);
  assert.ok(Number.isNaN(num([1, 2]))); assert.ok(Number.isNaN(num({})));
  assert.equal(String([]), ''); assert.equal(String([1, 2]), '1,2'); assert.equal(String({}), '[object Object]');
  assert.equal(String(null), 'null');
  assert.equal(parseInt('08.5kg', 10), 8);
  assert.ok(Number.isNaN(Number('08.5kg')));
  // falsy list
  for (const f of [false, 0, -0, 0n, '', null, undefined, NaN]) assert.equal(Boolean(f), false);
  for (const t of [[], {}, '0', 'false', new Boolean(false), -1, Infinity]) assert.equal(Boolean(t), true);
  // && || ?? ?.
  assert.equal(0 || 'x', 'x');
  assert.equal(0 ?? 'x', 0);
  assert.equal('' ?? 'x', '');
  assert.equal(null ?? 'x', 'x');
  assert.equal('a' && 'b', 'b');
  assert.equal(0 && 'b', 0);
  const u = null; assert.equal(u?.deep.prop, undefined);
  let cfg = {}; cfg.port ??= 8080; cfg.port ??= 9; assert.equal(cfg.port, 8080);
  // numbers
  assert.equal(0.1 + 0.2 === 0.3, false);
  assert.equal(0.1 + 0.2, 0.30000000000000004);
  assert.equal(Number.MAX_SAFE_INTEGER, 2 ** 53 - 1);
  assert.equal(2 ** 53 === 2 ** 53 + 1, true);
  assert.equal((123.456).toFixed(2), '123.46');
  assert.equal((255).toString(16), 'ff');
  assert.equal(isNaN('abc'), true);
  assert.equal(Number.isNaN('abc'), false);
  assert.equal(1 / 0, Infinity);
  assert.equal(Math.max() < Math.min(), true);
  assert.equal(~5, -6);
  assert.equal(5 & 3, 1); assert.equal(5 | 3, 7); assert.equal(5 ^ 3, 6);
  assert.equal(1_000_000, 1000000);
  assert.throws(() => eval('null || undefined ?? 1'), SyntaxError);
});

// ---------- 18.3 Control flow ----------
await sheet('18.3 control flow', () => {
  const pick = (v) => { switch (v) { case 1: return 'num'; case '1': return 'str'; default: return 'none'; } };
  assert.equal(pick('1'), 'str');                // switch uses ===
  const fall = []; switch (1) { case 1: fall.push(1); case 2: fall.push(2); break; case 3: fall.push(3); }
  assert.deepEqual(fall, [1, 2]);                // fall-through without break
  const keys = []; for (const k in ['a', 'b']) keys.push(k);
  assert.deepEqual(keys, ['0', '1']);            // for...in gives string keys
  const vals = []; for (const v of ['a', 'b']) vals.push(v);
  assert.deepEqual(vals, ['a', 'b']);
  let runs = 0; do { runs++; } while (false);
  assert.equal(runs, 1);
  const found = [];
  outer: for (let i = 0; i < 3; i++) { for (let j = 0; j < 3; j++) { if (j === 1) continue outer; if (i === 2) break outer; found.push(`${i}${j}`); } }
  assert.deepEqual(found, ['00', '10']);
  const fnsVar = []; for (var i = 0; i < 3; i++) fnsVar.push(() => i);
  const fnsLet = []; for (let k = 0; k < 3; k++) fnsLet.push(() => k);
  assert.deepEqual(fnsVar.map((f) => f()), [3, 3, 3]);
  assert.deepEqual(fnsLet.map((f) => f()), [0, 1, 2]);
});

// ---------- 18.4 Functions, closures, this ----------
await sheet('18.4 functions/closures/this', () => {
  assert.equal(hoisted(), 'works'); function hoisted() { return 'works'; }
  const f = (a, b = 2, ...rest) => [a, b, rest];
  assert.deepEqual(f(1), [1, 2, []]);
  assert.deepEqual(f(1, undefined, 3, 4), [1, 2, [3, 4]]);
  assert.deepEqual(f(1, null), [1, null, []]);   // defaults only replace undefined
  assert.equal(((a, b = 1, c) => 0).length, 1);
  assert.equal((() => ({ x: 1 }))().x, 1);
  assert.throws(() => new (() => {})(), TypeError);
  function counter() { let c = 0; return () => ++c; }
  const c1 = counter(); c1(); c1(); const c2 = counter();
  assert.equal(c1(), 3); assert.equal(c2(), 1);
  // this rules (module code is strict)
  const user = { name: 'Ada', hi() { return this?.name; } };
  assert.equal(user.hi(), 'Ada');
  const lost = user.hi; assert.equal(lost(), undefined);
  assert.equal(user.hi.call({ name: 'Lin' }), 'Lin');
  const bound = user.hi.bind({ name: 'B1' }).bind({ name: 'B2' });
  assert.equal(bound(), 'B1');                   // bind twice: first wins
  const box = { v: 42, arrow() { return [1].map(() => this.v)[0]; } };
  assert.equal(box.arrow(), 42);
  function Pt(x) { this.x = x; } assert.equal(new Pt(3).x, 3);
  function R() { this.a = 1; return { b: 2 }; } assert.deepEqual(new R(), { b: 2 });
  function P() { this.a = 1; return 5; } assert.deepEqual({ ...new P() }, { a: 1 });
  assert.equal(Math.max.apply(null, [1, 5, 2]), 5);
  assert.deepEqual(['1', '7', '11'].map(parseInt), [1, NaN, 3]);
  const fact = (n) => (n <= 1 ? 1 : n * fact(n - 1)); assert.equal(fact(5), 120);
  assert.throws(() => { const r = () => r(); r(); }, RangeError);
});

// ---------- 18.5 Objects, Math, Date, JSON ----------
await sheet('18.5 objects/Math/Date/JSON', () => {
  const key = 'dyn';
  const o = { b: 1, 2: 'two', a: 2, 1: 'one', [key]: 3 };
  assert.deepEqual(Object.keys(o), ['1', '2', 'b', 'a', 'dyn']);   // integer keys first
  const { a, b: renamed, missing = 'def' } = o;
  assert.deepEqual([a, renamed, missing], [2, 1, 'def']);
  const shallow = { ...{ inner: { n: 1 } } }; const src = { inner: { n: 1 } };
  const sh = { ...src }; sh.inner.n = 9; assert.equal(src.inner.n, 9);
  const deep = structuredClone(src); deep.inner.n = 1; assert.equal(src.inner.n, 9);
  assert.deepEqual({ ...{ x: 1, y: 1 }, ...{ y: 2 } }, { x: 1, y: 2 });   // later keys win
  assert.equal('toString' in {}, true);
  assert.equal(Object.hasOwn({}, 'toString'), false);
  const fr = Object.freeze({ n: { deep: 1 } });
  assert.throws(() => { 'use strict'; fr.n = 2; }, TypeError);
  fr.n.deep = 2; assert.equal(fr.n.deep, 2);     // freeze is shallow
  assert.equal(Object.isSealed(Object.seal({})), true);
  assert.deepEqual(Object.fromEntries([['a', 1]]), { a: 1 });
  // Math
  assert.equal(Math.round(2.5), 3); assert.equal(Math.round(-2.5), -2);
  assert.equal(Math.floor(-1.5), -2); assert.equal(Math.trunc(-1.5), -1); assert.equal(Math.ceil(1.1), 2);
  assert.equal(Math.max(...[3, 9, 1]), 9);
  assert.equal(Math.max(), -Infinity);
  // Date
  const d = new Date(2024, 0, 31);
  assert.equal(d.getMonth(), 0);
  d.setMonth(1);                                  // Feb 31 rolls over into March
  assert.equal(d.getMonth(), 2);
  assert.equal(new Date(0).toISOString(), '1970-01-01T00:00:00.000Z');
  assert.equal(new Date('2024-03-10').getUTCHours(), 0);   // date-only ISO = UTC
  assert.equal(new Date(Date.UTC(2024, 2, 10)).getUTCDay(), 0);  // Sunday
  // JSON
  assert.equal(JSON.stringify({ u: undefined, f() {}, n: NaN, d: new Date(0), m: new Map([[1, 2]]) }),
    '{"n":null,"d":"1970-01-01T00:00:00.000Z","m":{}}');
  assert.equal(JSON.stringify([undefined, () => 1]), '[null,null]');
  assert.equal(JSON.stringify(undefined), undefined);
  assert.throws(() => JSON.stringify({ big: 1n }), TypeError);
  const cyc = {}; cyc.self = cyc; assert.throws(() => JSON.stringify(cyc), TypeError);
  assert.throws(() => JSON.parse("{'a':1}"), SyntaxError);
  assert.equal(JSON.stringify({ a: [1] }, null, 2), '{\n  "a": [\n    1\n  ]\n}');
  const revived = JSON.parse('{"at":"2024-01-01T00:00:00.000Z"}', (k, v) => (k === 'at' ? new Date(v) : v));
  assert.ok(revived.at instanceof Date);
  assert.deepEqual(shallow, { inner: { n: 1 } });
});

// ---------- 18.6 Classes & prototypes ----------
await sheet('18.6 classes & prototypes', () => {
  class Animal {
    #secret = 's';
    static count = 0;
    static { Animal.ready = true; }
    constructor(name) { this.name = name; Animal.count++; }
    speak() { return `${this.name} makes a sound`; }
    get upper() { return this.name.toUpperCase(); }
    static isAnimal(x) { return #secret in x; }
  }
  class Dog extends Animal { speak() { return `${super.speak()} - woof`; } }
  const d = new Dog('Rex');
  assert.equal(d.speak(), 'Rex makes a sound - woof');
  assert.equal(d.upper, 'REX');
  assert.equal(Animal.isAnimal(d), true);
  assert.equal(Animal.isAnimal({}), false);
  assert.equal(Animal.ready, true);
  assert.equal(Object.getPrototypeOf(d), Dog.prototype);
  assert.equal(Object.getPrototypeOf(Dog.prototype), Animal.prototype);
  assert.equal(d instanceof Animal, true);
  assert.deepEqual(Object.keys(d), ['name']);    // methods are non-enumerable, #private hidden
  assert.throws(() => Animal('x'), TypeError);   // class needs new
  assert.throws(() => { class B extends Animal { constructor() { this.x = 1; super('b'); } } new B(); }, ReferenceError);
  const bare = Object.create(null); assert.equal('toString' in bare, false);
  assert.equal(Object.prototype.toString.call(new Date(0)), '[object Date]');
  assert.equal(Error.isError(new TypeError('x')), true);
  assert.equal(Object.getPrototypeOf(Object.prototype), null);
});

// ---------- 18.7 Strings & regex ----------
await sheet('18.7 strings & regex', () => {
  const s = 'Hello';
  assert.equal(s.at(-1), 'o'); assert.equal(s[10], undefined); assert.equal(s.charAt(10), '');
  assert.equal(s.slice(-3), 'llo'); assert.equal(s.slice(1, 3), 'el');
  assert.equal('a-b-c'.replace('-', '+'), 'a+b-c');
  assert.equal('a-b-c'.replaceAll('-', '+'), 'a+b+c');
  assert.equal('5'.padStart(3, '0'), '005');
  assert.deepEqual('a,b,,c'.split(','), ['a', 'b', '', 'c']);
  assert.equal('😀'.length, 2); assert.equal([...'😀'].length, 1);
  assert.equal([...new Intl.Segmenter().segment('👩‍👩‍👧')].length, 1);
  assert.equal('é'.normalize('NFC') === 'é'.normalize('NFC'), true);
  assert.equal(`${1 + 1}`, '2');
  assert.equal('a' < 'b', true); assert.equal(['b', 'a', 'C'].sort().join(''), 'Cab');
  assert.equal(['b', 'a', 'C'].sort((x, y) => x.localeCompare(y, 'en')).join(''), 'abC');
  assert.equal(/^\d{3}-\d{4}$/.test('555-1234'), true);
  assert.equal('x'.match(/y/), null);
  assert.deepEqual('a1b22'.match(/\d+/g), ['1', '22']);
  const m = '2024-03-10'.match(/(?<y>\d{4})-(?<mo>\d\d)-(?<d>\d\d)/);
  assert.equal(m.groups.y, '2024');
  assert.equal('2024-03-10'.replace(/(\d+)-(\d+)-(\d+)/, '$3/$2/$1'), '10/03/2024');
  assert.equal('Ann and Bob'.replace(/\b\w/g, (c) => c.toLowerCase()), 'ann and bob');
  assert.deepEqual([...'a1b2'.matchAll(/\d/g)].map((x) => x.index), [1, 3]);
  assert.throws(() => 'a1'.matchAll(/\d/), TypeError);
  const g = /a/g; assert.equal(g.test('aa'), true); assert.equal(g.lastIndex, 1);
  assert.equal(RegExp.escape('1.5*').length > 4, true);
  assert.equal(new RegExp(RegExp.escape('1.5*')).test('x1.5*'), true);
  assert.equal('<b>x</b>'.replace(/<.+>/, ''), '');       // greedy
  assert.equal('<b>x</b>'.replace(/<.+?>/, ''), 'x</b>'); // lazy
  assert.equal(/\p{L}+/u.test('Ωmega'), true);
});

// ---------- 18.8 Arrays ----------
await sheet('18.8 arrays', () => {
  const arr = [3, 1, 2];
  const sorted = arr.sort(); assert.equal(sorted, arr);          // sort mutates, returns same array
  const base = [3, 1, 2];
  assert.deepEqual(base.toSorted(), [1, 2, 3]);
  assert.deepEqual(base.toReversed(), [2, 1, 3]);
  assert.deepEqual(base.toSpliced(1, 1, 'x'), [3, 'x', 2]);
  assert.deepEqual(base.with(-1, 7), [3, 1, 7]);
  assert.deepEqual(base, [3, 1, 2]);
  assert.throws(() => base.with(10, 1), RangeError);
  const sp = ['a', 'b', 'c', 'd']; assert.deepEqual(sp.splice(1, 2), ['b', 'c']); assert.deepEqual(sp, ['a', 'd']);
  assert.equal([1].push(2), 2);                                  // push returns new length
  assert.deepEqual([10, 1, 2].sort(), [1, 10, 2]);
  assert.deepEqual([10, 1, 2].sort((x, y) => x - y), [1, 2, 10]);
  assert.equal([1, 2, 3].reduce((s, x) => s + x, 0), 6);
  assert.throws(() => [].reduce((s, x) => s + x), TypeError);
  assert.equal([NaN].includes(NaN), true); assert.equal([NaN].indexOf(NaN), -1);
  assert.equal([1, 2, 3].find((x) => x > 1), 2); assert.equal([1, 2, 3].findLast((x) => x > 1), 3);
  assert.equal([1, 2, 3].findIndex((x) => x > 5), -1);
  assert.deepEqual([1, [2, [3, [4]]]].flat(), [1, 2, [3, [4]]]);
  assert.deepEqual([1, [2, [3, [4]]]].flat(Infinity), [1, 2, 3, 4]);
  assert.deepEqual(['a b', 'c'].flatMap((s) => s.split(' ')), ['a', 'b', 'c']);
  assert.equal([1, 2].some((x) => x > 1), true); assert.equal([].every((x) => x > 1), true);
  assert.deepEqual(Array.from({ length: 3 }, (_, i) => i * 2), [0, 2, 4]);
  assert.deepEqual(Array.of(3), [3]); assert.equal(new Array(3).length, 3);
  const shared = Array(2).fill([]); shared[0].push('x'); assert.deepEqual(shared[1], ['x']);
  let x = 1, y = 2; [x, y] = [y, x]; assert.deepEqual([x, y], [2, 1]);
  const [first, , third = 'd', ...others] = [1, 2, undefined, 4, 5];
  assert.deepEqual([first, third, others], [1, 'd', [4, 5]]);
  assert.equal([1, 2, 3].at(-1), 3);
  assert.equal([1, null, undefined, 2].join('-'), '1---2');
  const h = [1, 2, 3]; h.length = 1; assert.deepEqual(h, [1]);
  const people = [{ n: 'b', a: 30 }, { n: 'a', a: 30 }, { n: 'c', a: 20 }];
  assert.deepEqual(people.toSorted((p, q) => p.a - q.a || p.n.localeCompare(q.n)).map((p) => p.n), ['c', 'a', 'b']);
  assert.equal(new Uint8Array([256, 257])[1], 1);
});

// ---------- 18.9 Map, Set, iterators, generators ----------
await sheet('18.9 Map/Set/iterators', async () => {
  const m = new Map([[1, 'num'], ['1', 'str']]);
  assert.equal(m.get(1), 'num'); assert.equal(m.get('1'), 'str'); assert.equal(m.size, 2);
  const objKey = {}; m.set(objKey, 'obj').set(NaN, 'nan');
  assert.equal(m.get(objKey), 'obj'); assert.equal(m.get(NaN), 'nan');
  assert.equal(JSON.stringify(m), '{}');
  assert.deepEqual(Object.fromEntries(new Map([['a', 1]])), { a: 1 });
  assert.deepEqual([...new Set([3, 1, 3, 2, 1])], [3, 1, 2]);
  const A = new Set([1, 2, 3]); const B = new Set([2, 3, 4]);
  assert.deepEqual([...A.union(B)], [1, 2, 3, 4]);
  assert.deepEqual([...A.intersection(B)], [2, 3]);
  assert.deepEqual([...A.difference(B)], [1]);
  assert.deepEqual([...A.symmetricDifference(B)], [1, 4]);
  assert.equal(new Set([2]).isSubsetOf(A), true);
  assert.equal(A.isDisjointFrom(new Set([9])), true);
  const wm = new WeakMap(); wm.set(objKey, 1); assert.equal(wm.has(objKey), true); assert.equal('size' in wm, false);
  function* nat() { let n = 1; while (true) yield n++; }
  assert.deepEqual(nat().filter((n) => n % 2).map((n) => n * n).take(3).toArray(), [1, 9, 25]);
  const gen = nat(); assert.deepEqual(gen.next(), { value: 1, done: false });
  function* two() { yield 'a'; return 'r'; }
  assert.deepEqual([...two()], ['a']);             // return value is not part of spread
  const iterable = { *[Symbol.iterator]() { yield 1; yield 2; } };
  assert.deepEqual([...iterable], [1, 2]);
  assert.throws(() => [...{ a: 1 }], TypeError);
  const g = Object.groupBy([1, 2, 3, 4], (n) => (n % 2 ? 'odd' : 'even'));
  assert.deepEqual({ ...g }, { odd: [1, 3], even: [2, 4] });
  assert.equal(Object.getPrototypeOf(g), null);
  assert.equal(Map.groupBy([1, 2, 3], (n) => n > 1).get(true).length, 2);
  async function* pages() { yield 1; yield 2; }
  const got = []; for await (const p of pages()) got.push(p);
  assert.deepEqual(got, [1, 2]);
  assert.deepEqual(await Array.fromAsync(pages()), [1, 2]);
});

// ---------- 18.10 Modules ----------
await sheet('18.10 modules', async () => {
  assert.equal(this, undefined);                 // module top-level this
  assert.equal(typeof import.meta.url, 'string');
  assert.equal(import.meta.url.startsWith('file:'), true);
  assert.equal(typeof import.meta.dirname, 'string');
  const ns = await import('node:path');          // dynamic import returns a namespace object
  assert.equal(typeof ns.join, 'function');
  assert.equal(ns.default, path);
  const json = await import('data:application/json,{"v":1}', { with: { type: 'json' } });
  assert.equal(json.default.v, 1);
});

// ---------- 18.11 Errors ----------
await sheet('18.11 errors', async () => {
  assert.throws(() => null.length, TypeError);
  assert.throws(() => undefinedName, ReferenceError);
  assert.throws(() => JSON.parse('{bad}'), SyntaxError);
  assert.throws(() => new Array(-1), RangeError);
  assert.throws(() => (1).toFixed(101), RangeError);
  assert.throws(() => decodeURIComponent('%'), URIError);
  assert.throws(() => 1n + 1, TypeError);
  class ValidationError extends Error {
    constructor(msg, opts) { super(msg, opts); this.name = 'ValidationError'; this.code = 'E_VALID'; }
  }
  const root = new Error('db down');
  const e = new ValidationError('save failed', { cause: root });
  assert.equal(e instanceof Error, true); assert.equal(e.name, 'ValidationError');
  assert.equal(e.cause, root); assert.equal(String(e), 'ValidationError: save failed');
  const order = [];
  const f = () => { try { order.push('try'); return 'from try'; } finally { order.push('finally'); } };
  assert.equal(f(), 'from try'); assert.deepEqual(order, ['try', 'finally']);
  // try/catch is synchronous: a later timer's throw escapes it -> must catch inside or await a promise
  const r = await Promise.any([Promise.reject(new Error('a')), Promise.reject(new Error('b'))]).catch((x) => x);
  assert.equal(r instanceof AggregateError, true); assert.equal(r.errors.length, 2);
  try { throw new Error('x'); } catch { /* optional catch binding (ES2019) */ }
});

// ---------- 18.12 Async & event loop ----------
await sheet('18.12 async & event loop', async () => {
  const log = [];
  log.push('A');
  setTimeout(() => log.push('B timeout'), 0);
  Promise.resolve().then(() => log.push('C then')).then(() => log.push('D then2'));
  queueMicrotask(() => log.push('M micro'));
  new Promise((res) => { log.push('P executor'); res(); });
  (async () => { log.push('F before await'); await null; log.push('F after await'); })();
  log.push('E');
  await sleep(20);
  assert.deepEqual(log, ['A', 'P executor', 'F before await', 'E', 'C then', 'M micro', 'F after await', 'D then2', 'B timeout']);
  // combinators
  const ok = (v, ms) => sleep(ms).then(() => v);
  const bad = (m, ms) => sleep(ms).then(() => { throw new Error(m); });
  assert.deepEqual(await Promise.all([ok(1, 5), ok(2, 1)]), [1, 2]);          // input order
  assert.deepEqual((await Promise.allSettled([ok(1, 1), bad('x', 1)])).map((s) => s.status), ['fulfilled', 'rejected']);
  assert.equal(await Promise.race([ok('slow', 20), ok('fast', 1)]), 'fast');
  assert.equal(await Promise.any([bad('x', 1), ok('y', 5)]), 'y');
  await assert.rejects(Promise.all([ok(1, 5), bad('boom', 1)]), /boom/);
  // parallel vs sequential awaits
  let t0 = performance.now(); await ok(1, 40); await ok(2, 40); const seq = performance.now() - t0;
  t0 = performance.now(); await Promise.all([ok(1, 40), ok(2, 40)]); const par = performance.now() - t0;
  assert.ok(seq >= 75 && par < 75, `seq ${seq} par ${par}`);
  // async fn always returns a promise; await throws rejections
  const af = async () => 1; assert.equal(af() instanceof Promise, true);
  await assert.rejects(async () => { await Promise.reject(new Error('r')); }, /r/);
  // withResolvers / try
  const { promise, resolve } = Promise.withResolvers(); resolve(7); assert.equal(await promise, 7);
  await assert.rejects(Promise.try(() => { throw new Error('sync'); }), /sync/);
  // AbortSignal.timeout
  await assert.rejects(sleep(200, null, { signal: AbortSignal.timeout(10) }), (e) => e.name === 'AbortError' || e.name === 'TimeoutError');
  // forEach(async) does not wait
  const done = []; [1, 2].forEach(async (n) => { await sleep(5); done.push(n); });
  assert.equal(done.length, 0);
  await sleep(20); assert.equal(done.length, 2);
  // Node: nextTick before promise microtasks inside a callback (CommonJS-style ordering)
  const nodeLog = await new Promise((res) => {
    setTimeout(() => {
      const l = [];
      Promise.resolve().then(() => l.push('promise'));
      process.nextTick(() => l.push('nextTick'));
      setImmediate(() => { l.push('immediate'); res(l); });
    }, 0);
  });
  assert.deepEqual(nodeLog, ['nextTick', 'promise', 'immediate']);
});

// ---------- 18.13 DOM & events ----------
await sheet('18.13 DOM & events', () => {
  document.body.innerHTML = '<ul id="list"><li class="item" data-id="7"><button>x</button></li></ul><input id="inp" value="init">';
  const list = document.getElementById('list');
  const live = list.getElementsByTagName('li'); const stat = list.querySelectorAll('li');
  const li = document.createElement('li'); li.textContent = '<b>safe</b>'; list.append(li);
  assert.equal(live.length, 2); assert.equal(stat.length, 1);      // live vs static
  assert.equal(li.innerHTML, '&lt;b&gt;safe&lt;/b&gt;');            // textContent never parses
  const first = list.querySelector('.item');
  assert.equal(first.dataset.id, '7');
  first.classList.add('done'); first.classList.toggle('item');
  assert.equal(first.className, 'done');
  assert.equal(first.querySelector('button').closest('li'), first);
  const inp = document.getElementById('inp'); inp.value = 'typed';
  assert.equal(inp.getAttribute('value'), 'init'); assert.equal(inp.value, 'typed');   // attribute vs property
  const order = [];
  document.body.addEventListener('click', () => order.push('body capture'), { capture: true });
  document.body.addEventListener('click', (e) => order.push(`body bubble target=${e.target.tagName} current=${e.currentTarget.tagName}`));
  list.addEventListener('click', (e) => { const item = e.target.closest('li'); if (item) order.push('delegated ' + item.dataset.id); });
  first.querySelector('button').click();
  assert.deepEqual(order, ['body capture', 'delegated 7', 'body bubble target=BUTTON current=BODY']);
  const ev = new Event('ping'); let heard = 0; list.addEventListener('ping', () => heard++);
  first.dispatchEvent(ev); assert.equal(heard, 0);                        // no bubbles:true -> parent does not hear
  first.dispatchEvent(new Event('ping', { bubbles: true })); assert.equal(heard, 1);
  const form = document.createElement('form'); form.innerHTML = '<input name="t" value="a"><input name="t" value="b">';
  assert.deepEqual(new FormDataCtor(form).getAll('t'), ['a', 'b']);
  let once = 0; const btn = document.createElement('button'); btn.addEventListener('click', () => once++, { once: true });
  btn.click(); btn.click(); assert.equal(once, 1);
  const a = document.createElement('a'); a.href = '#x'; let prevented = false;
  a.addEventListener('click', (e) => { e.preventDefault(); prevented = e.defaultPrevented; }); a.click();
  assert.equal(prevented, true);
});
function FormDataCtor(form) { return new window.FormData(form); }

// ---------- 18.14 Browser APIs (the parts Node shares) ----------
await sheet('18.14 browser APIs', async () => {
  const u = new URL('https://shop.example.com:8443/search?q=red shoes&page=2#top');
  assert.equal(u.origin, 'https://shop.example.com:8443');
  assert.equal(u.searchParams.get('q'), 'red shoes');
  u.searchParams.set('page', '3'); u.searchParams.append('tag', 'a&b');
  assert.equal(u.search, '?q=red+shoes&page=3&tag=a%26b');
  assert.equal(new URL('../img/a.png', 'https://x.dev/app/page/').href, 'https://x.dev/app/img/a.png');
  assert.throws(() => new URL('not a url'), TypeError);
  assert.equal(encodeURIComponent('a b&c'), 'a%20b%26c');
  localStorage.setItem('n', 42); assert.equal(localStorage.getItem('n'), '42');          // strings only
  assert.equal(localStorage.getItem('missing'), null);
  const r = new Response('not found', { status: 404 });
  assert.equal(r.ok, false);                                                              // fetch resolves on 404
  const j = new Response(JSON.stringify({ a: 1 }), { headers: { 'Content-Type': 'application/json' } });
  assert.deepEqual(await j.json(), { a: 1 });
  await assert.rejects(j.text(), TypeError);                                              // body read once
  const cloned = structuredClone({ d: new Date(0), m: new Map([[1, 1]]) });
  assert.equal(cloned.d instanceof Date && cloned.m instanceof Map, true);
  assert.throws(() => structuredClone({ f() {} }), (e) => e.name === 'DataCloneError');
  const esc = (s) => s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  assert.equal(esc('<img onerror="x">'), '&lt;img onerror=&quot;x&quot;&gt;');
});

// ---------- 18.15 Node core ----------
await sheet('18.15 Node core', async () => {
  assert.equal(path.posix.join('/srv', 'app', '..', 'data'), '/srv/data');
  assert.equal(path.posix.resolve('/srv/app', 'logs'), '/srv/app/logs');
  assert.equal(path.posix.extname('price.test.mjs'), '.mjs');
  assert.equal(path.posix.basename('/a/b/price.test.mjs', '.mjs'), 'price.test');
  assert.deepEqual(path.posix.parse('/home/u/notes.txt'), { root: '/', dir: '/home/u', base: 'notes.txt', ext: '.txt', name: 'notes' });
  assert.equal(Buffer.from('é').length, 2); assert.equal('é'.length, 1);
  assert.equal(Buffer.from('hi').toString('hex'), '6869');
  assert.equal(Buffer.from('hi').toString('base64'), 'aGk=');
  const { values, positionals } = parseArgs({ args: ['--port', '8080', 'file.txt'], options: { port: { type: 'string' } }, allowPositionals: true });
  assert.deepEqual([values.port, positionals], ['8080', ['file.txt']]);
  process.env.JS18_DEMO = 5; assert.equal(process.env.JS18_DEMO, '5');   // env values are strings
  const em = new EventEmitter(); const seen = [];
  em.on('e', (x) => seen.push(`on ${x}`)); em.once('e', (x) => seen.push(`once ${x}`));
  seen.push('before'); em.emit('e', 1); seen.push('after'); em.emit('e', 2);
  assert.deepEqual(seen, ['before', 'on 1', 'once 1', 'after', 'on 2']);     // emit is synchronous
  assert.throws(() => new EventEmitter().emit('error', new Error('unhandled')), /unhandled/);
  const p = once(em, 'ready'); em.emit('ready', 'ok'); assert.deepEqual(await p, ['ok']);
  const out = [];
  await pipeline(Readable.from(['a', 'b']),
    new Transform({ transform(chunk, _enc, cb) { cb(null, chunk.toString().toUpperCase()); } }),
    async function (source) { for await (const c of source) out.push(c.toString()); });
  assert.equal(out.join(''), 'AB');
});

// ---------- 18.16 Testing & TypeScript-adjacent runtime facts ----------
await sheet('18.16 testing', async () => {
  assert.equal(1, 1);
  assert.throws(() => assert.equal({ a: 1 }, { a: 1 }));        // equal = Object.is (reference)
  assert.deepEqual({ a: 1 }, { a: 1 });
  assert.throws(() => assert.deepEqual({ a: 1 }, { a: '1' }));   // strict deep: type matters
  assert.throws(() => { throw new TypeError('bad'); }, TypeError);
  assert.throws(() => { throw new Error('bad input'); }, /bad/);
  await assert.rejects(Promise.reject(new Error('no')), { message: 'no' });
  assert.match('order-42', /^order-\d+$/);
});

// ---------- 18.18 Version table: what Node 24 actually ships ----------
await sheet('18.18 versions (Node 24 availability)', () => {
  const has = {
    'Object.groupBy (ES2024)': typeof Object.groupBy === 'function',
    'Promise.withResolvers (ES2024)': typeof Promise.withResolvers === 'function',
    'String isWellFormed (ES2024)': typeof ''.isWellFormed === 'function',
    'Set#union (ES2025)': typeof Set.prototype.union === 'function',
    'Iterator.from (ES2025)': typeof Iterator.from === 'function',
    'Promise.try (ES2025)': typeof Promise.try === 'function',
    'RegExp.escape (ES2025)': typeof RegExp.escape === 'function',
    'Float16Array (ES2025)': typeof Float16Array === 'function',
    'Array.fromAsync (ES2026)': typeof Array.fromAsync === 'function',
    'Error.isError (ES2026)': typeof Error.isError === 'function',
    'JSON.rawJSON (ES2026)': typeof JSON.rawJSON === 'function',
    'Atomics.pause (ES2027)': typeof Atomics.pause === 'function',
  };
  for (const [k, v] of Object.entries(has)) assert.equal(v, true, k);
  const missing = {
    'Math.sumPrecise (ES2026)': typeof Math.sumPrecise,
    'Uint8Array.fromBase64 (ES2026)': typeof Uint8Array.fromBase64,
    'Map#getOrInsert (ES2026)': typeof Map.prototype.getOrInsert,
    'Iterator.concat (ES2026)': typeof Iterator.concat,
    'Temporal (ES2027)': typeof globalThis.Temporal,
    'Iterator.zip (ES2027)': typeof Iterator.zip,
  };
  const disposed = [];
  { using res = { [Symbol.dispose]() { disposed.push('disposed'); } }; disposed.push('body'); }
  assert.deepEqual(disposed, ['body', 'disposed']);                 // using (ES2027) already works
  console.log('      Node', process.version, '- not yet available:', Object.keys(missing).filter((k) => missing[k] === 'undefined').join(', ') || 'none');
});

console.log(process.exitCode ? 'SOME CHECKS FAILED' : `all ${results.length} cheat-sheet checks passed`);
