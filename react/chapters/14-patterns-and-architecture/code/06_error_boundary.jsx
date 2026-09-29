// Error boundaries: what they catch, what they don't, reset, and the React 19 root error callbacks.
import '../../../../tools_dom.mjs';
import { act, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ErrorBoundary } from './ErrorBoundary.jsx';

const container = document.getElementById('root');
const area = (id) => container.querySelector(`#${id}`)?.textContent ?? '(nothing rendered)';
const click = (label) => act(async () => {
  [...container.querySelectorAll('button')].find(b => b.textContent === label).click();
  await new Promise(r => setTimeout(r, 0));                        // let promise callbacks run
});

// React 19: root-level callbacks (for logging/reporting) instead of default console output.
const rootOptions = {
  onCaughtError(error) { console.log(`   onCaughtError   : "${error.message}"`); },
  onUncaughtError(error) { console.log(`   onUncaughtError : "${error.message}" -> whole root unmounted`); },
};
const root = createRoot(container, rootOptions);

function ProductPrice({ product }) {
  return <p>Price: ${product.price.toFixed(2)}</p>;              // throws if product is null
}

function Reviews() {
  const [error, setError] = useState(null);
  if (error) throw error;                                          // re-throw during render -> boundary sees it
  return <button onClick={() => {
    Promise.reject(new Error('network down'))                      // async errors are NOT caught by boundaries…
      .catch(e => setError(e));                                    // …unless you move them into render like this
  }}>Load reviews</button>;
}

const Fallback = ({ error, reset }) => (
  <div role="alert">Could not show this part ({error.message}). <button onClick={reset}>Try again</button></div>
);

function Shop() {
  const [product, setProduct] = useState({ price: 18 });
  const [id, setId] = useState(1);
  const [importMsg, setImportMsg] = useState('');
  return (
    <>
      <div id="price">
        <ErrorBoundary resetKeys={[id]} fallback={(p) => <Fallback {...p} />}
          onError={(e, info) => console.log('   componentDidCatch: stack starts', info.componentStack.trim().split('\n')[0].split(' (')[0])}>
          <ProductPrice product={product} />
        </ErrorBoundary>
      </div>
      <div id="reviews"><ErrorBoundary fallback={(p) => <Fallback {...p} />}><Reviews /></ErrorBoundary></div>
      <button onClick={() => setProduct(null)}>Break price</button>
      <button onClick={() => { setProduct({ price: 21 }); setId(i => i + 1); }}>Next product</button>
      <button onClick={() => {
        try { JSON.parse('{bad json'); }                           // event-handler errors are NOT caught by boundaries
        catch { setImportMsg('Could not read the file'); }         // handle them yourself
      }}>Import</button>
      <p id="import">{importMsg}</p>
    </>
  );
}

await act(() => root.render(<Shop />));
console.log('1. start              :', area('price'));
await click('Break price');
console.log('2. render error       :', area('price'));
await click('Try again');
console.log('3. reset, still broken:', area('price'));
await click('Next product');
console.log('4. resetKeys changed  :', area('price'));
await click('Import');
console.log('5. handler error      :', area('import'), '| price still fine:', area('price'));
await click('Load reviews');
console.log('6. async error        :', area('reviews'), '| price still fine:', area('price'));

// 7. No boundary at all: React 19 unmounts the whole root.
const box = document.body.appendChild(document.createElement('div'));
const root2 = createRoot(box, rootOptions);
try { await act(() => root2.render(<><h1>Shop</h1><ProductPrice product={null} /></>)); }
catch { /* act() re-throws uncaught errors in tests; a browser would not */ }
console.log('7. no boundary        : container HTML =', JSON.stringify(box.innerHTML));
