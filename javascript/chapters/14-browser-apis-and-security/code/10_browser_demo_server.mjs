// Two local origins for the browser demos in this chapter:
//   http://localhost:4101  "the site"  - serves the .html/.js pages in this folder (csp.html gets a CSP header)
//   http://localhost:4102  "the API"   - a DIFFERENT origin (other port), used by cors.html
// `node 10_browser_demo_server.mjs --serve` keeps both running for the screenshots.
// Without --serve (as the book's runner does) it checks the CORS/CSP headers with Node's fetch and exits.
import http from 'node:http';
import { readFile } from 'node:fs/promises';
import { extname } from 'node:path';

const SITE = 'http://localhost:4101';
const log = [];                                           // every request the API receives

const site = http.createServer(async (req, res) => {
  const path = new URL(req.url, SITE).pathname.slice(1) || 'cors.html';
  if (!/^[\w.-]+\.(html|js)$/.test(path)) { res.writeHead(404); return res.end(); }  // no ../ tricks
  try {
    const body = await readFile(new URL(path, import.meta.url));
    const headers = { 'Content-Type': extname(path) === '.js' ? 'text/javascript' : 'text/html; charset=utf-8' };
    if (path === 'csp.html') headers['Content-Security-Policy'] = "default-src 'self'; script-src 'self'; img-src 'self'; style-src 'self' 'unsafe-inline'";
    res.writeHead(200, headers); res.end(body);
  } catch { res.writeHead(404); res.end(); }
});

const api = http.createServer((req, res) => {
  const path = new URL(req.url, 'http://localhost:4102').pathname;
  if (path !== '/log') log.push(`${req.method} ${path}${req.headers['access-control-request-method'] ? ` (preflight for ${req.headers['access-control-request-method']})` : ''}`);
  const allow = { 'Access-Control-Allow-Origin': SITE, Vary: 'Origin' };
  const json = (status, body, headers = {}) => { res.writeHead(status, { 'Content-Type': 'application/json', ...headers }); res.end(JSON.stringify(body)); };

  if (req.method === 'OPTIONS') {                          // preflight: allow POST with a JSON body, but not PUT
    return res.writeHead(204, { ...allow, 'Access-Control-Allow-Methods': 'GET, POST', 'Access-Control-Allow-Headers': 'Content-Type', 'Access-Control-Max-Age': '600' }).end();
  }
  if (path === '/public') return json(200, { msg: 'hello from another origin' }, allow);
  if (path === '/private') return json(200, { secret: 'no CORS header on this one' });   // browser will hide it
  if (path === '/data') return json(200, { saved: true, method: req.method }, allow);
  if (path === '/log') return json(200, log, allow);
  json(404, {}, allow);
});

await Promise.all([new Promise((r) => site.listen(4101, r)), new Promise((r) => api.listen(4102, r))]);

if (process.argv.includes('--serve')) {
  console.log('serving', SITE, 'and http://localhost:4102 - Ctrl+C to stop');
} else {
  // Node's fetch does not enforce CORS (only browsers do) - so we can look at the raw headers.
  const pre = await fetch('http://localhost:4102/data', {
    method: 'OPTIONS', headers: { Origin: SITE, 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'content-type' },
  });
  console.log('preflight ->', pre.status, Object.fromEntries([...pre.headers].filter(([k]) => k.startsWith('access-control'))));
  const pub = await fetch('http://localhost:4102/public', { headers: { Origin: SITE } });
  const priv = await fetch('http://localhost:4102/private', { headers: { Origin: SITE } });
  console.log('/public  ACAO:', pub.headers.get('access-control-allow-origin'));
  console.log('/private ACAO:', priv.headers.get('access-control-allow-origin'), '| Node still reads it:', (await priv.json()).secret);
  const csp = await fetch(`${SITE}/csp.html`);
  console.log('csp.html CSP :', csp.headers.get('content-security-policy'));
  site.close(); api.close();
}
