// Streaming SSR end to end: a node:http server streams HTML with renderToPipeableStream,
// headless Chrome loads it, we screenshot the shell (fallback) and the finished, hydrated page.
import http from 'node:http';
import path from 'node:path';
import { renderToPipeableStream } from 'react-dom/server';
import { build } from 'esbuild';
import puppeteer from 'puppeteer-core';
import App from './shop_app.jsx';

const REVIEWS = [
  { id: 1, stars: 5, text: 'Survived three winters of camping.', author: 'Asha', helpful: 4 },
  { id: 2, stars: 4, text: 'Handle gets hot over a fire.', author: 'Tom', helpful: 1 },
];
const slowReviews = () => new Promise(res => setTimeout(() => res(REVIEWS), 1500)); // pretend slow DB

// 1. Bundle the browser entry (hydrateRoot + the same App) in memory.
const bundle = await build({
  entryPoints: [path.resolve('ssr_client.jsx')], bundle: true, write: false, format: 'iife',
  jsx: 'automatic', minify: true, define: { 'process.env.NODE_ENV': '"production"' }, logLevel: 'error',
});
const clientJs = bundle.outputFiles[0].text;

// 2. The server.
const t0 = Date.now(); const log = (m) => console.log(`[${String(Date.now() - t0).padStart(4)} ms] ${m}`);
const server = http.createServer((req, res) => {
  if (req.url === '/client.js') { res.setHeader('content-type', 'text/javascript'); return res.end(clientJs); }
  if (req.url === '/api/reviews') { res.setHeader('content-type', 'application/json'); return res.end(JSON.stringify(REVIEWS)); }
  log(`server: GET ${req.url} -> start rendering`);
  let didError = false;
  const { pipe, abort } = renderToPipeableStream(<App reviewsPromise={slowReviews()} />, {
    bootstrapScripts: ['/client.js'],                        // React adds <script src="/client.js" async>
    onShellReady() {                                         // everything outside <Suspense> is ready
      log('server: onShellReady -> send shell (with fallback) now');
      res.statusCode = didError ? 500 : 200;
      res.setHeader('content-type', 'text/html; charset=utf-8');
      pipe(res);
    },
    onAllReady() { log('server: onAllReady -> reviews streamed, response ends'); },
    onShellError() { res.statusCode = 500; res.end('<h1>Something went wrong</h1>'); },
    onError(err) { didError = true; console.error(err); },
  });
  setTimeout(abort, 10_000);                                 // never hang forever on a slow data source
});
await new Promise(r => server.listen(0, '127.0.0.1', r));
const url = `http://127.0.0.1:${server.address().port}/`;

// 3. A real browser.
const browser = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: true });
try {
  const page = await browser.newPage();
  await page.setViewport({ width: 700, height: 470, deviceScaleFactor: 2 });
  page.on('console', m => log(`browser console: ${m.text()}`));
  const loaded = page.goto(url, { waitUntil: 'load' });      // don't wait: we want to see the page mid-stream
  await page.waitForSelector('#reviews-fallback');
  log('browser: shell painted, reviews fallback visible');
  await page.waitForFunction(() => window.__hydrated === true);
  await page.click('main section:first-child button');       // is the shell interactive already?
  log(`browser: shell hydrated while reviews still loading -> ${await page.$eval('button', b => b.textContent)}, `
    + `fallback still shown: ${!!(await page.$('#reviews-fallback'))}`);
  await page.screenshot({ path: '../img/ssr_1_shell.png' });

  await page.waitForSelector('.review');
  await loaded;
  log('browser: streamed reviews swapped in place of the fallback');
  await page.click('.review button');                        // the streamed part hydrates too
  const texts = await page.$$eval('button', bs => bs.map(b => b.textContent));
  log(`browser: after clicking Helpful -> ${JSON.stringify(texts)}`);
  await page.screenshot({ path: '../img/ssr_2_hydrated.png' });
} finally {
  await browser.close(); server.close();
}
