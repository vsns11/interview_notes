// Drives the bookshop: server state from TanStack Query, cart in Zustand, currency from context, filter in useState.
import '../../../../tools_dom.mjs';
import { render, screen, fireEvent, act, cleanup } from '@testing-library/react';
import { QueryClient } from '@tanstack/react-query';
import Shop, { createCatalogApi, useCart } from './shop.jsx';

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
let requests = 0;
const api = createCatalogApi(20);
const fetchBooks = () => { requests++; return api(); };
const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: Infinity } } });

render(<Shop fetchBooks={fetchBooks} queryClient={queryClient} currency={{ code: 'EUR', locale: 'de-DE' }} />);
console.log('start      :', screen.getByText(/Loading catalog/).textContent);
await act(() => wait(40));
const titles = () => [...document.querySelectorAll('.books li span:first-child')].map((s) => s.textContent);
console.log('catalog    :', titles().length, 'books | catalog requests:', requests, '(Catalog and Cart share one query)');

fireEvent.click(screen.getByRole('button', { name: 'fiction' }));
console.log('fiction    :', titles());
fireEvent.click(screen.getByLabelText('Add Dune'));
fireEvent.click(screen.getByLabelText('Add Dune'));
fireEvent.click(screen.getByRole('button', { name: 'all' }));
fireEvent.click(screen.getByLabelText('Add The Pragmatic Programmer'));
const cart = screen.getByRole('complementary', { name: 'Cart' });
const cartText = () => [...cart.querySelectorAll('li span')].map((s) => s.textContent).join('; ') + ' | ' + cart.querySelector('.total').textContent;
console.log('cart       :', cart.querySelector('h2').textContent, '->', cartText());
console.log('store      :', JSON.stringify(useCart.getState().lines));
fireEvent.click(screen.getByLabelText('Remove Dune'));
console.log('removed    :', cartText());
act(() => useCart.getState().clear());                         // e.g. after checkout, from outside React
console.log('cleared    :', cart.querySelector('p').textContent);
cleanup();
queryClient.clear();                                           // unmount + drop the cache so the script can exit
