// Components that need providers (context, router): a custom render() with a wrapper.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { createContext, useContext, useReducer } from 'react';
import { MemoryRouter, Routes, Route, Link, useParams } from 'react-router';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

// --- app code ---
export function cartReducer(state, action) {             // pure function: unit-test it directly
  switch (action.type) {
    case 'add': return { ...state, [action.sku]: (state[action.sku] ?? 0) + 1 };
    case 'clear': return {};
    default: throw new Error(`unknown action ${action.type}`);
  }
}
const CartContext = createContext(null);
function CartProvider({ children, initial = {} }) {
  const [cart, dispatch] = useReducer(cartReducer, initial);
  return <CartContext value={{ cart, dispatch }}>{children}</CartContext>;
}
const useCart = () => useContext(CartContext);

function CartBadge() {
  const { cart } = useCart();
  const n = Object.values(cart).reduce((a, b) => a + b, 0);
  return <span aria-label="items in cart">{n}</span>;
}
function ProductPage() {
  const { sku } = useParams();
  const { dispatch } = useCart();
  return <main><h1>Product {sku}</h1><button onClick={() => dispatch({ type: 'add', sku })}>Add to cart</button></main>;
}
function App() {
  return (
    <>
      <nav><Link to="/">Home</Link> <Link to="/p/MUG">Mug</Link> <CartBadge /></nav>
      <Routes>
        <Route path="/" element={<h1>Home</h1>} />
        <Route path="/p/:sku" element={<ProductPage />} />
      </Routes>
    </>
  );
}

// --- test utilities: one place that knows which providers the app needs ---
function renderWithProviders(ui, { route = '/', cart = {} } = {}) {
  const Wrapper = ({ children }) => (
    <MemoryRouter initialEntries={[route]}>             {/* a router that keeps history in memory */}
      <CartProvider initial={cart}>{children}</CartProvider>
    </MemoryRouter>
  );
  return { user: userEvent.setup(), ...render(ui, { wrapper: Wrapper }) };
}

afterEach(cleanup);

test('reducer: plain unit test, no React needed', () => {
  assert.deepEqual(cartReducer({ MUG: 1 }, { type: 'add', sku: 'MUG' }), { MUG: 2 });
  assert.deepEqual(cartReducer({ MUG: 1 }, { type: 'clear' }), {});
  assert.throws(() => cartReducer({}, { type: 'oops' }), /unknown action oops/);
});

test('starts on a given route with a pre-filled cart', () => {
  renderWithProviders(<App />, { route: '/p/LAMP', cart: { MUG: 2 } });
  assert.equal(screen.getByRole('heading').textContent, 'Product LAMP');
  assert.equal(screen.getByLabelText('items in cart').textContent, '2');
});

test('navigates with a link, then adds to cart', async () => {
  const { user } = renderWithProviders(<App />);
  assert.equal(screen.getByRole('heading').textContent, 'Home');
  await user.click(screen.getByRole('link', { name: 'Mug' }));
  assert.equal(screen.getByRole('heading').textContent, 'Product MUG');
  await user.click(screen.getByRole('button', { name: 'Add to cart' }));
  await user.click(screen.getByRole('button', { name: 'Add to cart' }));
  assert.equal(screen.getByLabelText('items in cart').textContent, '2');
});

test('without the provider the component crashes - which is why the wrapper exists', () => {
  const quiet = console.error; console.error = () => {};
  assert.throws(() => render(<CartBadge />), /Cannot destructure property 'cart'/);
  console.error = quiet;
});
