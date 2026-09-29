// Unit tests for src/cart.mjs.  Run:  node --test   (or: node cart.test.mjs)
import { describe, it, test, before, beforeEach, afterEach, after } from 'node:test';
import assert from 'node:assert/strict';
import { Cart, applyDiscount, formatPrice } from './src/cart.mjs';

describe('Cart', () => {
  let cart;
  beforeEach(() => { cart = new Cart(); });          // fresh object for every test

  it('starts empty', () => {
    assert.equal(cart.count, 0);
    assert.equal(cart.subtotal(), 0);
  });

  it('adds items and merges the same sku', () => {
    // Arrange - Act - Assert
    cart.add('pen', 150, 2).add('ink', 900).add('pen', 150);
    assert.equal(cart.count, 4);
    assert.deepEqual(cart.lines(), [
      { sku: 'pen', price: 150, qty: 3 },
      { sku: 'ink', price: 900, qty: 1 },
    ]);
    assert.equal(cart.subtotal(), 1350);
  });

  it('removes items', () => {
    cart.add('pen', 150);
    assert.equal(cart.remove('pen'), true);
    assert.equal(cart.remove('pen'), false);
  });

  describe('validation', () => {
    const bad = [
      ['', 100, 1, TypeError],
      ['pen', -1, 1, RangeError],
      ['pen', 1.5, 1, RangeError],
      ['pen', 100, 0, RangeError],
    ];
    for (const [sku, price, qty, ErrorType] of bad) {     // table-driven tests
      it(`rejects add(${JSON.stringify(sku)}, ${price}, ${qty})`, () => {
        assert.throws(() => cart.add(sku, price, qty), ErrorType);
      });
    }
  });

  it('lines() returns copies, not internal state', () => {
    cart.add('pen', 150);
    cart.lines()[0].qty = 99;
    assert.equal(cart.count, 1);
  });
});

describe('applyDiscount', () => {
  it('takes a percentage off and rounds to cents', () => {
    assert.equal(applyDiscount(1000, 10), 900);
    assert.equal(applyDiscount(999, 15), 849);         // 849.15 -> 849
    assert.equal(applyDiscount(1000, 0), 1000);
    assert.equal(applyDiscount(1000, 100), 0);
  });
  it('rejects percentages outside 0-100', () => {
    assert.throws(() => applyDiscount(1000, 101), { name: 'RangeError', message: 'bad percent: 101' });
  });
});

test('formatPrice', async (t) => {                      // subtests with t.test
  await t.test('US dollars', () => assert.equal(formatPrice(123456), '$1,234.56'));
  await t.test('euros in Germany', () => assert.equal(formatPrice(123456, 'EUR', 'de-DE'), '1.234,56 €'));
});

test.skip('multi-currency carts', () => {});           // reported as skipped
test.todo('coupon codes');                               // reported as todo

// Hook order demo
describe('hooks order', () => {
  const log = [];
  before(() => log.push('before'));
  beforeEach(() => log.push('beforeEach'));
  afterEach(() => log.push('afterEach'));
  after(() => console.log('# hooks ran:', log.join(' > ')));
  it('one', () => log.push('test1'));
  it('two', () => log.push('test2'));
});
