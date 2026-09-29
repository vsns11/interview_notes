// The everyday string methods, grouped by job. None of them change the original string.
const text = 'Hello, World!';
console.log(text.length);

// --- Searching ---
const log = 'ERROR 42: disk full; ERROR 7: fan';
console.log(log.indexOf('ERROR'), log.lastIndexOf('ERROR'), log.indexOf('WARN'), log.indexOf('ERROR', 1));
console.log(log.includes('disk'), log.startsWith('ERROR'), log.endsWith('fan'), log.startsWith('42', 6));
console.log(log.search(/\d+/));          // index of first regex match

// --- Extracting ---
console.log(text.slice(0, 5), '|', text.slice(7), '|', text.slice(-6, -1));
console.log(text.substring(0, 5), '|', text.substring(5, 0), '|', text.substring(-3, 2));
console.log(text.slice(5, 0) === '', text.substr(7, 5), '(substr: legacy, avoid)');
console.log(text.charAt(4), text.at(-1), text.charCodeAt(0), String.fromCharCode(72, 105));

// --- Case ---
console.log(text.toUpperCase(), text.toLowerCase());
console.log('istanbul'.toLocaleUpperCase('tr-TR'), 'istanbul'.toUpperCase());
console.log('Straße'.toUpperCase(), 'ǅ'.toLowerCase());

// --- Whitespace & padding ---
const raw = '   padded text \n';
console.log(JSON.stringify(raw.trim()), JSON.stringify(raw.trimStart()), JSON.stringify(raw.trimEnd()));
console.log('7'.padStart(3, '0'), 'ab'.padEnd(5, '.') + '|', '4111222233334444'.slice(-4).padStart(16, '*'));

// --- Building ---
console.log('ab'.repeat(3), 'a'.concat('b', 'c', 1), ['x', 'y', 'z'].join('-'));

// --- Splitting ---
console.log('a,b,,c'.split(','), 'hello'.split(''), 'a b  c'.split(' '), 'a b  c'.split(/\s+/), 'x'.split(','), 'a,b,c'.split(',', 2));

// --- Replacing ---
const s = 'cat, cat, cat';
console.log(s.replace('cat', 'dog'), '|', s.replaceAll('cat', 'dog'), '|', s.replace(/cat/g, 'cow'));

// --- Comparing ---
console.log('a'.localeCompare('b'), 'b'.localeCompare('a'), 'a'.localeCompare('a'));
console.log('résumé'.localeCompare('resume', 'en', { sensitivity: 'base' }), 'ä'.localeCompare('z', 'de'), 'ä'.localeCompare('z', 'sv'));
const collator = new Intl.Collator('en', { numeric: true });
console.log(['file10', 'file2', 'file1'].sort(), ['file10', 'file2', 'file1'].sort(collator.compare));

// --- Conversions ---
const wrapped = new String('boxed');
console.log(wrapped.toString(), wrapped.valueOf(), typeof wrapped.valueOf());
console.log(Number('42px'), parseInt('42px', 10), parseFloat('3.14abc'), Number(''), Number('  7 '));

// Real task: slugify a blog title
const slugify = (title) => title.trim().toLowerCase()
  .normalize('NFKD').replace(/[̀-ͯ]/g, '')   // strip accents
  .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
console.log(slugify('  Crème Brûlée: 10 Tips & Tricks!  '));

// Real task: capitalize each word
const titleCase = (str) => str.split(' ').map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
console.log(titleCase('the qUICK brown fox'));
