// A small specificity calculator (Selectors Level 4 rules) -- our own implementation, for learning.
// Specificity is a triple (A, B, C):
//   A = ID selectors                                   #main
//   B = classes, attribute selectors, pseudo-classes   .btn  [type="text"]  :hover
//   C = type selectors and pseudo-elements             p  ::before
// Special cases: * and combinators count nothing; :where() counts 0; :is()/:not()/:has() and
// :nth-child(An+B of S) count as their MOST specific argument; legacy :before/:after/:first-line/
// :first-letter written with one colon are still pseudo-elements.
// Not handled (not needed here): namespaces, escapes, nesting (&).
// Imported by specificity.mjs (prints a table) and cascade.mjs.

const LEGACY_PSEUDO_ELEMENTS = ['before', 'after', 'first-line', 'first-letter'];

// Split "a, b:is(c, d)" on top-level commas only.
function splitTopLevel(s) {
  const parts = []; let depth = 0, cur = '';
  for (const ch of s) {
    if ('(['.includes(ch)) depth++;
    if (')]'.includes(ch)) depth--;
    if (ch === ',' && depth === 0) { parts.push(cur.trim()); cur = ''; } else cur += ch;
  }
  if (cur.trim()) parts.push(cur.trim());
  return parts;
}

// Read the text inside matching parentheses starting at s[i] === '('.
function readParens(s, i) {
  let depth = 0, j = i;
  for (; j < s.length; j++) {
    if (s[j] === '(') depth++;
    else if (s[j] === ')' && --depth === 0) break;
  }
  return [s.slice(i + 1, j), j + 1];
}

const add = (x, y) => x.map((v, k) => v + y[k]);
export const compare = (x, y) => x[0] - y[0] || x[1] - y[1] || x[2] - y[2];
const maxOf = list => list.map(specificity).reduce((m, s) => (compare(s, m) > 0 ? s : m), [0, 0, 0]);

export function specificity(selector) {
  let s = [0, 0, 0];
  let i = 0;
  const str = selector.trim();
  while (i < str.length) {
    const ch = str[i];
    if (ch === '#') {                                   // ID
      s = add(s, [1, 0, 0]); i++;
      while (i < str.length && /[\w-]/.test(str[i])) i++;
    } else if (ch === '.') {                            // class
      s = add(s, [0, 1, 0]); i++;
      while (i < str.length && /[\w-]/.test(str[i])) i++;
    } else if (ch === '[') {                            // attribute selector
      s = add(s, [0, 1, 0]);
      i = str.indexOf(']', i) + 1;
    } else if (ch === ':') {
      const isElement = str[i + 1] === ':';
      i += isElement ? 2 : 1;
      let name = '';
      while (i < str.length && /[\w-]/.test(str[i])) name += str[i++];
      let arg = null;
      if (str[i] === '(') [arg, i] = readParens(str, i);
      name = name.toLowerCase();
      if (isElement || LEGACY_PSEUDO_ELEMENTS.includes(name)) s = add(s, [0, 0, 1]);
      else if (name === 'where') { /* always zero */ }
      else if (['is', 'not', 'has', 'matches'].includes(name)) s = add(s, maxOf(splitTopLevel(arg)));
      else if (['nth-child', 'nth-last-child'].includes(name) && / of /.test(arg))
        s = add(add(s, [0, 1, 0]), maxOf(splitTopLevel(arg.split(/ of /)[1])));
      else s = add(s, [0, 1, 0]);                       // any other pseudo-class
    } else if (/[a-zA-Z]/.test(ch)) {                   // type selector
      s = add(s, [0, 0, 1]);
      while (i < str.length && /[\w-]/.test(str[i])) i++;
    } else i++;                                         // *, combinators, spaces: count nothing
  }
  return s;
}

