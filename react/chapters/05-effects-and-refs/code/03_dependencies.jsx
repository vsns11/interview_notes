// Dependencies: every reactive value the Effect reads must be listed. Fix unwanted re-runs by changing the CODE, not the array.
import '../../../../tools_dom.mjs';
import { useState, useEffect, useEffectEvent } from 'react';
import { render, screen, fireEvent, act, cleanup } from '@testing-library/react';

let connects = 0;
const connect = (opts) => { connects++; return () => {}; };

// 1. An object created during render is a NEW object every render -> Effect re-runs on every keystroke.
function ObjectDep({ roomId }) {
  const [draft, setDraft] = useState('');
  const options = { serverUrl: 'https://chat.example', roomId };     // new {} each render
  useEffect(() => connect(options), [options]);
  return <input aria-label="draft" value={draft} onChange={(e) => setDraft(e.target.value)} />;
}
// Fix: create the object INSIDE the Effect and depend on the primitive.
function PrimitiveDep({ roomId }) {
  const [draft, setDraft] = useState('');
  useEffect(() => {
    const options = { serverUrl: 'https://chat.example', roomId };
    return connect(options);
  }, [roomId]);
  return <input aria-label="draft" value={draft} onChange={(e) => setDraft(e.target.value)} />;
}

// 2. Reading state only to compute the next state: use an updater, and the dependency disappears.
function Counter({ useUpdater }) {
  const [count, setCount] = useState(0);
  const [restarts, setRestarts] = useState(0);
  useEffect(() => {
    setRestarts((r) => r + 1);
    const id = setInterval(() => (useUpdater ? setCount((c) => c + 1) : setCount(count + 1)), 10);
    return () => clearInterval(id);
  }, useUpdater ? [] : [count]);                                   // eslint would demand [count] in the first case
  return <p>{useUpdater ? 'updater' : 'count dep'}: count={count} interval restarts={restarts}</p>;
}

// 3. useEffectEvent (React 19.2+): read the LATEST props/state inside an Effect without making them dependencies.
const log = [];
function ChatRoom({ roomId, theme }) {
  const onConnected = useEffectEvent(() => log.push(`connected to ${roomId}, toast in ${theme} theme`));
  useEffect(() => {
    log.push(`connect ${roomId}`);
    const t = setTimeout(() => onConnected(), 5);                // "the server said connected"
    return () => { clearTimeout(t); log.push(`disconnect ${roomId}`); };
  }, [roomId]);                                                   // theme is NOT a dependency
  return null;
}

const typeInto = (text) => { for (let i = 1; i <= text.length; i++) fireEvent.change(screen.getByLabelText('draft'), { target: { value: text.slice(0, i) } }); };

connects = 0; render(<ObjectDep roomId="music" />); typeInto('hello');
console.log('object dependency   : connects after typing 5 letters =', connects); cleanup();
connects = 0; render(<PrimitiveDep roomId="music" />); typeInto('hello');
console.log('primitive dependency: connects after typing 5 letters =', connects); cleanup();

globalThis.IS_REACT_ACT_ENVIRONMENT = false;       // let timers drive React like a real browser for this demo
for (const useUpdater of [false, true]) {
  render(<Counter useUpdater={useUpdater} />);
  await new Promise((r) => setTimeout(r, 65));
  console.log(screen.getByRole('paragraph').textContent);
  cleanup();
}
globalThis.IS_REACT_ACT_ENVIRONMENT = true;

const r = render(<ChatRoom roomId="general" theme="light" />);
await act(() => new Promise((res) => setTimeout(res, 1)));
r.rerender(<ChatRoom roomId="general" theme="dark" />);     // theme changes before "connected" arrives
await act(() => new Promise((res) => setTimeout(res, 20)));
r.rerender(<ChatRoom roomId="travel" theme="dark" />);
await act(() => new Promise((res) => setTimeout(res, 20)));
console.log('useEffectEvent:', log);
