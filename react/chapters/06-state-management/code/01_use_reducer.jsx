// useReducer: move "how state changes" out of event handlers into one pure function.
import '../../../../tools_dom.mjs';
import { useReducer } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { tasksReducer } from './tasks-reducer.js';

// 1. Reducers are plain functions: test them without React.
const s0 = [{ id: 1, text: 'Visit Kafka Museum', done: true }];
const s1 = tasksReducer(s0, { type: 'added', id: 2, text: 'Watch a puppet show' });
const s2 = tasksReducer(s1, { type: 'toggled', id: 2 });
console.log('pure reducer:', JSON.stringify(s2.map((t) => `${t.done ? '[x]' : '[ ]'} ${t.text}`)), '| s0 untouched:', s0.length === 1);
try { tasksReducer(s0, { type: 'addded' }); } catch (e) { console.log('typo caught  :', e.message); }

// 2. In a component: dispatch describes WHAT happened; the reducer decides HOW state changes.
let nextId = 3;
function createInitialTasks(username) {                 // lazy initialiser: runs once
  console.log(`  (init for ${username})`);
  return [{ id: 1, text: `${username}'s first task`, done: false }];
}
function TaskApp({ username }) {
  const [tasks, dispatch] = useReducer(tasksReducer, username, createInitialTasks);
  return (
    <>
      <button onClick={() => dispatch({ type: 'added', id: nextId++, text: 'Buy stamps' })}>add</button>
      <button onClick={() => dispatch({ type: 'cleared_done' })}>clear done</button>
      <ul>
        {tasks.map((t) => (
          <li key={t.id}>
            <label><input type="checkbox" checked={t.done} onChange={() => dispatch({ type: 'toggled', id: t.id })} />{t.text}</label>
            <button onClick={() => dispatch({ type: 'deleted', id: t.id })}>delete {t.text}</button>
          </li>
        ))}
      </ul>
    </>
  );
}

const show = () => screen.queryAllByRole('listitem').map((li) => `${li.querySelector('input').checked ? '[x]' : '[ ]'} ${li.querySelector('label').textContent}`);
render(<TaskApp username="Ada" />);
fireEvent.click(screen.getByText('add'));
fireEvent.click(screen.getByText('add'));
console.log('after 2 adds       :', show());
fireEvent.click(screen.getByLabelText("Ada's first task"));
fireEvent.click(screen.getByText('clear done'));
console.log('toggle + clear done:', show());
fireEvent.click(screen.getAllByText('delete Buy stamps')[0]);
console.log('delete one         :', show());
