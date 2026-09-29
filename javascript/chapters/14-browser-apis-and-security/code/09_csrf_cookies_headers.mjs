// Session cookies, CSRF tokens and security headers on a local node:http server.
// Node's fetch has no cookie jar, so we play "the browser" by hand and show what it would send.
import http from 'node:http';
import { randomBytes, timingSafeEqual } from 'node:crypto';

const sessions = new Map();                                  // sid -> { user, csrf }
const SECURITY_HEADERS = {
  'Content-Security-Policy': "default-src 'self'; script-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'",
  'X-Content-Type-Options': 'nosniff',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
};
const parseCookies = (h = '') => Object.fromEntries(h.split(/;\s*/).filter(Boolean).map((c) => c.split('=')));
const same = (a = '', b = '') => a.length === b.length && timingSafeEqual(Buffer.from(a), Buffer.from(b));

const server = http.createServer(async (req, res) => {
  const reply = (status, body, extra = {}) => { res.writeHead(status, { 'Content-Type': 'application/json', ...SECURITY_HEADERS, ...extra }); res.end(JSON.stringify(body)); };
  const session = sessions.get(parseCookies(req.headers.cookie).sid);

  if (req.method === 'POST' && req.url === '/login') {
    const sid = randomBytes(16).toString('hex');
    const csrf = randomBytes(16).toString('hex');
    sessions.set(sid, { user: 'ada', csrf });
    return reply(200, { csrf }, {                          // token goes to the page, cookie to the browser
      'Set-Cookie': `sid=${sid}; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=3600`,
    });
  }
  if (req.method === 'POST' && req.url === '/transfer') {
    if (!session) return reply(401, { error: 'not logged in' });
    if (!same(req.headers['x-csrf-token'], session.csrf)) return reply(403, { error: 'CSRF token missing or wrong' });
    return reply(200, { ok: true, by: session.user });
  }
  reply(404, { error: 'not found' });
});
await new Promise((r) => server.listen(0, '127.0.0.1', r));
const BASE = `http://127.0.0.1:${server.address().port}`;

// 1) Login: the server sets an HttpOnly session cookie and returns a CSRF token
const login = await fetch(`${BASE}/login`, { method: 'POST' });
const setCookie = login.headers.get('set-cookie');
const { csrf } = await login.json();
console.log('1) Set-Cookie:', setCookie.replace(/sid=\w+/, 'sid=<random>'));
console.log('   CSP       :', login.headers.get('content-security-policy'));
const cookie = setCookie.split(';')[0];                    // what the browser would send back

// 2) Forged request from evil.example: the browser attaches the cookie (if SameSite allows) but
//    the attacker cannot read our page, so it cannot know the CSRF token
const forged = await fetch(`${BASE}/transfer`, { method: 'POST', headers: { Cookie: cookie } });
console.log('2) forged request  ->', forged.status, (await forged.json()).error);

// 3) Our own page sends the token in a header
const legit = await fetch(`${BASE}/transfer`, { method: 'POST', headers: { Cookie: cookie, 'X-CSRF-Token': csrf } });
console.log('3) legit request   ->', legit.status, await legit.json());

// 4) No cookie at all (e.g. SameSite=Strict/Lax blocked it on a cross-site POST)
const noCookie = await fetch(`${BASE}/transfer`, { method: 'POST', headers: { 'X-CSRF-Token': csrf } });
console.log('4) without cookie  ->', noCookie.status, (await noCookie.json()).error);
server.close();
