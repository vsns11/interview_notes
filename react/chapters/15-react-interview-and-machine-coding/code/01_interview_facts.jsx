// Proves the answers to the interview questions that are easiest to get wrong.
// Each block renders a tiny component in jsdom and prints what really happened.
import '../../../../tools_dom.mjs';
import { StrictMode, Component, createContext, memo, useContext, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { render, screen, fireEvent, cleanup, act } from '@testing-library/react';

const log = [];
const section = (title) => { cleanup(); log.length = 0; console.log(`\n# ${title}`); };
const flush = () => { console.log(log.map((l) => '  ' + l).join('\n')); log.length = 0; };

// ---------------------------------------------------------------- Q: state snapshot and batching
section('snapshot + batching: three setN(n + 1) vs three setN(m => m + 1)');
function Counter() {
  const [n, setN] = useState(0);
  log.push(`render n=${n}`);
  return (
    <>
      <button onClick={() => { setN(n + 1); setN(n + 1); setN(n + 1); log.push(`handler still sees n=${n}`); }}>plain</button>
      <button onClick={() => { setN((m) => m + 1); setN((m) => m + 1); setN((m) => m + 1); }}>updater</button>
    </>
  );
}
render(<Counter />);
fireEvent.click(screen.getByText('plain'));
fireEvent.click(screen.getByText('updater'));
flush();

// ---------------------------------------------------------------- Q: effect order, cleanup, layout vs passive
section('effect order: child before parent, layout before passive, cleanup sees OLD values');
function Child({ v }) {
  useLayoutEffect(() => { log.push(`child  layout setup   v=${v}`); return () => log.push(`child  layout cleanup v=${v}`); }, [v]);
  useEffect(() => { log.push(`child  effect setup   v=${v}`); return () => log.push(`child  effect cleanup v=${v}`); }, [v]);
  return null;
}
function Parent({ v }) {
  useEffect(() => { log.push(`parent effect setup   v=${v}`); return () => log.push(`parent effect cleanup v=${v}`); }, [v]);
  return <Child v={v} />;
}
const { rerender } = render(<Parent v={1} />);
log.push('--- re-render with v=2');
rerender(<Parent v={2} />);
flush();

// ---------------------------------------------------------------- Q: StrictMode double calls
section('StrictMode (development only): render twice, effects setup -> cleanup -> setup');
let renderCalls = 0;
function Tracked() {
  renderCalls++;
  useEffect(() => { log.push('effect setup'); return () => log.push('effect cleanup'); }, []);
  return <p>hi</p>;
}
render(<StrictMode><Tracked /></StrictMode>);
log.push(`component function called ${renderCalls} times for one mount`);
flush();

// ---------------------------------------------------------------- Q: key resets state
section('a different key = a new component instance = fresh state');
function Draft() { const [text] = useState(() => { log.push('Draft state initialised'); return ''; }); return <p>{text}</p>; }
const r2 = render(<Draft key="alice" />);
r2.rerender(<Draft key="alice" />);
log.push('(same key: no new initialisation)');
r2.rerender(<Draft key="bob" />);
flush();

// ---------------------------------------------------------------- Q: refs don't re-render
section('changing ref.current does not re-render; state does');
function RefVsState() {
  const clicks = useRef(0);
  const [, force] = useState(0);
  log.push(`render (ref=${clicks.current})`);
  return (
    <>
      <button onClick={() => { clicks.current++; }}>ref++</button>
      <button onClick={() => force((x) => x + 1)}>re-render</button>
    </>
  );
}
render(<RefVsState />);
fireEvent.click(screen.getByText('ref++'));
fireEvent.click(screen.getByText('ref++'));
log.push('(clicked ref++ twice: no renders logged)');
fireEvent.click(screen.getByText('re-render'));
flush();

// ---------------------------------------------------------------- Q: context and memo
section('memo does not stop context updates; a new object value re-renders every consumer');
const Theme = createContext(null);
const Label = memo(function Label() { const t = useContext(Theme); log.push(`Label rendered (${t.mode})`); return null; });
const Plain = memo(function Plain() { log.push('Plain rendered'); return null; });
function ThemeRoot() {
  const [, setTick] = useState(0);
  const value = { mode: 'dark' };                          // new object on EVERY render
  return (
    <Theme value={value}>
      <button onClick={() => setTick((t) => t + 1)}>tick</button>
      <Label /><Plain />
    </Theme>
  );
}
render(<ThemeRoot />);
log.push('--- parent re-renders, nothing really changed');
fireEvent.click(screen.getByText('tick'));
flush();

// ---------------------------------------------------------------- Q: portals and event bubbling
section('events from a portal bubble through the REACT tree, not the DOM tree');
function WithPortal() {
  return (
    <div onClick={() => log.push('React parent onClick fired')} data-testid="parent">
      {createPortal(<button>inside portal</button>, document.body)}
    </div>
  );
}
render(<WithPortal />);
const btn = screen.getByText('inside portal');
log.push(`DOM parent of the button: <${btn.parentElement.tagName.toLowerCase()}>, inside the React parent's div? ${screen.getByTestId('parent').contains(btn)}`);
fireEvent.click(btn);
flush();

// ---------------------------------------------------------------- Q: what error boundaries catch
section('error boundaries catch render errors, not event-handler errors');
class Boundary extends Component {
  state = { error: null };
  static getDerivedStateFromError(error) { return { error }; }
  render() { return this.state.error ? <p>Fallback: {this.state.error.message}</p> : this.props.children; }
}
function Bomb() { throw new Error('render failed'); }
function ClickBomb() { return <button onClick={() => { throw new Error('click failed'); }}>boom</button>; }
const quiet = console.error; console.error = () => {};          // React/jsdom report the errors too
render(<Boundary><Bomb /></Boundary>);
log.push(`render error -> ${screen.getByText(/Fallback/).textContent}`);
window.addEventListener('error', (e) => { e.preventDefault(); log.push(`click error -> reached window 'error' event: ${e.error.message}`); }, { once: true });
render(<Boundary><ClickBomb /></Boundary>);
await act(async () => { screen.getByText('boom').click(); });
log.push(`boundary fallback shown after the click? ${screen.queryAllByText(/Fallback: click/).length > 0}`);
console.error = quiet;
flush();
