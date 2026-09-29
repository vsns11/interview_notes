// URL and URLSearchParams: parse and build URLs without string bugs
const u = new URL('https://shop.example.com:8443/search/results?q=red+shoes&size=9&size=10#reviews');
console.log('1) parts:', { protocol: u.protocol, host: u.host, hostname: u.hostname, port: u.port, pathname: u.pathname, search: u.search, hash: u.hash, origin: u.origin });

// 2) searchParams: get, getAll, has, set, append, delete, sort, iterate
const p = u.searchParams;
console.log('2) q =', p.get('q'), '| size =', p.get('size'), '| getAll =', p.getAll('size'), '| has page:', p.has('page'));
p.set('page', '2');
p.append('size', '11');
p.delete('q');
p.sort();
console.log('   now:', u.search, '| entries:', [...p]);

// 3) Relative URLs resolve against a base
console.log('3)', new URL('../img/logo.png', 'https://example.com/docs/guide/').href);
console.log('  ', new URL('/api?x=1', 'https://example.com/docs/guide/').href);

// 4) Encoding is automatic and correct
const search = new URLSearchParams({ q: 'Tom & Jerry', tag: 'c++', emoji: '✓' });
console.log('4)', search.toString());
console.log('   manual concat would break:', 'q=' + 'Tom & Jerry', '<- the & starts a new parameter');
console.log('   encodeURIComponent:', encodeURIComponent('Tom & Jerry/?'), '| encodeURI keeps / and ?:', encodeURI('/a b/?x=1&y=2'));

// 5) Validating user-supplied URLs (e.g. a profile "website" field)
function safeHttpUrl(input) {
  try {
    const url = new URL(input);
    return ['http:', 'https:'].includes(url.protocol) ? url.href : null;
  } catch { return null; }                              // not a URL at all
}
for (const s of ['https://ada.dev', 'javascript:alert(1)', 'not a url', 'data:text/html,<b>x</b>']) console.log('5)', s.padEnd(24), '->', safeHttpUrl(s));
console.log('   URL.canParse:', URL.canParse('https://x.y'), URL.canParse('nope'));

// 6) Form-encoded bodies and query objects
console.log('6)', Object.fromEntries(new URLSearchParams('a=1&b=two')), '|', new URLSearchParams([['k', 'v1'], ['k', 'v2']]).toString());
