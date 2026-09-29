// The chapter in one app: events, useState, immutable array updates, lifted + derived state, controlled input.
import '../../../../tools_dom.mjs';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TodoApp from './todo.jsx';

const user = userEvent.setup();
render(<TodoApp initialTodos={[{ id: 1, title: 'Read chapter 3', done: true }]} />);
const show = (label) => {
  const items = screen.queryAllByRole('listitem').map((li) => `${within(li).getByRole('checkbox').checked ? '[x]' : '[ ]'} ${li.textContent.replace('✕', '').trim()}`);
  console.log(`${label.padEnd(27)} | ${(items.join(', ') || screen.getByText('Nothing here.').textContent).padEnd(64)} | ${screen.getByRole('contentinfo').firstChild.textContent}`);
};
show('start');
await user.type(screen.getByLabelText('New task'), 'Build the todo app{Enter}');
await user.type(screen.getByLabelText('New task'), 'Explain batching');
await user.click(screen.getByRole('button', { name: 'Add' }));
show('added two');
console.log('input cleared after add?   :', screen.getByLabelText('New task').value === '');
await user.click(screen.getByLabelText('Build the todo app'));
show('ticked "Build the todo app"');
await user.click(screen.getByRole('button', { name: 'active' }));
show('filter = active');
await user.click(screen.getByRole('button', { name: 'done' }));
show('filter = done');
await user.click(screen.getByRole('button', { name: 'Clear completed' }));
show('clear completed');
await user.click(screen.getByRole('button', { name: 'all' }));
await user.click(screen.getByRole('button', { name: 'Delete Explain batching' }));
show('filter = all, delete last');
