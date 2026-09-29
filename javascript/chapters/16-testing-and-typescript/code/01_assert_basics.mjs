// node:assert/strict - the assertion library that node:test uses
import assert from 'node:assert/strict';

function check(label, fn) {
  try { fn(); console.log('PASS', label); }
  catch (err) { console.log('FAIL', label, '->', err.code, '|', err.message.split('\n')[0]); }
}

check('equal uses Object.is', () => assert.equal(0.1 + 0.2, 0.3));
check('equal numbers', () => assert.equal(2 + 2, 4));
check('equal objects (reference!)', () => assert.equal({ a: 1 }, { a: 1 }));
check('deepEqual objects', () => assert.deepEqual({ a: 1, tags: ['x'] }, { a: 1, tags: ['x'] }));
check('deepEqual is type-strict', () => assert.deepEqual({ n: 1 }, { n: '1' }));
check('ok (truthy)', () => assert.ok([].length));
check('match regex', () => assert.match('order #A-1042', /#A-\d+/));
check('throws with class + message', () =>
  assert.throws(() => { throw new RangeError('qty must be >= 1'); }, { name: 'RangeError', message: /qty/ }));
check('throws but nothing thrown', () => assert.throws(() => 42));
check('notDeepEqual', () => assert.notDeepEqual([1, 2], [2, 1]));
check('float with tolerance', () => assert.ok(Math.abs(0.1 + 0.2 - 0.3) < Number.EPSILON));

// The diff that a failing deepEqual prints:
try {
  assert.deepEqual({ id: 7, items: ['pen', 'ink'] }, { id: 7, items: ['pen', 'paper'] });
} catch (err) {
  console.log('--- AssertionError message ---\n' + err.message);
}

// rejects: for promises
await assert.rejects(Promise.reject(new Error('timeout')), /timeout/);
console.log('PASS rejects');
