// Router loaders start the fetches; TanStack Query caches them. Revisits are instant and avoid refetching.
import '../../../../tools_dom.mjs';
import { createMemoryRouter } from 'react-router';
import { RouterProvider } from 'react-router/dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { startApi } from './api-server.js';
import { makeRoutes } from './shop.jsx';

const api = await startApi({ delayMs: 30, log: m => console.log(m) });
const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
const router = createMemoryRouter(makeRoutes({ base: api.base, queryClient }), { initialEntries: ['/products/1'] });
render(<QueryClientProvider client={queryClient}><RouterProvider router={router} /></QueryClientProvider>);

await waitFor(() => screen.getByText('Clicky!'));
console.log('deep link /products/1 ->', screen.getByRole('heading', { level: 1 }).textContent, '| reviews:', screen.getAllByRole('listitem').length);
fireEvent.click(screen.getByRole('link', { name: 'Monitor' }));
await waitFor(() => screen.getByText('No reviews yet.'));
console.log('click Monitor         ->', screen.getByRole('heading', { level: 1 }).textContent);
fireEvent.click(screen.getByRole('link', { name: 'Keyboard' }));
await waitFor(() => screen.getByText('Clicky!'));
console.log('back to Keyboard      ->', screen.getByRole('heading', { level: 1 }).textContent, '(served from the cache: no requests)');
await act(() => router.navigate('/products/7'));   // programmatic navigation from outside React
await waitFor(() => screen.getByRole('alert'));
console.log('/products/7           ->', screen.getByRole('alert').textContent);
queryClient.clear(); api.close();
