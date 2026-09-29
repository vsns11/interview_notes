// Redux Toolkit: one store, "slices" of state with reducers written in mutating style (Immer), selectors, thunks.
import '../../../../tools_dom.mjs';
import { configureStore, createSlice, createAsyncThunk, createSelector } from '@reduxjs/toolkit';
import { Provider, useSelector, useDispatch } from 'react-redux';
import { render, screen, fireEvent, act } from '@testing-library/react';

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const fakeApi = { async fetchProducts() { await wait(20); return [{ id: 'p1', name: 'Notebook', price: 4 }, { id: 'p2', name: 'Pen', price: 2 }]; } };

// ---- features/products/productsSlice.js : async data with createAsyncThunk
export const fetchProducts = createAsyncThunk('products/fetch', () => fakeApi.fetchProducts());
const productsSlice = createSlice({
  name: 'products',
  initialState: { items: [], status: 'idle', error: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => { state.status = 'loading'; })
      .addCase(fetchProducts.fulfilled, (state, action) => { state.status = 'succeeded'; state.items = action.payload; })
      .addCase(fetchProducts.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; });
  },
});

// ---- features/cart/cartSlice.js : "mutating" code is safe here — Immer produces a new immutable state
const cartSlice = createSlice({
  name: 'cart',
  initialState: { lines: {} },                         // { [productId]: quantity }
  reducers: {
    added(state, action) { const id = action.payload; state.lines[id] = (state.lines[id] ?? 0) + 1; },
    removed(state, action) { delete state.lines[action.payload]; },
    cleared() { return { lines: {} }; },               // or return a brand-new state
  },
});
export const { added, removed, cleared } = cartSlice.actions;   // action creators generated for you

// ---- selectors: read and derive; createSelector memoises derived data
const selectLines = (state) => state.cart.lines;
const selectProducts = (state) => state.products.items;
export const selectCartTotal = createSelector([selectLines, selectProducts], (lines, products) => {
  totalComputations++;
  return products.reduce((sum, p) => sum + (lines[p.id] ?? 0) * p.price, 0);
});
let totalComputations = 0;

// ---- app/store.js
export const makeStore = () => configureStore({ reducer: { products: productsSlice.reducer, cart: cartSlice.reducer } });
const store = makeStore();

// ---- components
const renders = {};
function ProductList() {
  renders.ProductList = (renders.ProductList ?? 0) + 1;
  const { items, status } = useSelector((s) => s.products);
  const dispatch = useDispatch();
  if (status === 'loading') return <p>Loading…</p>;
  return <ul>{items.map((p) => <li key={p.id}>{p.name} ${p.price} <button onClick={() => dispatch(added(p.id))}>add {p.name}</button></li>)}</ul>;
}
function CartTotal() {
  renders.CartTotal = (renders.CartTotal ?? 0) + 1;
  const total = useSelector(selectCartTotal);          // re-renders only when THIS value changes
  return <p>Total: ${total}</p>;
}
function App() { return <><ProductList /><CartTotal /></>; }

render(<Provider store={store}><App /></Provider>);
console.log('initial state :', JSON.stringify(store.getState()));
await act(() => store.dispatch(fetchProducts()));      // thunks can be dispatched from anywhere
console.log('after fetch   :', screen.getAllByRole('listitem').map((li) => li.firstChild.textContent).join(', '), '|', store.getState().products.status);
for (const k in renders) delete renders[k];
fireEvent.click(screen.getByText('add Notebook'));
fireEvent.click(screen.getByText('add Notebook'));
fireEvent.click(screen.getByText('add Pen'));
console.log('cart lines    :', JSON.stringify(store.getState().cart.lines), '|', screen.getByText(/Total/).textContent);
console.log('renders       :', JSON.stringify(renders), '(ProductList does not re-render: the products slice did not change)');
const before = totalComputations;
selectCartTotal(store.getState()); selectCartTotal(store.getState());
console.log('memoised selector recomputed on repeat calls:', totalComputations - before, 'times');
act(() => { store.dispatch(cleared()); });
console.log('after cleared :', screen.getByText(/Total/).textContent, '| action object:', JSON.stringify(added('p9')));
