// Imports are live, read-only views of the exporter's variables. Modules are singletons.
import { count, increment } from './counter.mjs';
import * as ns from './math.mjs';
import * as again from './math.mjs';     // same module -> evaluated once, same namespace object

console.log('before     :', count);
increment(); increment();
console.log('after x2   :', count, '(the import sees the new value)');

try {
  count = 100;                           // imports are read-only
} catch (e) {
  console.log('assign     :', e.name, '-', e.message);
}

console.log('same ns?   :', ns === again);
console.log('ns tag     :', Object.prototype.toString.call(ns));
console.log('ns sealed  :', Object.isSealed(ns), '| frozen:', Object.isFrozen(ns));
try { ns.add = null; } catch (e) { console.log('ns write   :', e.name, '-', e.message); }

// Module scope: strict mode, `this` is undefined, top-level bindings are not globals
console.log('this       :', this);
const topLevel = 1;
console.log('global?    :', 'topLevel' in globalThis);
try { undeclared = 5; } catch (e) { console.log('strict     :', e.name, '-', e.message); }
