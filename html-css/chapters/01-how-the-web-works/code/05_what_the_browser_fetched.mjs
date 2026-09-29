// Serve site/ over HTTP, open it in real headless Chrome, and list every request/response —
// the same information DevTools' Network panel shows.
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import puppeteer from 'puppeteer-core';

const types = { '.html': 'text/html; charset=utf-8', '.css': 'text/css', '.js': 'text/javascript' };
const server = http.createServer((req, res) => {
  const file = path.join('site', decodeURIComponent(new URL(req.url, 'http://x').pathname));
  if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) { res.writeHead(404); return res.end(); }
  res.writeHead(200, { 'Content-Type': types[path.extname(file)] ?? 'application/octet-stream' });
  fs.createReadStream(file).pipe(res);
});
await new Promise(r => server.listen(0, '127.0.0.1', r));
const origin = `http://127.0.0.1:${server.address().port}`;

const browser = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: 'new' });
const page = await browser.newPage();
const lines = [];
page.on('response', r => lines.push(`${r.status()}  ${r.request().resourceType().padEnd(10)} ${r.url().replace(origin, '').padEnd(15)} ${r.headers()['content-type'] ?? ''}`));
await page.goto(origin + '/index.html', { waitUntil: 'networkidle0' });
console.log('status type       path            content-type');
console.log(lines.sort((a, b) => a.includes('favicon') - b.includes('favicon')).join('\n'));

console.log('\nAfter load:', await page.evaluate(() => ({
  title: document.title,
  elements: document.querySelectorAll('*').length,
  h1Color: getComputedStyle(document.querySelector('h1')).color,   // proves the CSS was applied
})));
await page.click('#hi');                                             // proves the JavaScript runs
console.log('After clicking "Say hi":', await page.$eval('#msg', el => el.textContent));
await browser.close(); server.close();
