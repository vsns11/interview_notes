// TanStack Query v5: useMutation, invalidation, and optimistic updates with rollback.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { startApi } from './api-server.js';

const api = await startApi({ delayMs: 30, log: (m) => console.log(m) });
const getTodos = async () => (await fetch(`${api.base}/api/todos`)).json();
async function addTodo(title) {
  const res = await fetch(`${api.base}/api/todos`, { method: 'POST', body: JSON.stringify({ title }) });
  if (!res.ok) throw new Error((await res.json()).message);
  return res.json();
}

function Todos({ optimistic }) {
  const queryClient = useQueryClient();
  const [title, setTitle] = useState('');
  const todos = useQuery({ queryKey: ['todos'], queryFn: getTodos });

  const mutation = useMutation({
    mutationFn: addTodo,
    // Optimistic path: show the new item immediately, remember the old list for rollback.
    onMutate: optimistic ? async (newTitle) => {
      await queryClient.cancelQueries({ queryKey: ['todos'] });          // don't let a refetch overwrite us
      const previous = queryClient.getQueryData(['todos']);
      queryClient.setQueryData(['todos'], old => [...old, { id: 'temp', title: newTitle + ' (saving)' }]);
      return { previous };                                               // becomes the 3rd argument of onError
    } : undefined,
    onError: (err, newTitle, ctx) => { if (ctx?.previous) queryClient.setQueryData(['todos'], ctx.previous); },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['todos'] }),  // refetch the truth either way
  });

  if (todos.isPending) return <p>Loading…</p>;
  return (
    <form onSubmit={e => { e.preventDefault(); mutation.mutate(title); setTitle(''); }}>
      <ul>{todos.data.map(t => <li key={t.id}>{t.title}</li>)}</ul>
      <input aria-label="title" value={title} onChange={e => setTitle(e.target.value)} />
      <button disabled={mutation.isPending}>{mutation.isPending ? 'Adding…' : 'Add'}</button>
      {mutation.isError && <p role="alert">Could not add: {mutation.error.message}</p>}
    </form>
  );
}

const items = () => screen.getAllByRole('listitem').map(li => li.textContent).join(' | ');
async function add(title) {
  fireEvent.change(screen.getByLabelText('title'), { target: { value: title } });
  fireEvent.click(screen.getByRole('button'));
}

const client = new QueryClient({ defaultOptions: {
  queries: { retry: false },
  mutations: { gcTime: 0 },        // script-only: finished mutations are otherwise kept 5 min, keeping Node alive
} });
let view = render(<QueryClientProvider client={client}><Todos /></QueryClientProvider>);
await waitFor(() => screen.getByRole('list'));
console.log('A) plain mutation + invalidate');
await add('Learn mutations');
console.log('   right after submit :', items(), '| button:', screen.getByRole('button').textContent);
await waitFor(() => screen.getByText('Learn mutations'));
console.log('   after refetch      :', items());
view.unmount();

console.log('B) optimistic update');
view = render(<QueryClientProvider client={client}><Todos optimistic /></QueryClientProvider>);
await waitFor(() => screen.getByRole('list'));
await add('Ship it');
await waitFor(() => screen.getByText(/saving/));
console.log('   right after submit :', items());
await waitFor(() => screen.getByText('Ship it'));
console.log('   after server + refetch:', items());

console.log('C) optimistic update that fails');
await add('This will fail');
await waitFor(() => screen.getByText(/saving/));
console.log('   right after submit :', items());
await waitFor(() => screen.getByRole('alert'));
console.log('   after the 500      :', items(), '|', screen.getByRole('alert').textContent);
view.unmount(); client.clear(); api.close();
