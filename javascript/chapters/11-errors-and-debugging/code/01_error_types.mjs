// The built-in error types, each triggered for real and caught.
const cases = {
  TypeError:      () => null.length,
  ReferenceError: () => notDeclaredAnywhere + 1,
  SyntaxError:    () => JSON.parse('{ bad json }'),
  RangeError:     () => new Array(-1),
  URIError:       () => decodeURIComponent('%'),
  EvalError:      () => { throw new EvalError('never thrown by modern engines — only kept for compatibility'); },
  AggregateError: () => { throw new AggregateError([new Error('a'), new TypeError('b')], 'several things failed'); },
};

for (const fn of Object.values(cases)) {
  try {
    fn();
  } catch (err) {
    console.log(`${err.name.padEnd(15)} | instanceof Error: ${err instanceof Error} | ${err.message}`);
  }
}

// Every error object has name + message (+ stack, + optional cause)
const e = new TypeError('price must be a number', { cause: { got: 'ten' } });
console.log('\nname   :', e.name);
console.log('message:', e.message);
console.log('cause  :', e.cause);
console.log('stack  :', e.stack.split('\n')[0], '... (+', e.stack.split('\n').length - 1, 'frame lines)');
console.log('String :', String(e));

// Hierarchy: all built-in types inherit from Error
console.log('\nproto chain:', Object.getPrototypeOf(TypeError) === Error, Object.getPrototypeOf(RangeError.prototype) === Error.prototype);

// AggregateError from Promise.any when every promise rejects
try {
  await Promise.any([Promise.reject(new Error('mirror 1 down')), Promise.reject(new Error('mirror 2 down'))]);
} catch (agg) {
  console.log('\nPromise.any ->', agg.name, '|', agg.message, '|', agg.errors.map((x) => x.message));
}

// Error.isError (ES2026) — robust check that also works across realms
console.log('\nError.isError:', Error.isError(e), Error.isError({ name: 'Error', message: 'fake' }));

// SyntaxError in source code is thrown when the code is PARSED, not when a line runs:
try {
  new Function('return 1 +');   // parsing happens here
} catch (err) {
  console.log('parse-time  :', err.name, '-', err.message);
}
