// The iteration protocols: iterables (Symbol.iterator) and iterators (next()).

// 1) Driving a built-in iterator by hand
const it = ['a', 'b'][Symbol.iterator]();
console.log(it.next(), it.next(), it.next());

// 2) What is iterable?
const isIterable = (x) => x != null && typeof x[Symbol.iterator] === 'function';
console.log([['array', [1]], ['string', 'hi'], ['Map', new Map()], ['Set', new Set()], ['object', {}], ['number', 5]]
  .map(([n, v]) => `${n}:${isIterable(v)}`).join(' '));

// 3) Making an object iterable: return an object with next() from [Symbol.iterator]
const countdown = {
  from: 3,
  [Symbol.iterator]() {
    let current = this.from;
    return {
      next: () => (current > 0 ? { value: current--, done: false } : { value: undefined, done: true }),
      [Symbol.iterator]() { return this; },           // iterators are usually iterable too
    };
  },
};
console.log([...countdown], Array.from(countdown), Math.max(...countdown));
for (const n of countdown) process.stdout.write(n + ' ');
console.log('liftoff');
const [first, ...others] = countdown;
console.log(first, others, new Set(countdown).size, new Map([[1, 'x']]).size);

// 4) A class with an iterator: a numeric range
class Range {
  constructor(start, end, step = 1) { Object.assign(this, { start, end, step }); }
  *[Symbol.iterator]() {                                // a generator method (see 05) is the easy way
    for (let v = this.start; v < this.end; v += this.step) yield v;
  }
}
console.log([...new Range(0, 10, 3)]);

// 5) return(): called when a consumer stops early (break, destructuring, throw) - used for cleanup
function resource() {
  let i = 0;
  return {
    [Symbol.iterator]() { return this; },
    next() { i++; return { value: i, done: i > 5 }; },
    return() { console.log('  cleanup after', i, 'items'); return { done: true }; },
  };
}
for (const x of resource()) { if (x === 2) break; }
const [one] = resource();
console.log('first item was', one);

// 6) Iterators are single-use; iterables can be iterated again
const arr = [1, 2];
const once = arr.values();
console.log([...once], [...once], [...arr], [...arr]);

// 7) Everything that consumes iterables
const src = new Set(['x', 'y']);
console.log([...src], Array.from(src), Object.fromEntries([['k', 1]]), new Map(Object.entries({ a: 1 })));
await Promise.all(src).then((v) => console.log('Promise.all takes an iterable:', v));
