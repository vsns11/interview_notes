// Refactoring: one component doing everything -> custom Hook (logic) + presentational component (UI).
import '../../../../tools_dom.mjs';
import { render, renderHook, screen, cleanup, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect, useState } from 'react';

// A fake API (would be fetch in a real app).
const PRODUCTS = ['Trail Mug', 'Trail Stove', 'Camp Chair', 'Camp Lantern'];
const api = { search: (q) => new Promise(r => setTimeout(() => r(PRODUCTS.filter(p => p.toLowerCase().includes(q.toLowerCase()))), 20)) };

// ---------------- BEFORE: data logic, effects and markup tangled together ----------------
function ProductSearchBefore() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [status, setStatus] = useState('idle');
  useEffect(() => {
    if (!query) { setResults([]); setStatus('idle'); return; }
    let ignore = false;
    setStatus('loading');
    api.search(query).then(r => { if (!ignore) { setResults(r); setStatus('done'); } });
    return () => { ignore = true; };
  }, [query]);
  return (
    <section>
      <label>Search <input value={query} onChange={e => setQuery(e.target.value)} /></label>
      {status === 'loading' && <p role="status">Searching…</p>}
      {status === 'done' && <ul>{results.map(r => <li key={r}>{r}</li>)}</ul>}
    </section>
  );
}

// ---------------- AFTER: 1) a custom Hook owns the logic -----------------------------------
export function useProductSearch(query, search = api.search) {
  const [state, setState] = useState({ status: 'idle', results: [] });
  useEffect(() => {
    if (!query) { setState({ status: 'idle', results: [] }); return; }
    let ignore = false;                                          // ignore stale responses (Chapter 5)
    setState(s => ({ ...s, status: 'loading' }));
    search(query).then(results => { if (!ignore) setState({ status: 'done', results }); });
    return () => { ignore = true; };
  }, [query, search]);
  return state;
}

// 2) a presentational component: props in, markup out — no effects, no fetching
export function ProductSearchView({ query, onQueryChange, status, results }) {
  return (
    <section>
      <label>Search <input value={query} onChange={e => onQueryChange(e.target.value)} /></label>
      {status === 'loading' && <p role="status">Searching…</p>}
      {status === 'done' && <ul>{results.map(r => <li key={r}>{r}</li>)}</ul>}
    </section>
  );
}

// 3) a thin "container" wiring them together
function ProductSearch() {
  const [query, setQuery] = useState('');
  const { status, results } = useProductSearch(query);
  return <ProductSearchView query={query} onQueryChange={setQuery} status={status} results={results} />;
}

// ---------------- Same behaviour before and after ---------------------------------------
const user = userEvent.setup();
for (const [name, C] of [['before', ProductSearchBefore], ['after ', ProductSearch]]) {
  render(<C />);
  await user.type(screen.getByLabelText('Search'), 'trail');
  await waitFor(() => screen.getByRole('list'));
  console.log(`${name}: ${screen.getAllByRole('listitem').map(li => li.textContent).join(', ')}`);
  cleanup();
}

// ---------------- The pieces can now be tested (and reused) separately --------------------
const fakeSearch = async (q) => [`${q} result`];
const { result, rerender } = renderHook(({ q }) => useProductSearch(q, fakeSearch), { initialProps: { q: '' } });
console.log('hook, empty query :', JSON.stringify(result.current));
rerender({ q: 'mug' });
await waitFor(() => { if (result.current.status !== 'done') throw new Error('wait'); });
console.log('hook, "mug"       :', JSON.stringify(result.current));

render(<ProductSearchView query="x" onQueryChange={() => {}} status="loading" results={[]} />);
console.log('view in loading   :', screen.getByRole('status').textContent, '(no API needed)');
