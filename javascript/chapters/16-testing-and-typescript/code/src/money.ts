// tools_run: skip  (module under test for money.test.ts)
export type Currency = 'USD' | 'EUR' | 'INR';
export interface Money { readonly cents: number; readonly currency: Currency }

export function money(cents: number, currency: Currency = 'USD'): Money {
  if (!Number.isInteger(cents)) throw new TypeError('cents must be an integer');
  return Object.freeze({ cents, currency });
}

export function add(a: Money, b: Money): Money {
  if (a.currency !== b.currency) throw new Error(`currency mismatch: ${a.currency} vs ${b.currency}`);
  return money(a.cents + b.cents, a.currency);
}

// Split an amount into n parts that differ by at most 1 cent and add up exactly.
export function split(m: Money, n: number): Money[] {
  const base = Math.floor(m.cents / n);
  const extra = m.cents % n;
  return Array.from({ length: n }, (_, i) => money(base + (i < extra ? 1 : 0), m.currency));
}
