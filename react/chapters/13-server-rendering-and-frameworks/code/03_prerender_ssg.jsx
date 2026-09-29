// Static generation with prerenderToNodeStream (waits for ALL data), then
// partial prerendering: prerender what's ready, resume the rest later.
import { Suspense, use } from 'react';
import { prerenderToNodeStream, prerender } from 'react-dom/static';
import { resume } from 'react-dom/server';

const later = (value, delay) => new Promise(r => setTimeout(() => r(value), delay));
const text = async (stream) => { let s = ''; for await (const c of stream) s += typeof c === 'string' ? c : Buffer.from(c).toString(); return s; };

// --- 1. SSG: build-time HTML, no fallbacks left in it -------------------------------------
const posts = later(['Packing list for a weekend hike', 'Choosing a camping stove'], 150);
function PostList() { return <ul>{use(posts).map(p => <li key={p}>{p}</li>)}</ul>; }
function Blog() {
  return <main><h1>Blog</h1><Suspense fallback={<p>Loading…</p>}><PostList /></Suspense></main>;
}
const { prelude } = await prerenderToNodeStream(<Blog />);   // resolves only when everything is ready
console.log('SSG (prerenderToNodeStream):\n  ' + await text(prelude) + '\n');

// --- 2. Partial prerendering: static shell now, dynamic hole filled per request ------------
let cart = new Promise(() => {});                             // per-user data: not available at build time
function Cart() { return <p>Cart: {use(cart)}</p>; }
function Store() {
  return (
    <main>
      <h1>Store</h1><p>Free shipping over $50</p>
      <Suspense fallback={<p>Cart: …</p>}><Cart /></Suspense>
    </main>
  );
}
const controller = new AbortController();
setTimeout(() => controller.abort(), 50);                     // "build" stops waiting after 50 ms
const pre = await prerender(<Store />, { signal: controller.signal, onError() {} });
const shell = await text(pre.prelude);
console.log('PPR prelude (cache on a CDN):\n  ' + shell.replace(/<script>.*<\/script>/, '<script>…</script>'));
const saved = JSON.stringify(pre.postponed);                  // store with the prelude
console.log(`postponed state: ${saved.length} bytes of JSON\n`);

cart = Promise.resolve('3 items');                            // a later request: per-user data is available
const rest = await resume(<Store />, JSON.parse(saved));
console.log('resume() stream appended after the prelude:\n  '
  + (await text(rest)).replace(/<script>[\s\S]*<\/script>/, '<script>/* swap code */</script>'));
