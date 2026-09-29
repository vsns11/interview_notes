// tools_run: skip  (component module shared by the server in 04_ssr_server_hydrate.jsx and the browser in ssr_client.jsx)
// The SAME component tree renders on the server (to HTML) and in the browser (to hydrate that HTML).
import { Suspense, use, useEffect, useState } from 'react';

const css = `
  body { font: 16px/1.5 system-ui, sans-serif; margin: 0; background: #f6f7f9; color: #1d2330; }
  header { background: #1d2330; color: #fff; padding: 10px 20px; font-weight: 600; }
  main { max-width: 620px; margin: 16px auto; padding: 0 16px; }
  .card { background: #fff; border: 1px solid #dde1e7; border-radius: 10px; padding: 14px 18px; margin-bottom: 14px; }
  button { font: inherit; padding: 6px 12px; border-radius: 6px; border: 1px solid #3553c9; background: #3553c9; color: #fff; cursor: pointer; }
  .review button { background: #fff; color: #3553c9; font-size: 14px; padding: 2px 10px; }
  .skeleton { color: #7a8394; font-style: italic; }
  .skeleton div { height: 14px; background: #e4e7ec; border-radius: 4px; margin: 8px 0; }
`;

function AddToCart() {
  const [count, setCount] = useState(0);
  useEffect(() => { window.__hydrated = true; }, []);   // Effects never run on the server: this proves hydration happened
  return <button onClick={() => setCount(c => c + 1)}>Add to cart ({count})</button>;
}

function Review({ r }) {
  const [helpful, setHelpful] = useState(r.helpful);
  return (
    <li className="review">
      <b>{'★'.repeat(r.stars)}</b> {r.text} — <i>{r.author}</i>{' '}
      <button onClick={() => setHelpful(h => h + 1)}>Helpful ({helpful})</button>
    </li>
  );
}

// Suspends until the reviews promise resolves (server: a slow "database"; browser: a fetch).
function Reviews({ reviewsPromise }) {
  const reviews = use(reviewsPromise);
  return <ul>{reviews.map(r => <Review key={r.id} r={r} />)}</ul>;
}

function ReviewsSkeleton() {
  return <div className="skeleton" id="reviews-fallback">Loading reviews…<div /><div /></div>;
}

export default function App({ reviewsPromise }) {
  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <link rel="icon" href="data:," />
        <title>Trail Mug — Shop</title>
        <style>{css}</style>
      </head>
      <body>
        <header>Hill &amp; Trail Outfitters</header>
        <main>
          <section className="card">
            <h1>Trail Mug</h1>
            <p>Enamel camping mug, 350 ml. <b>$18</b></p>
            <AddToCart />
          </section>
          <section className="card">
            <h2>Reviews</h2>
            <Suspense fallback={<ReviewsSkeleton />}>
              <Reviews reviewsPromise={reviewsPromise} />
            </Suspense>
          </section>
        </main>
      </body>
    </html>
  );
}
