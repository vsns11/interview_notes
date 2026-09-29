// Queries: which one to use, and what each variant (get/query/find, single/All) does.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, within, cleanup, logRoles } from '@testing-library/react';
process.env.COLORS = 'false';                               // plain (uncoloured) DOM printouts

function ProductCard({ product, inCart }) {
  return (
    <article aria-labelledby="p-title">
      <img src={product.image} alt={`Photo of ${product.name}`} />
      <h2 id="p-title">{product.name}</h2>
      <p>{product.price.toFixed(2)} EUR</p>
      <label htmlFor="qty">Quantity</label>
      <input id="qty" type="number" defaultValue={1} />
      <input placeholder="Gift message (optional)" />
      <ul aria-label="features">{product.features.map(f => <li key={f}>{f}</li>)}</ul>
      <button>Add to cart</button>
      <button title="Save for later">♡</button>
      {inCart && <p role="status">Already in your cart</p>}
      <span data-testid="sku">{product.sku}</span>
    </article>
  );
}
const lamp = { name: 'Desk lamp', price: 39.5, image: '/lamp.jpg', sku: 'LMP-01', features: ['LED', 'Dimmable', 'USB-C'] };

afterEach(cleanup);

test('priority 1: queries everyone can perceive (role, label, placeholder, text, display value)', () => {
  render(<ProductCard product={lamp} />);
  assert.equal(screen.getByRole('heading', { name: 'Desk lamp', level: 2 }).tagName, 'H2');
  assert.equal(screen.getByRole('button', { name: 'Add to cart' }).tagName, 'BUTTON');
  assert.equal(screen.getByRole('article', { name: 'Desk lamp' }).tagName, 'ARTICLE');  // name from aria-labelledby
  assert.equal(screen.getByLabelText('Quantity').value, '1');
  assert.ok(screen.getByPlaceholderText(/gift message/i));
  assert.ok(screen.getByText('39.50 EUR'));
  assert.equal(screen.getByDisplayValue('1').id, 'qty');
});

test('priority 2: semantic attributes (alt text, title)', () => {
  render(<ProductCard product={lamp} />);
  assert.equal(screen.getByAltText(/photo of desk lamp/i).getAttribute('src'), '/lamp.jpg');
  assert.ok(screen.getByTitle('Save for later'));
  // A button's accessible name comes from its CONTENT first; title is only a fallback. So a screen
  // reader announces this icon button as "♡" - the test just revealed an accessibility bug.
  assert.ok(screen.getByRole('button', { name: '♡' }));
  assert.equal(screen.queryByRole('button', { name: 'Save for later' }), null);
  // Fix in the component: <button aria-label="Save for later">♡</button>
});

test('priority 3: test IDs, only when nothing user-visible identifies the element', () => {
  render(<ProductCard product={lamp} />);
  assert.equal(screen.getByTestId('sku').textContent, 'LMP-01');
});

test('getAllBy for many matches, within() to search inside one element', () => {
  render(<ProductCard product={lamp} />);
  const list = screen.getByRole('list', { name: 'features' });
  const items = within(list).getAllByRole('listitem');
  assert.deepEqual(items.map(li => li.textContent), ['LED', 'Dimmable', 'USB-C']);
});

test('queryBy to assert that something is NOT there', () => {
  const { rerender } = render(<ProductCard product={lamp} inCart={false} />);
  assert.equal(screen.queryByRole('status'), null);         // queryBy returns null instead of throwing
  rerender(<ProductCard product={lamp} inCart />);
  assert.equal(screen.getByRole('status').textContent, 'Already in your cart');
});

test('a failing getBy explains itself', () => {
  render(<ProductCard product={lamp} />);
  assert.throws(() => screen.getByRole('button', { name: 'Buy now' }), /Unable to find an accessible element/);
  let message = '';
  try { screen.getByRole('button', { name: 'Buy now' }); } catch (e) { message = e.message; }
  console.log('    error message (first 3 lines):\n' + message.split('\n').slice(0, 3).map(l => '    | ' + l).join('\n'));
});

test('logRoles prints every role and accessible name - handy when writing queries', () => {
  const { container } = render(<ul aria-label="tiny"><li>one</li></ul>);
  const lines = [];
  const orig = console.log; console.log = s => lines.push(s);
  logRoles(container);
  console.log = orig;
  console.log('    logRoles output:\n' + lines.join('\n').split('\n').filter(Boolean).map(l => '    | ' + l).join('\n'));
});
