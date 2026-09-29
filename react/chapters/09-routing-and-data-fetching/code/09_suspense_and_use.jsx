// Suspense + use(): read a promise during render, show a fallback while it is pending,
// catch rejections with an error boundary, and keep old content visible with a transition.
import '../../../../tools_dom.mjs';
import { Suspense, use, useState, useTransition, createContext } from 'react';
import { QueryClient, QueryClientProvider, useSuspenseQuery } from '@tanstack/react-query';
import { createRoot } from 'react-dom/client';
import { screen, fireEvent, waitFor } from '@testing-library/dom';   // no act(): let React schedule work like a browser
import { ErrorBoundary } from './ErrorBoundary.jsx';
import { startApi } from './api-server.js';

const api = await startApi({ delayMs: 40 });
console.error = () => {};                                // hide React's logging of the caught error below
globalThis.IS_REACT_ACT_ENVIRONMENT = false;             // we drive React like a real browser here, not inside act()

// A promise must be CREATED ONCE and reused across renders, so cache it (here: by id).
const cache = new Map();
function fetchProduct(id) {
  if (!cache.has(id)) cache.set(id, fetch(`${api.base}/api/products/${id}`).then(async r => {
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.json();
  }));
  return cache.get(id);
}

const CurrencyContext = createContext('$');
function Product({ id, showPrice }) {
  const product = use(fetchProduct(id));                 // suspends until resolved; throws to the boundary if rejected
  if (!showPrice) return <h2>{product.name}</h2>;
  const currency = use(CurrencyContext);                 // unlike useContext, use() may sit inside an if
  return <h2>{product.name} {currency}{product.price}</h2>;
}

function Page() {
  const [id, setId] = useState(1);
  const [isPending, startTransition] = useTransition();
  return (
    <>
      <button onClick={() => setId(2)}>plain setId(2)</button>
      <button onClick={() => startTransition(() => setId(3))}>transition setId(3)</button>
      <button onClick={() => setId(9)}>missing</button>
      {isPending && <p>(switching…)</p>}
      <ErrorBoundary fallback={({ error, reset }) => <p role="alert">Failed: {error.message} <button onClick={() => { reset(); setId(1); }}>Retry</button></p>}>
        <Suspense fallback={<p>Loading product…</p>}>
          <Product id={id} showPrice />
        </Suspense>
      </ErrorBoundary>
    </>
  );
}

const text = () => {                                     // what is VISIBLE: React hides suspended content with display:none
  const copy = document.getElementById('root').cloneNode(true);
  copy.querySelectorAll('button, [style*="display: none"]').forEach(n => n.remove());
  return [...copy.children].map(n => n.textContent).join(' ');
};
const root = createRoot(document.getElementById('root'));
root.render(<Page />);
await new Promise(r => setTimeout(r, 5));
console.log('first render      :', text());
await waitFor(() => screen.getByText(/Keyboard/));
console.log('resolved          :', text());
fireEvent.click(screen.getByText('plain setId(2)'));
await new Promise(r => setTimeout(r, 5));
console.log('plain update      :', text(), '<- content replaced by the fallback');
await waitFor(() => screen.getByText(/Mouse/));
fireEvent.click(screen.getByText('transition setId(3)'));
await new Promise(r => setTimeout(r, 5));
console.log('transition update :', text(), '<- old content kept, isPending shown');
await waitFor(() => screen.getByText(/Monitor/));
console.log('then              :', text());
fireEvent.click(screen.getByText('missing'));
await waitFor(() => screen.getByRole('alert'));
console.log('rejected promise  :', screen.getByRole('alert').textContent);
fireEvent.click(screen.getByText('Retry'));
await waitFor(() => screen.getByText(/Keyboard/));
console.log('after Retry       :', text());

// The same idea with TanStack Query: useSuspenseQuery never returns "pending" - data is always defined.
function SuspenseName({ id }) {
  const { data } = useSuspenseQuery({ queryKey: ['product', id], queryFn: () => fetchProduct(id) });
  return <p>useSuspenseQuery: {data.name}</p>;
}
const client = new QueryClient();
root.render(<QueryClientProvider client={client}><Suspense fallback={<p>fallback…</p>}><SuspenseName id={2} /></Suspense></QueryClientProvider>);
await waitFor(() => screen.getByText(/useSuspenseQuery/));
console.log(screen.getByText(/useSuspenseQuery/).textContent);
root.unmount(); client.clear(); api.close();
