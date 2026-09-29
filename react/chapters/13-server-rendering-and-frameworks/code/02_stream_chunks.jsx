// renderToPipeableStream: capture every chunk React writes and when it writes it.
import { Writable } from 'node:stream';
import { Suspense, use } from 'react';
import { renderToPipeableStream } from 'react-dom/server';

const t0 = Date.now(); const ms = () => String(Date.now() - t0).padStart(4) + ' ms';
const later = (value, delay) => new Promise(r => setTimeout(() => r(value), delay));

const pricePromise = later('$18', 100);            // fast data
const reviewsPromise = later(['Great mug', 'Too small'], 300); // slow data

function Price() { return <b>{use(pricePromise)}</b>; }
function Reviews() { return <ul>{use(reviewsPromise).map(r => <li key={r}>{r}</li>)}</ul>; }

function Page() {
  return (
    <main>
      <h1>Trail Mug</h1>
      <Suspense fallback={<i>price…</i>}><Price /></Suspense>
      <Suspense fallback={<i>reviews…</i>}><Reviews /></Suspense>
    </main>
  );
}

// A fake HTTP response that records chunks instead of sending them.
let n = 0;
const res = new Writable({
  write(chunk, _enc, done) {
    const text = chunk.toString()
      .replace(/<script>[\s\S]*?<\/script>/g, s => s.length > 60 ? `<script>/* ${s.length} chars of React's swap code */</script>` : s);
    console.log(`chunk ${++n} @ ${ms()}: ${text}`);
    done();
  },
  final(done) { console.log(`end      @ ${ms()}`); done(); },
});

const { pipe } = renderToPipeableStream(<Page />, {
  onShellReady() { console.log(`onShellReady @ ${ms()} -> pipe(res)`); pipe(res); },
  onAllReady() { console.log(`onAllReady   @ ${ms()}`); },
  onError(e) { console.error(e); },
});
