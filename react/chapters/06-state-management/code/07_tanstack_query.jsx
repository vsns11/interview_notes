// TanStack Query: server state = a cache keyed by queryKey, with loading/error states, deduping, staleness and invalidation.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { render, screen, fireEvent, act, cleanup } from '@testing-library/react';

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const settle = async (ms = 50) => { for (let t = 0; t < ms; t += 10) await act(() => wait(10)); };

// A fake server with a request log.
const db = { todos: [{ id: 1, title: 'Write chapter 6' }] };
const calls = [];
const api = {
  async getTodos() { calls.push('GET /todos'); await wait(15); return structuredClone(db.todos); },
  async getUser(id) { calls.push(`GET /users/${id}`); await wait(15); if (id === 404) throw new Error('User not found'); return { id, name: `User ${id}` }; },
  async addTodo(title) { calls.push('POST /todos'); await wait(15); const t = { id: db.todos.length + 1, title }; db.todos.push(t); return t; },
};

function useTodos() {                                     // wrap each query in a small custom Hook
  return useQuery({ queryKey: ['todos'], queryFn: api.getTodos, staleTime: 30_000 });
}
function TodoCount() { const { data } = useTodos(); return <p>count: {data?.length ?? '…'}</p>; }
function TodoList() {
  const { data, isPending, isError, error, isFetching } = useTodos();
  if (isPending) return <p>Loading…</p>;                  // no data yet
  if (isError) return <p role="alert">{error.message}</p>;
  return <ul aria-busy={isFetching}>{data.map((t) => <li key={t.id}>{t.title}</li>)}</ul>;
}
function AddTodo() {
  const queryClient = useQueryClient();
  const mutation = useMutation({
    mutationFn: api.addTodo,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] }),   // mark stale + refetch
  });
  return <button disabled={mutation.isPending} onClick={() => mutation.mutate('Review chapter 6')}>{mutation.isPending ? 'Adding…' : 'Add'}</button>;
}
function UserCard({ id }) {                                // the key includes the variable -> one cache entry per id
  const { data, status, error } = useQuery({ queryKey: ['user', id], queryFn: () => api.getUser(id) });
  return <p>user {id}: {status === 'success' ? data.name : status === 'error' ? `error (${error.message})` : 'loading'}</p>;
}
function Users() {
  const [id, setId] = useState(1);
  return <><UserCard id={id} /><button onClick={() => setId(id === 1 ? 2 : 1)}>switch user</button><button onClick={() => setId(404)}>missing</button></>;
}

// Test/script settings: no retries (default is 3) and no 5-minute garbage-collection timers keeping Node alive.
const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: Infinity }, mutations: { gcTime: Infinity } } });
const App = ({ children }) => <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;

render(<App><TodoList /><TodoCount /><AddTodo /></App>);
console.log('first render  :', screen.getByText(/Loading/).textContent, '|', screen.getByText(/count/).textContent);
await settle();
console.log('loaded        :', screen.getAllByRole('listitem').map((l) => l.textContent), '|', screen.getByText(/count/).textContent, '| requests:', calls);
cleanup();

calls.length = 0;
render(<App><TodoList /></App>);                         // mount again within staleTime
console.log('remount       : shows', screen.getAllByRole('listitem').length, 'item(s) instantly from cache | requests:', calls);
cleanup();

render(<App><TodoList /><AddTodo /></App>);
fireEvent.click(screen.getByText('Add'));
await act(() => wait(1));
console.log('mutating      : button =', screen.getByRole('button').textContent);
await settle(80);
console.log('after mutation:', screen.getAllByRole('listitem').map((l) => l.textContent), '| requests:', calls);
cleanup();

calls.length = 0;
render(<App><Users /></App>);
await settle();
fireEvent.click(screen.getByText('switch user')); await settle();
fireEvent.click(screen.getByText('switch user'));          // back to user 1: cached
console.log('user switch   :', screen.getByText(/user 1/).textContent, '(from cache at once; refetched in the background because staleTime is 0)');
await settle();
fireEvent.click(screen.getByText('missing')); await settle();
console.log('missing user  :', screen.getByText(/user 404/).textContent, '| requests:', calls);
console.log('cache keys    :', queryClient.getQueryCache().getAll().map((q) => JSON.stringify(q.queryKey)).join(' '));
cleanup();
queryClient.clear();
