import { on } from 'node:events';
import { EventEmitter } from 'node:events';

// 1) An async generator: yields values that arrive over time
async function* countdown(from, ms) {
  for (let i = from; i > 0; i--) {
    await new Promise((r) => setTimeout(r, ms));
    yield i;
  }
}
for await (const n of countdown(3, 5)) console.log('countdown', n);

// 2) Paginated API -> one flat async stream
const pages = { 1: { items: ['a', 'b'], next: 2 }, 2: { items: ['c'], next: 3 }, 3: { items: ['d', 'e'], next: null } };
const fetchPage = async (n) => { await null; return pages[n]; };
async function* allItems() {
  let page = 1;
  while (page) {
    const { items, next } = await fetchPage(page);
    yield* items;                   // delegate to the array's iterator
    page = next;
  }
}
const collected = [];
for await (const item of allItems()) {
  collected.push(item);
  if (collected.length === 4) break;   // break calls return() -> generator stops fetching
}
console.log('first 4 items :', collected);

// 3) Array.fromAsync (ES2026) collects an async iterable into an array
console.log('fromAsync     :', await Array.fromAsync(allItems()));

// 4) for await also works on an array of promises (awaits each in order)
const ps = [Promise.resolve('p1'), new Promise((r) => setTimeout(() => r('p2'), 5))];
for await (const v of ps) console.log('for await over promises:', v);

// 5) The protocol by hand: Symbol.asyncIterator returning { next() -> Promise<{value, done}> }
const ticker = {
  [Symbol.asyncIterator]() {
    let i = 0;
    return { next: async () => (i < 2 ? { value: `tick${i++}`, done: false } : { value: undefined, done: true }) };
  },
};
for await (const tk of ticker) console.log('manual iterator:', tk);

// 6) Events as an async stream (Node events.on)
const bus = new EventEmitter();
const ac = new AbortController();
setTimeout(() => { bus.emit('msg', 'hello'); bus.emit('msg', 'world'); ac.abort(); }, 5);
try {
  for await (const [text] of on(bus, 'msg', { signal: ac.signal })) console.log('event:', text);
} catch (e) {
  console.log('stream ended  :', e.name);
}
