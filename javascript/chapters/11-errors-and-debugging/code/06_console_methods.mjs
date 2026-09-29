// The console toolbox (output goes to stdout for log/info/debug/table/dir/group/count, stderr for warn/error/trace).
console.log('log   : plain message', { id: 7 }, [1, 2]);
console.info('info  : alias of log in Node');
console.debug('debug : alias of log in Node (hidden by default in browser DevTools "Verbose" level)');
console.log('format: %s is %d years, %o', 'Ada', 36, { lang: 'js' });

const users = [
  { id: 1, name: 'Ada', role: 'admin' },
  { id: 2, name: 'Linus', role: 'dev' },
];
console.table(users);
console.table(users, ['name']);            // choose columns

const deep = { a: { b: { c: { d: { e: 'deep' } } } } };
console.log('default depth:', deep);
console.dir(deep, { depth: null });          // print everything

console.group('group: checkout');
console.log('validate cart');
console.group('payment');
console.log('charge card');
console.groupEnd();
console.groupEnd();

for (const k of ['a', 'b', 'a', 'a']) console.count(k);
console.countReset('a');
console.count('a');

console.assert(1 + 1 === 2, 'not printed: assertion holds');
console.assert(0.1 + 0.2 === 0.3, 'assertion failed: floating point strikes again'); // prints to stderr, does NOT throw

const label = 'sum loop';
console.time(label);
let s = 0; for (let i = 0; i < 1e6; i++) s += i;
console.timeEnd(label);                      // prints elapsed time (varies per run)

// console.trace prints a stack trace to stderr — like throwing, without throwing
function inner() { console.trace('trace: how did we get here?'); }
inner();
