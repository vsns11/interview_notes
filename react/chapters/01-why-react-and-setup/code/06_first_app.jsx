// The runnable twin of vite-app/src/main.jsx: same App, mounted into jsdom instead of a real browser.
import '../../../../tools_dom.mjs';
import { StrictMode, act } from 'react';
import { createRoot } from 'react-dom/client';
import App from './vite-app/src/App.jsx';

const root = createRoot(document.getElementById('root'));
await act(() => root.render(<StrictMode><App /></StrictMode>));
console.log('after first render :', document.getElementById('root').textContent);

const button = document.querySelector('button');
await act(() => button.click());
await act(() => button.click());
console.log('after two clicks   :', button.textContent);
