// tools_run: skip  (browser entry for todo.html)
import { createRoot } from 'react-dom/client';
import TodoApp from './todo.jsx';
import './todo.css';

const initialTodos = [
  { id: 1, title: 'Read chapter 3', done: true },
  { id: 2, title: 'Build the todo app', done: false },
  { id: 3, title: 'Explain batching to a friend', done: false },
];
createRoot(document.getElementById('root')).render(<TodoApp initialTodos={initialTodos} />);
