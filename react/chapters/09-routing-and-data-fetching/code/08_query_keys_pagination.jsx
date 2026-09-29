// Query keys with variables, pagination without flicker (keepPreviousData), dependent queries (enabled),
// and queryOptions() for reusable, typed query definitions.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery, queryOptions, keepPreviousData } from '@tanstack/react-query';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { startApi } from './api-server.js';

const api = await startApi({ delayMs: 30 });
const getJson = async (path) => (await fetch(api.base + path)).json();

// One place that defines key + fetcher; reuse it in components, loaders, prefetching, invalidation.
const productsPage = (page) => queryOptions({
  queryKey: ['products', { page }],              // variables go IN the key -> each page cached separately
  queryFn: () => getJson(`/api/products?page=${page}`),
});
const reviewsOf = (id) => queryOptions({ queryKey: ['products', id, 'reviews'], queryFn: () => getJson(`/api/products/${id}/reviews`) });

const frames = [];
function Catalog() {
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState(null);
  const list = useQuery({ ...productsPage(page), placeholderData: keepPreviousData });  // keep old page while next loads
  const reviews = useQuery({ ...reviewsOf(selected), enabled: selected !== null });     // waits until something is picked
  frames.push(`page=${page} showing=[${list.data?.items.map(p => p.name) ?? ''}] placeholder=${list.isPlaceholderData} reviews=${reviews.fetchStatus}/${reviews.status}`);
  if (list.isPending) return <p>Loading…</p>;
  return (
    <div style={{ opacity: list.isPlaceholderData ? 0.5 : 1 }}>
      <ul>{list.data.items.map(p => <li key={p.id}><button onClick={() => setSelected(p.id)}>{p.name}</button></li>)}</ul>
      <button onClick={() => setPage(p => p - 1)} disabled={page === 1}>Prev</button>
      <button onClick={() => setPage(p => p + 1)} disabled={!list.data.hasMore || list.isPlaceholderData}>Next</button>
      {reviews.data && <p>Reviews: {reviews.data.join(' / ') || 'none yet'}</p>}
    </div>
  );
}

const client = new QueryClient();
const view = render(<QueryClientProvider client={client}><Catalog /></QueryClientProvider>);
await waitFor(() => screen.getByText('Keyboard'));
const dump = (title) => { console.log(title); for (const f of [...new Set(frames)]) console.log('   ' + f); frames.length = 0; };
dump('load page 1:');
fireEvent.click(screen.getByText('Next'));
await waitFor(() => screen.getByText('Monitor'));
dump('click Next (old page stays visible, dimmed, until page 2 arrives):');
fireEvent.click(screen.getByText('Prev'));
await waitFor(() => screen.getByText('Keyboard'));
dump('click Prev (page 1 is cached -> instant):');
fireEvent.click(screen.getByText('Mouse'));
await waitFor(() => screen.getByText(/Reviews/));
dump('pick Mouse (the dependent reviews query is now enabled):');
console.log('cache keys:', client.getQueryCache().getAll().map(q => JSON.stringify(q.queryKey)).join('  '));
view.unmount(); client.clear(); api.close();
