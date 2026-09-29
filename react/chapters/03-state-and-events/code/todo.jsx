// tools_run: skip  (component module: used by 11_todo_app.jsx and the browser entry todo-main.jsx)
import { useState } from 'react';

let nextId = 100;

function AddTodo({ onAdd }) {
  const [text, setText] = useState('');                      // controlled input
  function handleSubmit(e) {
    e.preventDefault();                                      // no page reload
    const title = text.trim();
    if (!title) return;
    onAdd(title);                                            // tell the parent
    setText('');
  }
  return (
    <form className="add" onSubmit={handleSubmit}>
      <input aria-label="New task" placeholder="What needs doing?" value={text} onChange={(e) => setText(e.target.value)} />
      <button disabled={!text.trim()}>Add</button>
    </form>
  );
}

function TodoItem({ todo, onToggle, onDelete }) {
  return (
    <li className={todo.done ? 'done' : ''}>
      <label>
        <input type="checkbox" checked={todo.done} onChange={() => onToggle(todo.id)} /> {todo.title}
      </label>
      <button aria-label={`Delete ${todo.title}`} onClick={() => onDelete(todo.id)}>✕</button>
    </li>
  );
}

const FILTERS = { all: () => true, active: (t) => !t.done, done: (t) => t.done };

function FilterBar({ filter, onChange }) {
  return (
    <div className="filters" role="group" aria-label="Filter">
      {Object.keys(FILTERS).map((f) => (
        <button key={f} aria-pressed={filter === f} onClick={() => onChange(f)}>{f}</button>
      ))}
    </div>
  );
}

export default function TodoApp({ initialTodos = [] }) {
  const [todos, setTodos] = useState(initialTodos);          // lifted state: shared by all children
  const [filter, setFilter] = useState('all');

  const visible = todos.filter(FILTERS[filter]);             // derived, not stored
  const remaining = todos.filter((t) => !t.done).length;     // derived, not stored

  const add = (title) => setTodos((ts) => [...ts, { id: nextId++, title, done: false }]);
  const toggle = (id) => setTodos((ts) => ts.map((t) => (t.id === id ? { ...t, done: !t.done } : t)));
  const remove = (id) => setTodos((ts) => ts.filter((t) => t.id !== id));
  const clearDone = () => setTodos((ts) => ts.filter((t) => !t.done));

  return (
    <div className="todo">
      <h1>Today</h1>
      <AddTodo onAdd={add} />
      <FilterBar filter={filter} onChange={setFilter} />
      {visible.length === 0 ? <p className="empty">Nothing here.</p> : (
        <ul>{visible.map((t) => <TodoItem key={t.id} todo={t} onToggle={toggle} onDelete={remove} />)}</ul>
      )}
      <footer>
        <span>{remaining} {remaining === 1 ? 'task' : 'tasks'} left</span>
        {todos.length > remaining && <button onClick={clearDone}>Clear completed</button>}
      </footer>
    </div>
  );
}
