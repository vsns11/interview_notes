// fetch against a local node:http server started in this same script (no internet needed).
import http from 'node:http';

const users = [{ id: 1, name: 'Ada' }];
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');
  const send = (status, body, headers = {}) => {
    res.writeHead(status, { 'Content-Type': 'application/json', ...headers });
    res.end(JSON.stringify(body));
  };
  if (req.method === 'GET' && url.pathname === '/api/users') return send(200, users, { 'X-Total-Count': String(users.length) });
  if (req.method === 'POST' && url.pathname === '/api/users') {
    let raw = '';
    for await (const chunk of req) raw += chunk;
    if (req.headers['content-type'] !== 'application/json') return send(415, { error: 'send JSON' });
    const user = { id: users.length + 1, ...JSON.parse(raw) };
    users.push(user);
    return send(201, user, { Location: `/api/users/${user.id}` });
  }
  if (url.pathname === '/api/slow') return setTimeout(() => send(200, { ok: true }), 2000);
  if (url.pathname === '/api/crash') return send(500, { error: 'database unavailable' });
  if (url.pathname === '/page') { res.writeHead(200, { 'Content-Type': 'text/html' }); return res.end('<!doctype html><h1>Not JSON</h1>'); }
  send(404, { error: `no route ${req.method} ${url.pathname}` });
});
await new Promise((r) => server.listen(0, '127.0.0.1', r));   // port 0 = any free port
const BASE = `http://127.0.0.1:${server.address().port}`;

// 1) GET + JSON
const res = await fetch(`${BASE}/api/users`);
console.log('1) status:', res.status, res.statusText, '| ok:', res.ok, '| type:', res.headers.get('content-type'), '| total:', res.headers.get('x-total-count'));
console.log('   body  :', await res.json());

// 2) POST JSON
const created = await fetch(`${BASE}/api/users`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ name: 'Grace' }),
});
console.log('2) POST ->', created.status, created.headers.get('location'), await created.json());

// 3) HTTP errors do NOT reject: check res.ok yourself
const notFound = await fetch(`${BASE}/api/nope`);
console.log('3) 404 resolved, ok =', notFound.ok, '| body:', (await notFound.json()).error);

// 4) A reusable helper that turns HTTP errors into exceptions
class HttpError extends Error {
  constructor(res, body) { super(`HTTP ${res.status} for ${res.url}`); this.name = 'HttpError'; this.status = res.status; this.body = body; }
}
async function getJSON(path, init) {
  const r = await fetch(`${BASE}${path}`, init);
  const isJson = r.headers.get('content-type')?.includes('application/json');
  const body = isJson ? await r.json() : await r.text();
  if (!r.ok) throw new HttpError(r, body);
  if (!isJson) throw new TypeError(`expected JSON, got ${r.headers.get('content-type')}`);
  return body;
}
for (const path of ['/api/users', '/api/crash', '/page']) {
  try { console.log('4)', path, '->', (await getJSON(path)).length, 'users'); }
  catch (e) { console.log('4)', path, '->', e.name, '|', e.status ?? '', e.body?.error ?? e.message); }
}

// 5) Network failure DOES reject (TypeError)
const closed = http.createServer();
await new Promise((r) => closed.listen(0, '127.0.0.1', r));
const deadPort = closed.address().port;
await new Promise((r) => closed.close(r));                        // nothing listens there now
await fetch(`http://127.0.0.1:${deadPort}/`).catch((e) => console.log('5) network error:', e.name, '-', e.message, '| cause:', e.cause?.code));

// 6) Timeout / cancel with AbortSignal
const t0 = performance.now();
await fetch(`${BASE}/api/slow`, { signal: AbortSignal.timeout(100) })
  .catch((e) => console.log('6) aborted:', e.name, `after ~${Math.round((performance.now() - t0) / 100) * 100} ms`));

// 7) A body can be read only once; clone() first if you need it twice
const r7 = await fetch(`${BASE}/api/users`);
const copy = r7.clone();
await r7.json();
await r7.text().catch((e) => console.log('7) second read:', e.name, '-', e.message));
console.log('   clone still readable:', (await copy.json()).length, 'users');

// 8) Query strings, other methods, Request/Headers objects
const q = new URL('/api/nope', BASE);
q.searchParams.set('search', 'Ada Lovelace & co');
console.log('8) url:', q.href.replace(BASE, ''));
const req = new Request(`${BASE}/api/users`, { method: 'DELETE', headers: new Headers({ Authorization: 'Bearer demo-token' }) });
console.log('   Request:', req.method, req.headers.get('authorization'), '->', (await fetch(req)).status);

server.close();
