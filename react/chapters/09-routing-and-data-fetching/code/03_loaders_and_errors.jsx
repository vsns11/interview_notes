// Data mode: route loaders, useLoaderData, pending UI with useNavigation, error boundaries.
import '../../../../tools_dom.mjs';
import { createMemoryRouter, Link, Outlet, useLoaderData, useNavigation, useRouteError, isRouteErrorResponse, data } from 'react-router';
import { RouterProvider } from 'react-router/dom';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { startApi } from './api-server.js';

const api = await startApi({ delayMs: 40, log: (m) => console.log(m) });
async function getJson(path, request) {
  const res = await fetch(api.base + path, { signal: request.signal });   // aborted if the user navigates away
  if (res.status === 404) throw data((await res.json()).message, { status: 404 });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}

function Root() {
  const navigation = useNavigation();               // 'idle' | 'loading' | 'submitting'
  return (
    <div>
      <nav><Link to="/">All</Link> <Link to="/products/3">Monitor</Link> <Link to="/products/9">Missing</Link></nav>
      {navigation.state === 'loading' && <p role="status">Loading…</p>}
      <Outlet />
    </div>
  );
}
function ProductList() {
  const { products, categories } = useLoaderData();
  return <h1>{products.length} products in {categories.length} categories</h1>;
}
function Product() {
  const product = useLoaderData();
  return <h1>{product.name} costs ${product.price}</h1>;
}
function ProductError() {
  const error = useRouteError();                    // whatever the loader threw
  if (isRouteErrorResponse(error)) return <h1>{error.status}: {error.data}</h1>;
  return <h1>Unexpected: {error.message}</h1>;
}

const router = createMemoryRouter([
  {
    path: '/', Component: Root, HydrateFallback: () => <p>Starting…</p>,
    children: [
      {
        index: true, Component: ProductList,
        loader: async ({ request }) => {             // two requests in PARALLEL, not a waterfall
          const [products, categories] = await Promise.all([getJson('/api/products', request), getJson('/api/categories', request)]);
          return { products, categories };
        },
      },
      {
        path: 'products/:id', Component: Product, ErrorBoundary: ProductError,
        loader: ({ params, request }) => getJson(`/api/products/${params.id}`, request),
      },
    ],
  },
], { initialEntries: ['/'] });

render(<RouterProvider router={router} />);
console.log('first paint:', document.body.textContent);
await waitFor(() => screen.getByRole('heading'));
console.log('loaded     :', screen.getByRole('heading').textContent);

fireEvent.click(screen.getByText('Monitor'));
await waitFor(() => screen.getByRole('status'));
console.log('during nav :', screen.getByRole('status').textContent, '| still showing:', screen.getByRole('heading').textContent);
await waitFor(() => screen.getByText(/costs/));
console.log('after nav  :', screen.getByRole('heading').textContent);

fireEvent.click(screen.getByText('Missing'));
await waitFor(() => screen.getByText(/404/));
console.log('missing    :', screen.getByRole('heading').textContent, '| nav still rendered:', !!screen.queryByText('All'));
api.close();
