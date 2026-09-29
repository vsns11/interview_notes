// tools_run: skip  (module under test - imported by the *.test.mjs files)
export class Cart {
  #items = new Map();                                  // sku -> { sku, price, qty }

  add(sku, price, qty = 1) {
    if (typeof sku !== 'string' || sku === '') throw new TypeError('sku must be a non-empty string');
    if (!Number.isInteger(price) || price < 0) throw new RangeError('price must be integer cents >= 0');
    if (!Number.isInteger(qty) || qty < 1) throw new RangeError('qty must be an integer >= 1');
    const line = this.#items.get(sku) ?? { sku, price, qty: 0 };
    line.qty += qty;
    this.#items.set(sku, line);
    return this;
  }

  remove(sku) { return this.#items.delete(sku); }
  get count() { let n = 0; for (const l of this.#items.values()) n += l.qty; return n; }
  lines() { return [...this.#items.values()].map((l) => ({ ...l })); }
  subtotal() { let s = 0; for (const l of this.#items.values()) s += l.price * l.qty; return s; }
}

// Percentage discount, rounded to whole cents (half up).
export function applyDiscount(cents, percent) {
  if (percent < 0 || percent > 100) throw new RangeError(`bad percent: ${percent}`);
  return Math.round(cents * (100 - percent) / 100);
}

// Formats cents as a price string, e.g. 123456 -> "$1,234.56"
export function formatPrice(cents, currency = 'USD', locale = 'en-US') {
  return new Intl.NumberFormat(locale, { style: 'currency', currency }).format(cents / 100);
}

// Async dependency: the price service is INJECTED so tests can replace it.
export async function checkout(cart, { priceService, now = () => new Date() }) {
  if (cart.count === 0) throw new Error('cart is empty');
  const { percent } = await priceService.getDiscount(cart.subtotal());
  return { total: applyDiscount(cart.subtotal(), percent), at: now().toISOString() };
}
