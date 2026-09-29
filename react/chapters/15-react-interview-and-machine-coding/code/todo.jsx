// Machine coding 1 — todo list with filters, inline editing and localStorage persistence.
import { useEffect, useReducer, useRef, useState } from 'react';

export const FILTERS = {
  all: () => true,
  active: (t) => !t.done,
  completed: (t) => t.done,
};

// All todo changes live in one pure function: easy to read, easy to unit-test.
export function todosReducer(todos, action) {
  switch (action.type) {
    case 'added':
      return [...todos, { id: action.id, title: action.title, done: false }];
    case 'toggled':
      return todos.map((t) => (t.id === action.id ? { ...t, done: !t.done } : t));
    case 'edited':
      return todos.map((t) => (t.id === action.id ? { ...t, title: action.title } : t));
    case 'deleted':
      return todos.filter((t) => t.id !== action.id);
    case 'toggledAll': {
      const allDone = todos.every((t) => t.done);
      return todos.map((t) => ({ ...t, done: !allDone }));
    }
    case 'clearedCompleted':
      return todos.filter((t) => !t.done);
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}

function loadTodos(storageKey) {
  try {
    return JSON.parse(localStorage.getItem(storageKey)) ?? [];
  } catch {
    return [];                                    // corrupt JSON or storage blocked
  }
}

function TodoItem({ todo, dispatch }) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(todo.title);
  const inputRef = useRef(null);
  const cancelled = useRef(false);                // Escape must win over the blur that follows

  useEffect(() => {
    if (editing) { inputRef.current.focus(); inputRef.current.select(); }   // ready to type over
  }, [editing]);

  function startEdit() {
    cancelled.current = false;
    setDraft(todo.title);
    setEditing(true);
  }

  function finish() {
    if (cancelled.current) { cancelled.current = false; return; }
    const title = draft.trim();
    if (!title) dispatch({ type: 'deleted', id: todo.id });   // emptying a todo deletes it
    else dispatch({ type: 'edited', id: todo.id, title });
    setEditing(false);
  }

  if (editing) {
    return (
      <li className="editing">
        <input
          ref={inputRef}
          aria-label={`Edit ${todo.title}`}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onBlur={finish}
          onKeyDown={(e) => {
            if (e.key === 'Enter') finish();
            if (e.key === 'Escape') { cancelled.current = true; setEditing(false); }
          }}
        />
      </li>
    );
  }
  return (
    <li className={todo.done ? 'done' : undefined}>
      <input
        type="checkbox"
        aria-label={todo.title}
        checked={todo.done}
        onChange={() => dispatch({ type: 'toggled', id: todo.id })}
      />
      <span className="title" onDoubleClick={startEdit}>{todo.title}</span>
      <button className="icon" aria-label={`Edit ${todo.title}`} onClick={startEdit}>✎</button>
      <button className="icon" aria-label={`Delete ${todo.title}`} onClick={() => dispatch({ type: 'deleted', id: todo.id })}>✕</button>
    </li>
  );
}

export default function TodoApp({ storageKey = 'todos' }) {
  const [todos, dispatch] = useReducer(todosReducer, storageKey, loadTodos);   // lazy init: read storage once
  const [filter, setFilter] = useState('all');
  const [text, setText] = useState('');
  const nextId = useRef(Math.max(0, ...todos.map((t) => t.id)) + 1);

  useEffect(() => {
    localStorage.setItem(storageKey, JSON.stringify(todos));                   // sync to an external system
  }, [storageKey, todos]);

  const visible = todos.filter(FILTERS[filter]);          // derived — never stored
  const left = todos.filter((t) => !t.done).length;

  function handleSubmit(e) {
    e.preventDefault();
    const title = text.trim();
    if (!title) return;
    dispatch({ type: 'added', id: nextId.current++, title });
    setText('');
  }

  return (
    <section className="card todo" aria-labelledby="todo-h">
      <h2 id="todo-h">Todos</h2>
      <form onSubmit={handleSubmit} className="row">
        {todos.length > 0 && (
          <button type="button" className="icon" aria-label="Toggle all" onClick={() => dispatch({ type: 'toggledAll' })}>⌄</button>
        )}
        <input aria-label="New todo" placeholder="What needs to be done?" value={text} onChange={(e) => setText(e.target.value)} />
        <button>Add</button>
      </form>

      <div className="row" role="group" aria-label="Filter">
        {Object.keys(FILTERS).map((name) => (
          <button key={name} className="chip" aria-pressed={filter === name} onClick={() => setFilter(name)}>
            {name}
          </button>
        ))}
      </div>

      {visible.length === 0
        ? <p className="muted">{todos.length === 0 ? 'Nothing to do yet.' : `No ${filter} todos.`}</p>
        : <ul className="list">{visible.map((t) => <TodoItem key={t.id} todo={t} dispatch={dispatch} />)}</ul>}

      <footer className="row between">
        <span role="status">{left} {left === 1 ? 'item' : 'items'} left</span>
        {left < todos.length && <button onClick={() => dispatch({ type: 'clearedCompleted' })}>Clear completed</button>}
      </footer>
    </section>
  );
}
