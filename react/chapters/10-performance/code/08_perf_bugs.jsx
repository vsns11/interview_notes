// Four common React performance bugs, each measured before and after the fix.
import '../../../../tools_dom.mjs';
import { useState, useEffect, useMemo, memo, createContext, useContext } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

let mounts = 0, renders = 0;
const reset = () => { mounts = 0; renders = 0; };
function Tracked({ label }) {                 // counts renders AND mounts (fresh DOM + fresh state)
  renders++;
  useEffect(() => { mounts++; }, []);
  return <span>{label}</span>;
}
const click = (text, n = 3) => { for (let i = 0; i < n; i++) fireEvent.click(screen.getByText(text)); };
const report = (label) => console.log(`  ${label.padEnd(40)} renders=${renders}, mounts=${mounts}`);

// BUG 1: defining a component INSIDE another component. Each render creates a new function = a new
// component TYPE, so React unmounts the old subtree and mounts a new one (losing state and DOM).
console.log('1. component defined inside a component (3 parent re-renders)');
function Bad1() {
  const [n, setN] = useState(0);
  function Inner() { return <Tracked label="inner" />; }        // new type every render!
  return <><button onClick={() => setN(n + 1)}>bump</button><Inner /></>;
}
function InnerOk() { return <Tracked label="inner" />; }         // defined once, at module level
function Good1() {
  const [n, setN] = useState(0);
  return <><button onClick={() => setN(n + 1)}>bump</button><InnerOk /></>;
}
for (const [l, C] of [['bad : Inner declared in render', Bad1], ['good: Inner declared at top level', Good1]]) {
  render(<C />); reset(); click('bump'); report(l); cleanup();
}

// BUG 2: unstable keys. key={Math.random()} tells React "this is a different item" on every render.
console.log('2. unstable keys on a 3-item list (3 parent re-renders)');
const ITEMS = ['a', 'b', 'c'];
function List({ keyFn }) {
  const [n, setN] = useState(0);
  return <><button onClick={() => setN(n + 1)}>bump</button>
    <ul>{ITEMS.map(it => <li key={keyFn(it)}><Tracked label={it} /></li>)}</ul></>;
}
for (const [l, keyFn] of [['bad : key={Math.random()}', () => Math.random()], ['good: key={item.id}', it => it]]) {
  render(<List keyFn={keyFn} />); reset(); click('bump'); report(l); cleanup();
}

// BUG 3: a context value that is a NEW object every render re-renders every consumer,
// even consumers wrapped in memo(), even when user/theme did not change.
console.log('3. new context object each render (3 unrelated provider re-renders)');
const Ctx = createContext(null);
const Consumer = memo(function Consumer() { renders++; const { user } = useContext(Ctx); return <b>{user}</b>; });
function Provider({ stable }) {
  const [user] = useState('ada');
  const [, setTick] = useState(0);                                       // unrelated state
  const fresh = { user };                                                // new object each render
  const cached = useMemo(() => ({ user }), [user]);                      // same object until user changes
  return <Ctx value={stable ? cached : fresh}>
    <button onClick={() => setTick(t => t + 1)}>bump</button><Consumer /></Ctx>;
}
for (const [l, stable] of [['bad : value={{ user }}', false], ['good: value={useMemo(...)}', true]]) {
  render(<Provider stable={stable} />); reset(); click('bump'); report(l); cleanup();
}

// BUG 4: state kept too high. Typing into a box whose state lives in <App> re-renders the whole page.
console.log('4. input state kept too high (typing 5 characters)');
const Expensive = () => { renders++; return <div>big chart</div>; };
function AppHigh() {
  const [q, setQ] = useState('');
  return <><input aria-label="q" value={q} onChange={e => setQ(e.target.value)} /><Expensive /><Expensive /></>;
}
function SearchBox() {                                                    // state moved DOWN
  const [q, setQ] = useState('');
  return <input aria-label="q" value={q} onChange={e => setQ(e.target.value)} />;
}
const AppLow = () => <><SearchBox /><Expensive /><Expensive /></>;
for (const [l, C] of [['bad : query state in <App>', AppHigh], ['good: query state in <SearchBox>', AppLow]]) {
  render(<C />); reset();
  for (const v of ['r', 're', 'rea', 'reac', 'react']) fireEvent.change(screen.getByLabelText('q'), { target: { value: v } });
  report(l); cleanup();
}
