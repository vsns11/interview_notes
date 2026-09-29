// Regular expressions: creating, flags, test/exec/match/matchAll/replace/split, groups, lookaround.
const literal = /colou?r/i;                       // literal: compiled once, when the script loads
const dynamic = new RegExp('colou?r', 'i');       // constructor: pattern built from a string at run time
console.log(literal.test('Nice COLOR'), dynamic.source, dynamic.flags, literal.toString());

// test -> boolean
const zip = /^\d{5}(-\d{4})?$/;
console.log(['12345', '12345-6789', '1234', '12345-'].map((z) => `${z}:${zip.test(z)}`).join(' '));

// match without g -> first match with details; with g -> array of all matched strings
const text = 'Order #123 shipped, order #456 pending';
const first = text.match(/#(\d+)/);
console.log(first[0], first[1], first.index);
console.log(text.match(/#\d+/g), 'no match ->', text.match(/#x/g));

// matchAll (needs g) -> iterator of full match objects, including groups
for (const m of text.matchAll(/#(?<id>\d+) (?<status>\w+)/g)) {
  console.log(m.index, m.groups.id, m.groups.status);
}

// Named groups with destructuring
const { groups: { year, month, day } } = '2024-07-04'.match(/(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})/);
console.log(year, month, day);

// replace: $1 / $<name> / $& in the replacement string, or a function
console.log('2024-07-04'.replace(/(\d{4})-(\d{2})-(\d{2})/, '$3/$2/$1'));
console.log('2024-07-04'.replace(/(?<y>\d{4})-(?<m>\d{2})-(?<d>\d{2})/, '$<d>.$<m>.$<y>'));
console.log('price: 10 USD, 25 USD'.replace(/(\d+) USD/g, (all, n) => `€${(n * 0.9).toFixed(2)}`));
console.log('snake_case_name'.replace(/_(\w)/g, (_, c) => c.toUpperCase()));
console.log('a.b.c'.replaceAll('.', '/'), 'x'.replace('x', '$&$&'));

// split with a regex (capturing groups are kept in the result)
console.log('one, two;three  four'.split(/[,;\s]+/), '1+2-3'.split(/([+-])/));

// Greedy vs lazy quantifiers
const html = '<b>bold</b> and <i>italic</i>';
console.log(html.match(/<.+>/)[0], '|', html.match(/<.+?>/)[0], '|', html.match(/<[^>]+>/g));

// Anchors, word boundaries, multiline flag
console.log(/\bcat\b/.test('concatenate'), /\bcat\b/.test('a cat sat'));
const lines = 'id: 1\nname: Bo\nid: 2';
console.log(lines.match(/^id: \d+$/g), lines.match(/^id: \d+$/gm));

// dotAll (s) flag: . matches newlines too
console.log(/start.+end/.test('start\nend'), /start.+end/s.test('start\nend'));

// Lookahead / lookbehind (match a position without consuming characters)
console.log('$30 and €40 and $5'.match(/(?<=\$)\d+/g));        // numbers preceded by $
console.log('100px 2em 30px'.match(/\d+(?=px)/g));             // numbers followed by px
const strong = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
console.log(['weakpass', 'Str0ngPass', 'Short1A'].map((p) => `${p}:${strong.test(p)}`).join(' '));

// Backreference: repeated word
console.log('this is is a test test'.match(/\b(\w+) \1\b/g));

// The lastIndex trap: a /g regex is stateful when reused with test/exec
const g = /a/g;
console.log(g.test('a'), g.lastIndex, g.test('a'), g.lastIndex, g.test('a'));

// exec loop (the pre-matchAll way)
const re = /\d+/g; let m; const found = [];
while ((m = re.exec('a1b22c333')) !== null) found.push(`${m[0]}@${m.index}`);
console.log(found);

// sticky (y): match must start exactly at lastIndex
const sticky = /\d+/y; sticky.lastIndex = 1;
console.log(sticky.exec('a12b')?.[0], (sticky.lastIndex = 0, sticky.exec('a12b')));
