// Async code, test doubles (fakes, mock functions, method mocks) and fake timers.
import { describe, it, mock } from 'node:test';
import assert from 'node:assert/strict';
import { Cart, checkout } from './src/cart.mjs';

const fixedNow = () => new Date('2026-01-15T10:00:00Z');

describe('checkout (async + dependency injection)', () => {
  it('applies the discount returned by the price service (fake object)', async () => {
    const fakePriceService = { getDiscount: async () => ({ percent: 10 }) };   // a hand-written fake
    const cart = new Cart().add('pen', 1000);
    const receipt = await checkout(cart, { priceService: fakePriceService, now: fixedNow });
    assert.deepEqual(receipt, { total: 900, at: '2026-01-15T10:00:00.000Z' });
  });

  it('rejects an empty cart', async () => {
    await assert.rejects(checkout(new Cart(), { priceService: {} }), { message: 'cart is empty' });
  });

  it('propagates service failures', async () => {
    const failing = { getDiscount: async () => { throw new Error('503 from pricing') } };
    await assert.rejects(checkout(new Cart().add('pen', 100), { priceService: failing }), /503/);
  });

  it('calls the service once with the subtotal (mock.fn spy)', async () => {
    const getDiscount = mock.fn(async () => ({ percent: 0 }));
    await checkout(new Cart().add('ink', 250, 2), { priceService: { getDiscount }, now: fixedNow });
    assert.equal(getDiscount.mock.callCount(), 1);
    assert.deepEqual(getDiscount.mock.calls[0].arguments, [500]);
  });
});

describe('mock.method and t.mock', () => {
  const api = {
    async fetchUser(id) { throw new Error('real network call - must not run in tests'); },
  };

  it('replaces a method for one test; t.mock restores it automatically', async (t) => {
    const m = t.mock.method(api, 'fetchUser', async (id) => ({ id, name: 'Test User' }));
    assert.deepEqual(await api.fetchUser(7), { id: 7, name: 'Test User' });
    assert.equal(m.mock.callCount(), 1);
  });

  it('after the previous test the real method is back', async () => {
    await assert.rejects(api.fetchUser(1), /real network call/);
  });

  it('mockImplementationOnce: fail first, then succeed (retry logic)', async (t) => {
    const flaky = t.mock.fn(async () => 'ok');
    flaky.mock.mockImplementationOnce(async () => { throw new Error('ECONNRESET'); });
    async function withRetry(fn, tries = 3) {
      for (let i = 1; ; i++) {
        try { return await fn(); } catch (e) { if (i >= tries) throw e; }
      }
    }
    assert.equal(await withRetry(flaky), 'ok');
    assert.equal(flaky.mock.callCount(), 2);
  });
});

describe('fake timers', () => {
  function debounce(fn, ms) {
    let id;
    return (...args) => { clearTimeout(id); id = setTimeout(() => fn(...args), ms); };
  }

  it('debounce calls once, after the quiet period - without really waiting', (t) => {
    t.mock.timers.enable({ apis: ['setTimeout'] });
    const save = t.mock.fn();
    const debouncedSave = debounce(save, 300);
    debouncedSave('a'); debouncedSave('ab'); debouncedSave('abc');
    t.mock.timers.tick(299);
    assert.equal(save.mock.callCount(), 0);
    t.mock.timers.tick(1);
    assert.equal(save.mock.callCount(), 1);
    assert.deepEqual(save.mock.calls[0].arguments, ['abc']);
  });

  it('Date can be frozen too', (t) => {
    t.mock.timers.enable({ apis: ['Date'], now: new Date('2030-05-01T00:00:00Z') });
    assert.equal(new Date().toISOString(), '2030-05-01T00:00:00.000Z');
  });
});

