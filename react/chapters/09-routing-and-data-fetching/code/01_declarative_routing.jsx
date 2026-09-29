// Declarative routing: <Routes>/<Route>, links, URL params, search params, 404, programmatic navigation.
import '../../../../tools_dom.mjs';
import { MemoryRouter, Routes, Route, Link, NavLink, useParams, useSearchParams, useNavigate, useLocation } from 'react-router';
import { render, screen, fireEvent } from '@testing-library/react';

const PRODUCTS = [{ id: '1', name: 'Keyboard', price: 49 }, { id: '2', name: 'Mouse', price: 19 }, { id: '3', name: 'Monitor', price: 199 }];

function Nav() {
  const cls = ({ isActive }) => (isActive ? 'active' : undefined);   // NavLink knows if it matches the URL
  return (
    <nav>
      <NavLink to="/" end className={cls}>Home</NavLink> <NavLink to="/products" className={cls}>Products</NavLink>
    </nav>
  );
}
function Home() { return <h1>Welcome</h1>; }
function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();       // ?sort=price
  const sort = searchParams.get('sort') ?? 'name';
  const items = PRODUCTS.toSorted((a, b) => (sort === 'price' ? a.price - b.price : a.name.localeCompare(b.name)));
  return (
    <>
      <h1>Products (by {sort})</h1>
      <button onClick={() => setSearchParams({ sort: 'price' })}>Sort by price</button>
      <ul>{items.map(p => <li key={p.id}><Link to={`/products/${p.id}`}>{p.name}</Link></li>)}</ul>
    </>
  );
}
function ProductPage() {
  const { productId } = useParams();                                // from the :productId segment
  const navigate = useNavigate();
  const product = PRODUCTS.find(p => p.id === productId);
  if (!product) return <h1>No product {productId}</h1>;
  return (
    <>
      <h1>{product.name} - ${product.price}</h1>
      <button onClick={() => navigate('/products')}>Back to list</button>
      <button onClick={() => navigate(-1)}>Browser back</button>
    </>
  );
}
function NotFound() { return <h1>404 - nothing at {useLocation().pathname}</h1>; }

function App() {
  return (
    <>
      <Nav />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/products" element={<ProductList />} />
        <Route path="/products/:productId" element={<ProductPage />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}

let url = () => '';
function UrlSpy() { const l = useLocation(); url = () => l.pathname + l.search; return null; }
render(<MemoryRouter initialEntries={['/']}><App /><UrlSpy /></MemoryRouter>);
const show = (step) => {
  const active = document.querySelector('a.active')?.textContent ?? '-';
  console.log(`${step.padEnd(24)} url=${url().padEnd(20)} active=${active.padEnd(8)} h1="${screen.getByRole('heading').textContent}"`);
};
show('start');
fireEvent.click(screen.getByText('Products'));      show('click Products');
fireEvent.click(screen.getByText('Sort by price')); show('sort by price');
console.log('   list order:', screen.getAllByRole('listitem').map(li => li.textContent).join(', '));
fireEvent.click(screen.getByText('Monitor'));       show('click Monitor');
fireEvent.click(screen.getByText('Browser back'));  show('navigate(-1)');
fireEvent.click(screen.getByText('Mouse'));         show('click Mouse');
fireEvent.click(screen.getByText('Back to list'));  show('navigate("/products")');

render(<MemoryRouter initialEntries={['/nope']}><App /></MemoryRouter>, { container: document.body.appendChild(document.createElement('div')) });
console.log('deep link /nope ->', screen.getAllByRole('heading').at(-1).textContent);
