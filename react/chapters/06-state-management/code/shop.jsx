// tools_run: skip  (component module: used by 08_bookshop.jsx and the browser entry shop-main.jsx)
// Each kind of state in its natural home:
//   server state (the catalog)      -> TanStack Query
//   global client state (the cart)  -> Zustand store
//   rarely-changing app setting     -> Context (currency)
//   local UI state (genre filter)   -> useState
import { createContext, useContext, useState } from 'react';
import { create } from 'zustand';
import { useShallow } from 'zustand/react/shallow';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';

export const BOOKS = [
  { id: 'b1', title: 'The Pragmatic Programmer', genre: 'software', price: 42 },
  { id: 'b2', title: 'Designing Data-Intensive Applications', genre: 'software', price: 48 },
  { id: 'b3', title: 'The Hobbit', genre: 'fiction', price: 12 },
  { id: 'b4', title: 'Dune', genre: 'fiction', price: 15 },
];
export const createCatalogApi = (latency = 30) => async () => { await new Promise((r) => setTimeout(r, latency)); return BOOKS; };

// ---- Zustand: cart store (usable from any component, no provider)
export const useCart = create((set) => ({
  lines: {},                                                   // { [bookId]: qty }
  add: (id) => set((s) => ({ lines: { ...s.lines, [id]: (s.lines[id] ?? 0) + 1 } })),
  remove: (id) => set((s) => { const { [id]: _, ...rest } = s.lines; return { lines: rest }; }),
  clear: () => set({ lines: {} }),
}));

// ---- Context: currency formatter chosen once at the top
const CurrencyContext = createContext({ code: 'USD', locale: 'en-US' });
function useMoney() {
  const { code, locale } = useContext(CurrencyContext);
  return (n) => new Intl.NumberFormat(locale, { style: 'currency', currency: code }).format(n);
}

// ---- TanStack Query: the catalog
function useBooks(fetchBooks) {
  return useQuery({ queryKey: ['books'], queryFn: fetchBooks, staleTime: 60_000 });
}

function Catalog({ fetchBooks }) {
  const [genre, setGenre] = useState('all');                  // local UI state
  const { data: books, isPending, isError, error } = useBooks(fetchBooks);
  const add = useCart((s) => s.add);                           // select just the action -> never re-renders for cart changes
  const money = useMoney();
  if (isPending) return <p className="muted">Loading catalog…</p>;
  if (isError) return <p role="alert">{error.message}</p>;
  const visible = genre === 'all' ? books : books.filter((b) => b.genre === genre);   // derived
  return (
    <section>
      <div className="filters" role="group" aria-label="Genre">
        {['all', 'software', 'fiction'].map((g) => <button key={g} aria-pressed={genre === g} onClick={() => setGenre(g)}>{g}</button>)}
      </div>
      <ul className="books">
        {visible.map((b) => (
          <li key={b.id}><span>{b.title}</span><span className="price">{money(b.price)}</span>
            <button aria-label={`Add ${b.title}`} onClick={() => add(b.id)}>Add</button></li>
        ))}
      </ul>
    </section>
  );
}

function Cart({ fetchBooks }) {
  const { lines, remove, clear } = useCart(useShallow((s) => ({ lines: s.lines, remove: s.remove, clear: s.clear })));
  const { data: books = [] } = useBooks(fetchBooks);            // same key -> same cache, no extra request
  const money = useMoney();
  const items = books.filter((b) => lines[b.id]).map((b) => ({ ...b, qty: lines[b.id] }));
  const total = items.reduce((sum, i) => sum + i.qty * i.price, 0);   // derived
  return (
    <aside className="cart" aria-label="Cart">
      <h2>Cart ({items.reduce((n, i) => n + i.qty, 0)})</h2>
      {items.length === 0 ? <p className="muted">Your cart is empty.</p> : (
        <ul>{items.map((i) => <li key={i.id}><span>{i.qty} × {i.title}</span><button aria-label={`Remove ${i.title}`} onClick={() => remove(i.id)}>✕</button></li>)}</ul>
      )}
      <p className="total">Total <strong>{money(total)}</strong></p>
      {items.length > 0 && <button className="secondary" onClick={clear}>Empty cart</button>}
    </aside>
  );
}

export default function Shop({ fetchBooks, queryClient = new QueryClient(), currency = { code: 'USD', locale: 'en-US' } }) {
  return (
    <QueryClientProvider client={queryClient}>
      <CurrencyContext value={currency}>
        <div className="shop">
          <header><h1>Corner Bookshop</h1></header>
          <Catalog fetchBooks={fetchBooks} />
          <Cart fetchBooks={fetchBooks} />
        </div>
      </CurrencyContext>
    </QueryClientProvider>
  );
}
