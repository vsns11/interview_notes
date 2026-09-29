// @ts-check
// JSDoc types: TypeScript checking for plain .js/.mjs files - no build step at all.
// Check: tsc --noEmit (this file is listed in tsconfig "include"; // @ts-check turns checking on)

/**
 * @typedef {object} LineItem
 * @property {string} sku
 * @property {number} price  unit price in cents
 * @property {number} [qty]  optional, defaults to 1
 */

/**
 * Sum a list of line items.
 * @param {LineItem[]} items
 * @returns {number} total in cents
 */
export function sum(items) {
  return items.reduce((t, i) => t + i.price * (i.qty ?? 1), 0);
}

/**
 * @template T
 * @param {T[]} xs
 * @returns {T | undefined}
 */
export function last(xs) {
  return xs[xs.length - 1];
}

/** @type {Record<string, 'admin' | 'user'>} */
const roles = { ana: 'admin', bo: 'user' };

console.log('sum  :', sum([{ sku: 'pen', price: 150, qty: 2 }, { sku: 'ink', price: 900 }]));
console.log('last :', last(['a', 'b', 'c']));
console.log('roles:', roles);
