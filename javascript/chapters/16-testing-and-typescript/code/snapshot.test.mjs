// Snapshot testing (stable since Node 23.4). The first run needs:  node --test --test-update-snapshots
// which writes snapshot.test.mjs.snapshot next to this file; later runs compare against it.
import { test } from 'node:test';
import { Cart } from './src/cart.mjs';

function receiptText(cart) {
  const rows = cart.lines().map((l) => `${l.sku.padEnd(6)} x${l.qty}  ${(l.price * l.qty / 100).toFixed(2).padStart(7)}`);
  return [...rows, '-'.repeat(20), `TOTAL ${(cart.subtotal() / 100).toFixed(2).padStart(13)}`].join('\n');
}

test('receipt layout', (t) => {
  const cart = new Cart().add('pen', 150, 3).add('ink', 900);
  t.assert.snapshot(receiptText(cart));
});
