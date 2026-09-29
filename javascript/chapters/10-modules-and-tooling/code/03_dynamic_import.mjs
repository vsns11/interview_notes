// import(specifier) loads a module at runtime and returns a promise of its namespace.
import * as staticMath from './math.mjs';

const verbose = process.env.VERBOSE !== '0';   // decide at runtime whether to load the logger
if (verbose) {
  const { default: log } = await import('./logger.mjs');  // default export is the "default" key
  console.log(log('debug', 'logger loaded lazily'));
}

const name = 'math';
const dyn = await import(`./${name}.mjs`);                 // computed specifier
console.log('same instance as static import?', dyn === staticMath);

// Load several in parallel
const [m, l] = await Promise.all([import('./math.mjs'), import('./logger.mjs')]);
console.log('parallel :', m.add(1, 1), l.LEVELS.length);

// Failure is a rejected promise
try {
  await import('./does-not-exist.mjs');
} catch (e) {
  console.log('missing  :', e.code);
}

// Built-in modules use the node: scheme
const { basename } = await import('node:path');
console.log('node:path:', basename('/a/b/report.pdf'));
