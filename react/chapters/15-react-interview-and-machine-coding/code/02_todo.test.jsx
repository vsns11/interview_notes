// Tests for todo.jsx — written the way a user uses the app (roles and labels, real typing).
import '../../../../tools_dom.mjs';
import { test, afterEach, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TodoApp, { todosReducer } from './todo.jsx';

afterEach(cleanup);
beforeEach(() => localStorage.clear());

const titles = () => screen.queryAllByRole('listitem').map((li) => li.textContent.replace(/[✎✕]/g, '').trim());
const status = () => screen.getByRole('status').textContent;

test('reducer: toggledAll ticks everything, or unticks if all are done', () => {
  const two = [{ id: 1, title: 'a', done: true }, { id: 2, title: 'b', done: false }];
  const allDone = todosReducer(two, { type: 'toggledAll' });
  assert.deepEqual(allDone.map((t) => t.done), [true, true]);
  assert.deepEqual(todosReducer(allDone, { type: 'toggledAll' }).map((t) => t.done), [false, false]);
});

test('adds with Enter, ignores blank input, clears the box', async () => {
  const user = userEvent.setup();
  render(<TodoApp />);
  const box = screen.getByRole('textbox', { name: 'New todo' });
  await user.type(box, '   {Enter}');                       // whitespace only: ignored
  assert.equal(screen.getByText('Nothing to do yet.').tagName, 'P');
  await user.type(box, 'Buy milk{Enter}');
  await user.type(box, 'Write tests{Enter}');
  assert.deepEqual(titles(), ['Buy milk', 'Write tests']);
  assert.equal(box.value, '');
  assert.equal(status(), '2 items left');
});

test('toggle + filters + clear completed', async () => {
  const user = userEvent.setup();
  render(<TodoApp />);
  const box = screen.getByRole('textbox', { name: 'New todo' });
  for (const t of ['A', 'B', 'C']) await user.type(box, `${t}{Enter}`);
  await user.click(screen.getByRole('checkbox', { name: 'B' }));
  assert.equal(status(), '2 items left');

  await user.click(screen.getByRole('button', { name: 'active' }));
  assert.deepEqual(titles(), ['A', 'C']);
  assert.equal(screen.getByRole('button', { name: 'active' }).getAttribute('aria-pressed'), 'true');
  await user.click(screen.getByRole('button', { name: 'completed' }));
  assert.deepEqual(titles(), ['B']);

  await user.click(screen.getByRole('button', { name: 'Clear completed' }));
  assert.equal(screen.getByText('No completed todos.').tagName, 'P');
  assert.equal(screen.queryByRole('button', { name: 'Clear completed' }), null);
});

test('edit: Enter saves, Escape cancels, empty text deletes', async () => {
  const user = userEvent.setup();
  render(<TodoApp />);
  await user.type(screen.getByRole('textbox', { name: 'New todo' }), 'Wash car{Enter}');

  await user.dblClick(screen.getByText('Wash car'));
  const edit = screen.getByRole('textbox', { name: 'Edit Wash car' });
  assert.equal(document.activeElement, edit);                // focused automatically
  await user.clear(edit);
  await user.type(edit, 'Wash bike{Enter}');
  assert.deepEqual(titles(), ['Wash bike']);

  await user.click(screen.getByRole('button', { name: 'Edit Wash bike' }));
  await user.type(screen.getByRole('textbox', { name: 'Edit Wash bike' }), ' NOPE{Escape}');
  assert.deepEqual(titles(), ['Wash bike']);                // unchanged

  await user.click(screen.getByRole('button', { name: 'Edit Wash bike' }));
  await user.clear(screen.getByRole('textbox', { name: 'Edit Wash bike' }));
  await user.keyboard('{Enter}');
  assert.deepEqual(titles(), []);
});

test('persists to localStorage and restores on the next mount', async () => {
  const user = userEvent.setup();
  const first = render(<TodoApp storageKey="t1" />);
  await user.type(screen.getByRole('textbox', { name: 'New todo' }), 'Survive reload{Enter}');
  await user.click(screen.getByRole('checkbox', { name: 'Survive reload' }));
  first.unmount();

  render(<TodoApp storageKey="t1" />);                      // a "reload"
  const item = screen.getByRole('listitem');
  assert.equal(within(item).getByRole('checkbox').checked, true);
  console.log('    stored JSON:', localStorage.getItem('t1'));
});
