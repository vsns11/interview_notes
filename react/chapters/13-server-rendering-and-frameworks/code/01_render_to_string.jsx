// renderToString vs renderToStaticMarkup, and what the older string API does with <Suspense>.
import { Suspense, use, useState } from 'react';
import { renderToString, renderToStaticMarkup } from 'react-dom/server';

function Greeting({ name }) {
  const [likes] = useState(3);                      // state works: initial value is rendered
  return <p className="greet">Hello, {name}! Likes: {likes}</p>;
}

console.log('renderToString      :', renderToString(<Greeting name="Ada" />));
console.log('renderToStaticMarkup:', renderToStaticMarkup(<Greeting name="Ada" />));

// A component that suspends on data that is not ready yet.
const never = new Promise(() => {});               // never resolves
function Slow() { return <p>{use(never)}</p>; }
const page = (
  <main>
    <h1>Shop</h1>
    <Suspense fallback={<p>Loading…</p>}><Slow /></Suspense>
  </main>
);
const html = renderToString(page)
  .replace(/data-msg="([^"]*?)\n[^"]*"/, (_, first) => `data-msg="${first} …"`)  // shorten the dev-only message
  .replace(/ data-cstck="[^"]*"/, '');                                           // drop the dev-only stack
console.log('renderToString + pending Suspense:\n  ' + html);
