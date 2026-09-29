// What the browser receives first in a client-rendered SPA vs a server-rendered page, then hydration.
import '../../../../tools_dom.mjs';
import { renderToString } from 'react-dom/server';
import { hydrateRoot } from 'react-dom/client';
import { act, useState } from 'react';

function App() {
  const [n, setN] = useState(0);
  return <main><h1>Shop</h1><button onClick={() => setN(n + 1)}>Cart: {n}</button></main>;
}

// 1. CSR / SPA: the HTML file is an empty shell; JavaScript builds everything in the browser.
console.log('CSR first HTML :', '<div id="root"></div>  (+ <script src="bundle.js">)');

// 2. SSR: the server runs the same component and sends finished HTML.
const html = renderToString(<App />);
console.log('SSR first HTML :', `<div id="root">${html}</div>`);

// 3. Hydration: in the browser React attaches event handlers to that existing HTML.
document.getElementById('root').innerHTML = html;
const button = document.querySelector('button');
button.click();
console.log('click before hydration ->', button.textContent, '(nothing happens yet)');
await act(() => { hydrateRoot(document.getElementById('root'), <App />); });
await act(() => button.click());
console.log('click after hydration  ->', document.querySelector('button').textContent,
  '| same DOM node reused:', document.querySelector('button') === button);
