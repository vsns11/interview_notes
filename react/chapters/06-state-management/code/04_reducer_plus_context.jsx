// Reducer + context: one provider owns the state; any component can read it or dispatch — no prop drilling.
import '../../../../tools_dom.mjs';
import { createContext, useContext, useReducer, useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { tasksReducer } from './tasks-reducer.js';

// ---- TasksContext.jsx (in a real app: its own file exporting the provider + two Hooks)
const TasksContext = createContext(null);
const TasksDispatchContext = createContext(null);

export function TasksProvider({ initialTasks, children }) {
  const [tasks, dispatch] = useReducer(tasksReducer, initialTasks);
  return (
    <TasksContext value={tasks}>
      <TasksDispatchContext value={dispatch}>{children}</TasksDispatchContext>
    </TasksContext>
  );
}
export function useTasks() {
  const tasks = useContext(TasksContext);
  if (tasks === null) throw new Error('useTasks must be used inside <TasksProvider>');
  return tasks;
}
export function useTasksDispatch() {
  const dispatch = useContext(TasksDispatchContext);
  if (dispatch === null) throw new Error('useTasksDispatch must be used inside <TasksProvider>');
  return dispatch;                                      // stable identity: components using only this rarely re-render
}

// ---- Components deep in the tree: no tasks/onAdd/onToggle props passed through the middle.
let nextId = 10;
function AddTask() {
  const dispatch = useTasksDispatch();
  const [text, setText] = useState('');
  return (
    <form onSubmit={(e) => { e.preventDefault(); dispatch({ type: 'added', id: nextId++, text }); setText(''); }}>
      <input aria-label="New task" value={text} onChange={(e) => setText(e.target.value)} /><button>Add</button>
    </form>
  );
}
function TaskList() {
  const tasks = useTasks();
  const dispatch = useTasksDispatch();
  return <ul>{tasks.map((t) => (
    <li key={t.id}><label><input type="checkbox" checked={t.done} onChange={() => dispatch({ type: 'toggled', id: t.id })} />{t.text}</label></li>
  ))}</ul>;
}
function Counter() { const tasks = useTasks(); return <p>{tasks.filter((t) => !t.done).length} left</p>; }
function Layout() { return <main><AddTask /><section><TaskList /></section><footer><Counter /></footer></main>; }  // passes nothing down

render(<TasksProvider initialTasks={[{ id: 1, text: 'Philosopher’s Path', done: false }]}><Layout /></TasksProvider>);
fireEvent.change(screen.getByLabelText('New task'), { target: { value: 'Visit the temple' } });
fireEvent.click(screen.getByText('Add'));
fireEvent.click(screen.getByLabelText('Philosopher’s Path'));
console.log('tasks  :', screen.getAllByRole('listitem').map((li) => (li.querySelector('input').checked ? '[x] ' : '[ ] ') + li.textContent));
console.log('footer :', screen.getByText(/left/).textContent);

const orig = console.error; console.error = () => {};
try { render(<Counter />); } catch (e) { console.log('outside provider ->', e.message); }
console.error = orig;
