// node:events - EventEmitter, the publish/subscribe base of Node's APIs
import { EventEmitter, once, on } from 'node:events';

// 1) Basics: on / emit / once / off
const bus = new EventEmitter();
function onOrder(order) { console.log('  handler A got order', order.id); }
bus.on('order', onOrder);
bus.once('order', (o) => console.log('  once-handler fires only the first time', o.id));
bus.prependListener('order', () => console.log('  prepended handler runs first'));

console.log('emit #1 ->', bus.emit('order', { id: 1 }));   // true: had listeners
console.log('emit #2');
bus.emit('order', { id: 2 });
bus.off('order', onOrder);                                  // needs the SAME function reference
console.log('listeners left:', bus.listenerCount('order'));
console.log('emit with no listeners ->', bus.emit('nobody-listens'));

// 2) emit() is SYNCHRONOUS - listeners run before emit returns
const e2 = new EventEmitter();
e2.on('ping', () => console.log('  (listener runs)'));
console.log('before emit'); e2.emit('ping'); console.log('after emit');

// 3) Subclassing: a class that "is" an emitter
class Kettle extends EventEmitter {
  #temp = 20;
  heat(step) {
    this.#temp += step;
    this.emit('temp', this.#temp);
    if (this.#temp >= 100) this.emit('boiled');
  }
}
const k = new Kettle();
k.on('temp', (t) => { if (t % 40 === 0) console.log('  kettle at', t); });
k.once('boiled', () => console.log('  kettle: boiled!'));
for (let i = 0; i < 4; i++) k.heat(20);

// 4) The special 'error' event: with no listener, emit('error') THROWS
const e3 = new EventEmitter();
try { e3.emit('error', new Error('disk full')); }
catch (err) { console.log('unhandled error event threw:', err.message); }
e3.on('error', (err) => console.log('handled error event:', err.message));
e3.emit('error', new Error('disk full'));

// 5) Promise helpers: once() waits for one event; on() is an async iterator
const e4 = new EventEmitter();
setTimeout(() => e4.emit('ready', 'db', 42), 10);
const [name, port] = await once(e4, 'ready');               // resolves with the args array
console.log('once() resolved with:', name, port);

const ac = new AbortController();
setTimeout(() => { e4.emit('tick', 1); e4.emit('tick', 2); e4.emit('tick', 3); }, 10);
let count = 0;
try {
  for await (const [n] of on(e4, 'tick', { signal: ac.signal })) {
    console.log('  tick', n);
    if (++count === 3) ac.abort();                          // stop listening
  }
} catch (err) {
  console.log('on() loop ended by', err.name);
}

// 6) Memory-leak guard: > 10 listeners on one event prints a warning
const e5 = new EventEmitter();
console.log('default max listeners:', e5.getMaxListeners());
