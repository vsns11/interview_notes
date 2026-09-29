// tools_run: skip  (helper) — a tiny in-memory JSON API on 127.0.0.1 for this chapter's examples.
import http from 'node:http';

export async function startApi({ delayMs = 30, log = null } = {}) {
  const db = {
    products: [
      { id: 1, name: 'Keyboard', price: 49, category: 'input' },
      { id: 2, name: 'Mouse', price: 19, category: 'input' },
      { id: 3, name: 'Monitor', price: 199, category: 'display' },
    ],
    reviews: { 1: ['Clicky!', 'Great keys'], 2: ['Fits my hand'], 3: [] },
    todos: [{ id: 1, title: 'Read chapter 9', done: false }],
  };
  let nextTodo = 2;
  const stats = { requests: [] };
  const server = http.createServer(async (req, res) => {
    const url = new URL(req.url, 'http://x');
    const key = `${req.method} ${url.pathname}${url.search}`;
    stats.requests.push(key);
    log?.(`  [server] start ${key}`);
    let body = '';
    for await (const chunk of req) body += chunk;
    await new Promise(r => setTimeout(r, url.searchParams.has('slow') ? delayMs * 4 : delayMs));
    const send = (status, data) => {
      log?.(`  [server] end   ${key} -> ${status}`);
      res.writeHead(status, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(data));
    };
    let m;
    if (url.pathname === '/api/products') {
      const page = Number(url.searchParams.get('page') ?? 1), size = 2;
      const all = db.products.map(({ id, name, price }) => ({ id, name, price }));
      return send(200, url.searchParams.has('page') ? { items: all.slice((page - 1) * size, page * size), hasMore: page * size < all.length } : all);
    }
    if ((m = url.pathname.match(/^\/api\/products\/(\d+)$/))) {
      const p = db.products.find(p => p.id === +m[1]);
      return p ? send(200, p) : send(404, { message: `No product ${m[1]}` });
    }
    if ((m = url.pathname.match(/^\/api\/products\/(\d+)\/reviews$/))) return send(200, db.reviews[m[1]] ?? []);
    if (url.pathname === '/api/categories') return send(200, ['input', 'display']);
    if (url.pathname === '/api/todos' && req.method === 'GET') return send(200, db.todos);
    if (url.pathname === '/api/todos' && req.method === 'POST') {
      const { title } = JSON.parse(body);
      if (title.toLowerCase().includes('fail')) return send(500, { message: 'Server refused' });
      const todo = { id: nextTodo++, title, done: false }; db.todos.push(todo); return send(201, todo);
    }
    if (url.pathname === '/api/flaky') return send(stats.requests.filter(r => r.includes('flaky')).length < 3 ? 503 : 200, { ok: true });
    send(404, { message: 'Not found' });
  });
  await new Promise(r => server.listen(0, '127.0.0.1', r));
  return { base: `http://127.0.0.1:${server.address().port}`, stats, db, close: () => { server.closeAllConnections(); server.close(); } };
}
