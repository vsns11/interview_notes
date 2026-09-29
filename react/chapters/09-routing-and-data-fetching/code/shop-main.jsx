// tools_run: skip  (browser entry for shop.html; fetch is mocked, so no server is needed)
import { createRoot } from 'react-dom/client';
import { createMemoryRouter } from 'react-router';          // a real app would use createBrowserRouter
import { RouterProvider } from 'react-router/dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { makeRoutes } from './shop.jsx';
import './shop.css';

const PRODUCTS = [
  { id: 1, name: 'Keyboard', price: 49, category: 'input' },
  { id: 2, name: 'Mouse', price: 19, category: 'input' },
  { id: 3, name: 'Monitor', price: 199, category: 'display' },
];
const REVIEWS = { 1: ['Clicky!', 'Great keys'], 2: ['Fits my hand'], 3: [] };
window.fetch = async (url) => {
  await new Promise(r => setTimeout(r, 80));
  const path = new URL(url, location.href).pathname; let m;
  if (path === '/api/products') return Response.json(PRODUCTS.map(({ id, name, price }) => ({ id, name, price })));
  if ((m = path.match(/^\/api\/products\/(\d+)\/reviews$/))) return Response.json(REVIEWS[m[1]] ?? []);
  if ((m = path.match(/^\/api\/products\/(\d+)$/))) {
    const p = PRODUCTS.find(p => p.id === +m[1]);
    return p ? Response.json(p) : new Response('{}', { status: 404 });
  }
  return new Response('{}', { status: 404 });
};

const queryClient = new QueryClient();
const router = createMemoryRouter(makeRoutes({ queryClient }), { initialEntries: ['/products/1'] });
createRoot(document.getElementById('root')).render(
  <QueryClientProvider client={queryClient}><RouterProvider router={router} /></QueryClientProvider>
);
