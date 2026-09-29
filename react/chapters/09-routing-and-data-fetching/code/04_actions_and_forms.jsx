// Data mode: <Form> + route actions, validation errors, redirect, automatic revalidation, useFetcher.
import '../../../../tools_dom.mjs';
import { createMemoryRouter, Form, Outlet, Link, useLoaderData, useActionData, useNavigation, useFetcher, redirect, data } from 'react-router';
import { RouterProvider } from 'react-router/dom';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { startApi } from './api-server.js';

globalThis.FormData = window.FormData;               // jsdom forms need jsdom's FormData (a browser has one global)
const api = await startApi({ delayMs: 20, log: (m) => console.log(m) });
const postTodo = (title) => fetch(`${api.base}/api/todos`, { method: 'POST', body: JSON.stringify({ title }) });

async function todosLoader() { return (await fetch(`${api.base}/api/todos`)).json(); }

async function newTodoAction({ request }) {
  const form = await request.formData();              // the submitted <Form> fields
  const title = String(form.get('title') ?? '').trim();
  if (title.length < 3) return data({ errors: { title: 'At least 3 characters' } }, { status: 400 });
  await postTodo(title);
  return redirect('/todos');                           // Post/Redirect/Get
}

function TodoList() {
  const todos = useLoaderData();
  const fetcher = useFetcher();                        // submit WITHOUT navigating
  return (
    <>
      <h1>{todos.length} todos: {todos.map(t => t.title).join(', ')}</h1>
      <fetcher.Form method="post" action="/todos/new">
        <input name="title" aria-label="quick add" defaultValue="Buy milk" />
        <button>{fetcher.state === 'idle' ? 'Quick add' : 'Adding…'}</button>
      </fetcher.Form>
      <Link to="new">New todo page</Link>
    </>
  );
}
function NewTodo() {
  const actionData = useActionData();                  // what the action returned (validation errors)
  const navigation = useNavigation();
  return (
    <Form method="post">
      <h1>New todo</h1>
      <input name="title" aria-label="title" />
      {actionData?.errors?.title && <p role="alert">{actionData.errors.title}</p>}
      <button disabled={navigation.state === 'submitting'}>{navigation.state === 'submitting' ? 'Saving…' : 'Save'}</button>
    </Form>
  );
}

const router = createMemoryRouter([
  { path: '/todos', Component: Outlet, HydrateFallback: () => null, children: [
    { index: true, Component: TodoList, loader: todosLoader },
    { path: 'new', Component: NewTodo, action: newTodoAction },
  ] },
], { initialEntries: ['/todos'] });

render(<RouterProvider router={router} />);
await waitFor(() => screen.getByText(/todos:/));
console.log('list      :', screen.getByRole('heading').textContent);

fireEvent.click(screen.getByText('New todo page'));
await waitFor(() => screen.getByText('New todo'));
fireEvent.change(screen.getByLabelText('title'), { target: { value: 'ok' } });
fireEvent.click(screen.getByText('Save'));
await waitFor(() => screen.getByRole('alert'));
console.log('too short :', screen.getByRole('alert').textContent, '| url still', router.state.location.pathname);

fireEvent.change(screen.getByLabelText('title'), { target: { value: 'Write the router demo' } });
fireEvent.click(screen.getByText('Save'));
await waitFor(() => screen.getByText('Saving…'));
console.log('submitting: button says "Saving…"');
await waitFor(() => screen.getByText(/2 todos/));
console.log('redirected:', router.state.location.pathname, '|', screen.getByRole('heading').textContent);

fireEvent.click(screen.getByText('Quick add'));
await waitFor(() => screen.getByText(/3 todos/));
console.log('fetcher   :', router.state.location.pathname, '|', screen.getByRole('heading').textContent);
api.close();
