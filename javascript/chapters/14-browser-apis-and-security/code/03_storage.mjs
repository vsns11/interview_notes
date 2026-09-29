import dom from '../../../../tools_dom.mjs';

// 1) localStorage: strings only, per origin, survives restarts
localStorage.setItem('theme', 'dark');
localStorage.count = 5;                              // property style works but is discouraged
console.log('1) getItem      :', localStorage.getItem('theme'), '| missing ->', localStorage.getItem('nope'));
console.log('   numbers become strings:', typeof localStorage.getItem('count'), JSON.stringify(localStorage.getItem('count')));
localStorage.setItem('user', { name: 'Ada' });       // WRONG: stores "[object Object]"
console.log('   object stored raw     :', localStorage.getItem('user'));

// 2) Store structured data as JSON, with a safe reader
const store = {
  get(key, fallback = null) {
    try { const raw = localStorage.getItem(key); return raw === null ? fallback : JSON.parse(raw); }
    catch { return fallback; }                          // corrupt value or storage disabled
  },
  set(key, value) {
    try { localStorage.setItem(key, JSON.stringify(value)); return true; }
    catch (e) { console.log('   set failed:', e.name); return false; }  // QuotaExceededError, private mode...
  },
};
store.set('cart', { items: [{ sku: 'A-1', qty: 2 }], updated: '2026-09-28' });
console.log('2) JSON round trip:', store.get('cart').items[0], '| corrupt ->', (localStorage.setItem('bad', '{oops'), store.get('bad', 'fallback')));

// 3) Enumerate, remove, clear
console.log('3) length:', localStorage.length, '| keys:', Array.from({ length: localStorage.length }, (_, i) => localStorage.key(i)));
localStorage.removeItem('count');
console.log('   after removeItem:', localStorage.length);

// 4) Quota: about 5 MB per origin (jsdom uses 5,000,000 characters like browsers)
store.set('huge', 'x'.repeat(6_000_000));

// 5) sessionStorage: same API, but per TAB and cleared when the tab closes
sessionStorage.setItem('wizardStep', '2');
console.log('5) sessionStorage:', sessionStorage.getItem('wizardStep'), '| separate from localStorage:', localStorage.getItem('wizardStep'));

// 6) clear() removes everything for this origin
localStorage.clear();
console.log('6) after clear():', localStorage.length);

// 7) Cookies from JavaScript: one string, "name=value; name2=value2"
document.cookie = 'lang=en; Path=/; Max-Age=86400; SameSite=Lax';
document.cookie = 'consent=yes; Path=/';
document.cookie = 'session=secret; HttpOnly';        // IGNORED: HttpOnly cookies can only be set by the server
console.log('7) document.cookie:', JSON.stringify(document.cookie));
const cookies = Object.fromEntries(document.cookie.split('; ').map((c) => c.split('=').map(decodeURIComponent)));
console.log('   parsed:', cookies);
document.cookie = 'lang=; Path=/; Max-Age=0';         // delete = expire it
console.log('   after delete:', JSON.stringify(document.cookie));
