// Why a plain variable cannot drive the UI, and how useState fixes it.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

let renders = 0;
function BrokenCounter() {
  let count = 0;                          // re-created as 0 on every render
  renders++;
  return <button onClick={() => { count = count + 1; console.log('  inside handler count =', count); }}>Broken: {count}</button>;
}

function Counter() {
  const [count, setCount] = useState(0);  // React keeps this value between renders
  return <button onClick={() => setCount(count + 1)}>Working: {count}</button>;
}

render(<BrokenCounter />);
fireEvent.click(screen.getByRole('button', { name: /Broken/ }));
fireEvent.click(screen.getByRole('button', { name: /Broken/ }));
console.log('screen shows:', screen.getByRole('button', { name: /Broken/ }).textContent, '| renders:', renders);

render(<Counter />);
fireEvent.click(screen.getByRole('button', { name: /Working/ }));
fireEvent.click(screen.getByRole('button', { name: /Working/ }));
console.log('screen shows:', screen.getByRole('button', { name: /Working/ }).textContent);

// Each component instance has its OWN state.
function Pair() { return <div><Counter /><Counter /></div>; }
const { container } = render(<Pair />);
fireEvent.click(container.querySelectorAll('button')[0]);
console.log('two instances:', [...container.querySelectorAll('button')].map((b) => b.textContent).join(' | '));
