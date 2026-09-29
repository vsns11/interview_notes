// tools_run: skip  (type errors ON PURPOSE)  Check:  tsc --ignoreConfig --noEmit --allowJs --checkJs --strict --target es2024 jsdoc_errors.mjs
/**
 * @param {number} cents
 * @returns {string}
 */
function formatCents(cents) {
  return (cents / 100).toFixed(2);
}

formatCents('150');                         // string passed where number expected
/** @type {number} */
const count = formatCents(99);              // string result assigned to a number
console.log(count.toUpperCase());           // number has no toUpperCase
