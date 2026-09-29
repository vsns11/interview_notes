// TanStack Query v5: useQuery states, request de-duplication, caching + staleTime, retries, errors.
import '../../../../tools_dom.mjs';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { render, screen, waitFor, act } from '@testing-library/react';
import { startApi } from './api-server.js';

const api = await startApi({ delayMs: 30 });
const count = (path) => api.stats.requests.filter(r => r === `GET ${path}`).length;
async function fetchJson(path) {
  const res = await fetch(api.base + path);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

// ---- 1. The states a query goes through ----
const timeline = [];
function ProductName({ id, staleTime = 0 }) {
  const { data, status, fetchStatus, isPending, isFetching, error } = useQuery({
    queryKey: ['product', id],                        // cache key: identifies this data
    queryFn: () => fetchJson(`/api/products/${id}`),  // how to get it (must return a promise)
    staleTime,
  });
  timeline.push(`status=${status.padEnd(7)} fetchStatus=${fetchStatus.padEnd(8)} isPending=${isPending} isFetching=${isFetching} data=${data?.name}`);
  if (isPending) return <p>Loading…</p>;
  if (error) return <p role="alert">Error: {error.message}</p>;
  return <p>{data.name}</p>;
}

const client = new QueryClient();
const wrap = (ui) => <QueryClientProvider client={client}>{ui}</QueryClientProvider>;
let view = render(wrap(<ProductName id={1} />));
await waitFor(() => screen.getByText('Keyboard'));
console.log('1) first load:\n   ' + [...new Set(timeline)].join('\n   '));
view.unmount();

// ---- 2. Two components, same key, same time -> ONE request ----
view = render(wrap(<><ProductName id={2} /><ProductName id={2} /></>));
await waitFor(() => expectAll('Mouse', 2));
console.log(`2) two <ProductName id={2}/> mounted together -> requests for /api/products/2: ${count('/api/products/2')}`);
view.unmount();

// ---- 3. Cache: remount shows data instantly; staleTime decides whether to refetch in background ----
timeline.length = 0;
view = render(wrap(<ProductName id={1} />));
console.log(`3) remount id=1 (staleTime 0): first render -> ${timeline[0]}`);
await waitFor(() => expect(count('/api/products/1') === 2));
console.log(`   background refetch happened: requests for /api/products/1 = ${count('/api/products/1')}`);
view.unmount();
view = render(wrap(<ProductName id={1} staleTime={60_000} />));
await act(() => new Promise(r => setTimeout(r, 80)));
console.log(`   remount with staleTime 60s -> requests still ${count('/api/products/1')} (fresh data, no refetch)`);
view.unmount();

// ---- 4. Retries: /api/flaky fails twice with 503, then succeeds ----
function Flaky() {
  const q = useQuery({ queryKey: ['flaky'], queryFn: () => fetchJson('/api/flaky'), retry: 3, retryDelay: 10 });
  return <p>{q.isSuccess ? 'flaky ok' : `attempts failed so far: ${q.failureCount}`}</p>;
}
view = render(wrap(<Flaky />));
await waitFor(() => screen.getByText('flaky ok'));
console.log(`4) retry: 3 -> succeeded after ${count('/api/flaky')} requests`);
view.unmount();

// ---- 5. Errors surface as status "error" (retries turned off to keep it quick) ----
const noRetry = new QueryClient({ defaultOptions: { queries: { retry: false } } });
view = render(<QueryClientProvider client={noRetry}><ProductName id={9} /></QueryClientProvider>);
await waitFor(() => screen.getByRole('alert'));
console.log('5) missing product ->', screen.getByRole('alert').textContent);
view.unmount();
client.clear(); noRetry.clear();                      // drop cached queries and their 5-minute gc timers
api.close();

function expect(cond) { if (!cond) throw new Error('not yet'); }
function expectAll(text, n) { expect(screen.queryAllByText(text).length === n); }
