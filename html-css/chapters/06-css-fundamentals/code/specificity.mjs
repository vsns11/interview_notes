// Prints the specificity of sample selectors using our calculator in specificity_lib.mjs,
// and checks each result against a hand-computed expected value.
// Run: node specificity.mjs
import { specificity, compare } from './specificity_lib.mjs';

const samples = [
  '*',
  'li',
  'ul li',
  'ul > li + li',
  '.nav',
  'a:hover',
  'input[type="email"]',
  'p::first-line',
  'p:first-line',
  '.nav a.active',
  '#main',
  '#main .card p',
  '#main #sidebar',
  ':is(#a, .b, p) span',
  ':where(#a, .b, p) span',
  'button:not(.primary, [disabled])',
  '.card:has(> img)',
  'li:nth-child(2n+1 of .item)',
  'html body div#app main.content article.post > p:first-child::first-letter',
];

// Hand-computed expected values (from the rules above) -- the script checks itself.
const expected = {
  '*': [0, 0, 0], 'li': [0, 0, 1], 'ul li': [0, 0, 2], 'ul > li + li': [0, 0, 3], '.nav': [0, 1, 0],
  'a:hover': [0, 1, 1], 'input[type="email"]': [0, 1, 1], 'p::first-line': [0, 0, 2], 'p:first-line': [0, 0, 2],
  '.nav a.active': [0, 2, 1], '#main': [1, 0, 0], '#main .card p': [1, 1, 1], '#main #sidebar': [2, 0, 0],
  ':is(#a, .b, p) span': [1, 0, 1], ':where(#a, .b, p) span': [0, 0, 1],
  'button:not(.primary, [disabled])': [0, 1, 1], '.card:has(> img)': [0, 1, 1],
  'li:nth-child(2n+1 of .item)': [0, 2, 1],
  'html body div#app main.content article.post > p:first-child::first-letter': [1, 3, 7],
};

console.log('selector'.padEnd(76), '(A,B,C)');
for (const sel of samples) {
  const s = specificity(sel);
  const ok = JSON.stringify(s) === JSON.stringify(expected[sel]) ? '' : '   <-- MISMATCH';
  console.log(sel.padEnd(76), `(${s.join(',')})${ok}`);
}

// Sort a few competing selectors for the same <a class="active"> inside <nav id="top" class="nav">
const competing = ['a', '.nav a', 'nav a.active', '#top a', ':where(#top) a.active', 'a:is(.active, #x)'];
console.log('\nWinner order for <nav id="top" class="nav"><a class="active">…</a></nav> (highest first):');
competing.map(sel => [sel, specificity(sel)])
  .sort((x, y) => compare(y[1], x[1]))
  .forEach(([sel, s], k) => console.log(`  ${k + 1}. ${sel.padEnd(24)} (${s.join(',')})`));
