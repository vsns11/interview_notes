// tools_run: skip  (fails ON PURPOSE to show failure output)  Run:  node --test demo_failing.mjs
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { applyDiscount } from './src/cart.mjs';

test('a wrong expectation', () => {
  assert.equal(applyDiscount(999, 15), 850);          // real answer is 849
});

test('a test that hangs', { timeout: 50 }, async () => {
  await new Promise((r) => setTimeout(r, 1000));
});

test('forgot to await the rejection', async () => {
  assert.rejects(Promise.resolve('no error'));          // missing await -> failure escapes the test
});
