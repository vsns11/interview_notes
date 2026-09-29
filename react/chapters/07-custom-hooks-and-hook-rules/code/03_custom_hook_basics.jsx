// A custom Hook shares LOGIC, not STATE: every call gets its own state.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// Custom Hook: a function whose name starts with "use" and that calls other Hooks.
function useToggle(initial = false) {
  const [on, setOn] = useState(initial);
  const toggle = () => setOn(v => !v);
  return [on, toggle];                     // return whatever shape is convenient
}

function Spoiler({ title, children }) {
  const [open, toggleOpen] = useToggle();
  return (
    <section>
      <button onClick={toggleOpen}>{open ? 'Hide' : 'Show'} {title}</button>
      {open && <p>{children}</p>}
    </section>
  );
}

function DarkSwitch() {
  const [dark, toggleDark] = useToggle(true);
  return <button onClick={toggleDark}>theme: {dark ? 'dark' : 'light'}</button>;
}

const { container } = render(
  <>
    <Spoiler title="ending">The butler did it.</Spoiler>
    <Spoiler title="twist">It was a dream.</Spoiler>
    <DarkSwitch />
  </>
);
const show = () => [...container.querySelectorAll('button, p')].map(n => n.textContent).join(' | ');
console.log('start          :', show());
fireEvent.click(screen.getByText(/Show ending/));
console.log('open "ending"  :', show());
fireEvent.click(screen.getByText(/theme/));
console.log('toggle theme   :', show());

// Not every helper is a Hook: this one calls no Hooks, so it should NOT start with "use".
function formatCount(n) { return n === 1 ? '1 item' : `${n} items`; }
console.log('plain helper   :', formatCount(3));
