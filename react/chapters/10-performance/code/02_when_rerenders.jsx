// When does a component re-render? Render counters make it visible.
import '../../../../tools_dom.mjs';
import { useState, createContext, useContext } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

const renders = {};
const count = name => { renders[name] = (renders[name] ?? 0) + 1; };
const snapshot = () => { const s = { ...renders }; for (const k in renders) delete renders[k]; return s; };

// --- Case A: a parent's state change re-renders ALL of its children (props or not) ---
function Child({ name }) { count(name); return <li>{name}</li>; }
function Parent() {
  count('Parent');
  const [n, setN] = useState(0);
  return (
    <div>
      <button onClick={() => setN(n + 1)}>parent clicks {n}</button>
      <ul><Child name="ChildWithProps" /><Static /></ul>
    </div>
  );
}
function Static() { count('StaticNoProps'); return <li>static</li>; }

// --- Case B: the same children passed in as JSX from ABOVE do NOT re-render ---
function Wrapper({ children }) {
  count('Wrapper');
  const [n, setN] = useState(0);
  return <div><button onClick={() => setN(n + 1)}>wrapper clicks {n}</button>{children}</div>;
}

// --- Case C: a context change re-renders every consumer, wherever it is ---
const ThemeCtx = createContext('light');
function Consumer() { count('ContextConsumer'); return <i>{useContext(ThemeCtx)}</i>; }
function NotConsumer() { count('NotAConsumer'); return <i>plain</i>; }
function ThemeProvider({ children }) {         // state lives here; children come from above
  const [theme, setTheme] = useState('light');
  return (
    <ThemeCtx value={theme}>
      <button onClick={() => setTheme(t => (t === 'light' ? 'dark' : 'light'))}>toggle theme</button>
      {children}
    </ThemeCtx>
  );
}
function Middle() { count('Middle'); return <><Consumer /><NotConsumer /></>; }
const ThemeApp = () => <ThemeProvider><Middle /></ThemeProvider>;

render(<><Parent /><Wrapper><Child name="ChildAsChildren" /></Wrapper><ThemeApp /></>);
console.log('mount            ', JSON.stringify(snapshot()));

fireEvent.click(screen.getByText(/parent clicks/));
console.log('Parent setState  ', JSON.stringify(snapshot()));

fireEvent.click(screen.getByText(/wrapper clicks/));
console.log('Wrapper setState ', JSON.stringify(snapshot()));

fireEvent.click(screen.getByText('toggle theme'));
console.log('theme change     ', JSON.stringify(snapshot()));
