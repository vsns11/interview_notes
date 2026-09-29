// Reading a stack trace: newest call first.
import path from 'node:path';

function validate(cart) { if (cart.items.length === 0) throw new Error('empty cart'); }
function checkout(cart) { validate(cart); }
function handleClick() { checkout({ items: [] }); }

try {
  handleClick();
} catch (err) {
  // shorten absolute file paths so the output is the same on every machine
  const pretty = err.stack.replace(/file:\/\/\S+\//g, '').replace(/\(.*[\\/](\S+?:\d+:\d+)\)/g, '($1)');
  console.log(pretty.split('\n').slice(0, 5).join('\n'));
}

// V8-specific helpers (Node, Chrome, Edge; not standard):
console.log('\nError.stackTraceLimit default:', Error.stackTraceLimit);

class TimeoutError extends Error {
  constructor(ms) {
    super(`timed out after ${ms} ms`);
    this.name = 'TimeoutError';
  }
}
function makeTimeout() { return new TimeoutError(500); }
const t = makeTimeout();
console.log('frames mention makeTimeout?', t.stack.includes('makeTimeout'));

const o = {};
Error.captureStackTrace(o);                     // attach a .stack to any object
console.log('captureStackTrace on plain object:', typeof o.stack, path.basename(import.meta.filename));
