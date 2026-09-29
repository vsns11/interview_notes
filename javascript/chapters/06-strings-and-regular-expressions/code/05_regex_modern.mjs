// Newer regex features: u and v flags, Unicode property escapes, RegExp.escape, modifiers,
// duplicate named groups, d flag (indices). Plus practical parsing tasks.

// u flag: treat the pattern and input as code points
console.log(/^.$/.test('😀'), /^.$/u.test('😀'));
console.log('Ünïcödé 123 ñ'.match(/\p{L}+/gu));                   // any letter in any script
console.log('Price: ٣٤ or 34'.match(/\p{Nd}+/gu));                 // decimal digits in any script
console.log('Hi 👋 there 🎉'.replace(/\p{Extended_Pictographic}/gu, '*'));

// v flag (ES2024): set operations inside classes and properties of strings
console.log('abcdef'.match(/[\p{L}--[aeiou]]/gv));                 // letters minus vowels
console.log('I 🇮🇳 flags'.match(/\p{RGI_Emoji_Flag_Sequence}/v)?.[0]);

// RegExp.escape (ES2025): safely put user text into a pattern
const userInput = 'a.b*c';
const safe = new RegExp(RegExp.escape(userInput), 'g');
console.log(RegExp.escape(userInput), 'xa.b*cx axbbc'.replace(safe, '[hit]'));
console.log('without escaping:', 'xa.b*cx axbbc'.replace(new RegExp(userInput, 'g'), '[hit]'));

// d flag (ES2022): start/end indices of each group
const withIdx = /(?<key>\w+)=(?<val>\w+)/d.exec('  mode=dark');
console.log(withIdx.indices.groups.key, withIdx.indices.groups.val);

// Duplicate named capture groups (ES2025): same name in different alternatives
const dateRe = /(?<y>\d{4})-(?<m>\d{2})|(?<m>\d{2})\/(?<y>\d{4})/;
console.log('2024-07'.match(dateRe).groups, '07/2024'.match(dateRe).groups);

// Pattern modifiers (ES2025): turn a flag on for part of the pattern only
console.log(/^(?i:hello) World$/.test('HELLO World'), /^(?i:hello) World$/.test('HELLO WORLD'));

// Practical 1: parse web-server log lines into objects
const logs = `127.0.0.1 - [10/Oct/2024:13:55:36] "GET /index.html HTTP/1.1" 200 2326
10.0.0.5 - [10/Oct/2024:13:56:01] "POST /api/login HTTP/1.1" 401 87`;
const lineRe = /^(?<ip>\S+) - \[(?<time>[^\]]+)\] "(?<method>[A-Z]+) (?<path>\S+) [^"]+" (?<status>\d{3}) (?<bytes>\d+)$/gm;
console.table([...logs.matchAll(lineRe)].map((m) => ({ ...m.groups, status: Number(m.groups.status) })));

// Practical 2: a pragmatic email check (full RFC 5322 validation by regex is impractical)
const email = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
console.log(['a@b.co', 'no-at.com', 'x@y', 'sp ace@a.io'].map((e) => `${e}:${email.test(e)}`).join(' '));

// Practical 3: format a number with thousands separators (regex vs Intl)
const n = 1234567.891;
console.log(String(Math.trunc(n)).replace(/\B(?=(\d{3})+(?!\d))/g, ','), n.toLocaleString('en-US'), n.toLocaleString('de-DE'));

// Practical 4: template filling with a replacer function
const tpl = 'Hi {name}, your code is {code}. {unknown}';
const data = { name: 'Ana', code: 'X9' };
console.log(tpl.replace(/\{(\w+)\}/g, (m, key) => data[key] ?? m));
