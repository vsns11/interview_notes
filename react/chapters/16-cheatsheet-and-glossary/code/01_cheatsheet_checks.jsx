// Chapter 16 — every concrete behaviour quoted on the cheat sheets, asserted.
// Each check() prints "OK 16.x ..." or throws (non-zero exit) if React behaves differently.
// React 19.3 + react-dom in jsdom; React Router 8 and TanStack Query 5 for §16.10.
import '../../../../tools_dom.mjs';
import assert from 'node:assert/strict';
import { format } from 'node:util';
import {
  Component, StrictMode, Suspense, act, createContext, createElement, lazy, memo, use,
  useActionState, useCallback, useContext, useDeferredValue, useEffect, useId, useLayoutEffect, useMemo,
  useOptimistic, useReducer, useRef, useState, useSyncExternalStore, startTransition,
} from 'react';
import { browser, createPortal, useFormStatus } from 'react-dom';
import { createRoot, hydrateRoot } from 'react-dom/client';
import * as server from 'react-dom/server';
import * as staticApi from 'react-dom/static';
const { renderToStaticMarkup, renderToString } = server;
import { MemoryRouter, Routes, Route, useParams } from 'react-router';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

globalThis.FormData = window.FormData;                 // Node's FormData cannot read a <form> element
const errors = [];                                     // React's development warnings, first line only
const realError = console.error;
console.error = (...args) => errors.push(format(...args).split('\n')[0].replace(/^Error: /, ''));
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const html = (el) => renderToStaticMarkup(el);

let passed = 0;
async function check(title, fn) {
  errors.length = 0;
  await fn();
  cleanup();
  passed++;
  console.log(`OK   ${title}`);
}

// A tiny error boundary that remembers the message it caught.
let caught = null;
class Catch extends Component {
  state = { error: null };
  static getDerivedStateFromError(error) { caught = error.message; return { error }; }
  render() { return this.state.error ? <p>fallback</p> : this.props.children; }
}
async function mountRoot(element, options) {
  const container = document.createElement('div');
  document.body.append(container);
  const root = createRoot(container, options);
  await act(async () => root.render(element));
  return { root, container };
}

// ---------------------------------------------------------------------------- 16.1 JSX
await check('16.1 JSX rules', () => {
  const el = <h1 className="t">Hi, {'Ada'}</h1>;
  assert.deepEqual({ type: el.type, props: el.props }, { type: 'h1', props: { className: 't', children: ['Hi, ', 'Ada'] } });
  assert.deepEqual(el.props, createElement('h1', { className: 't' }, 'Hi, ', 'Ada').props);   // JSX = createElement
  assert.equal(Object.isFrozen(el), true);                                                     // immutable (dev)
  assert.equal(html(<label htmlFor="bio" className="l">Bio</label>), '<label for="bio" class="l">Bio</label>');
  assert.equal(html(<p style={{ fontSize: 14, lineHeight: 1.5, zIndex: 2 }} />), '<p style="font-size:14px;line-height:1.5;z-index:2"></p>');
  assert.equal(html(<><b>a</b><i>b</i></>), '<b>a</b><i>b</i>');                                 // fragment adds no node
  assert.equal(html(<p>{false}{null}{undefined}{true}</p>), '<p></p>');                         // render nothing
  assert.equal(html(<p>{0 && <b>x</b>}</p>), '<p>0</p>');                                        // the 0 && trap
  assert.equal(html(<p>{[1, 2].length > 0 && 'has items'}</p>), '<p>has items</p>');
  assert.equal(html(<p>{'<script>alert(1)</script>'}</p>), '<p>&lt;script&gt;alert(1)&lt;/script&gt;</p>'); // escaped
  assert.throws(() => html(<p>{{ name: 'Ada' }}</p>), /Objects are not valid as a React child/);
});

// ---------------------------------------------------------------------------- 16.2 Components & props
await check('16.2 components and props', () => {
  let seen;
  function Spy(props) { seen = props; return null; }
  html(<Spy key="k1" name="Ada" age={36} admin />);
  assert.deepEqual(seen, { name: 'Ada', age: 36, admin: true });           // key is not a prop; bare attr = true
  function Avatar({ size = 100 }) { return <i>{String(size)}</i>; }
  assert.equal(html(<Avatar />), '<i>100</i>');                             // missing -> default
  assert.equal(html(<Avatar size={undefined} />), '<i>100</i>');            // undefined -> default
  assert.equal(html(<Avatar size={null} />), '<i>null</i>');                // null -> NOT default
  assert.equal(html(<Avatar size={0} />), '<i>0</i>');
  function Mutate(props) { props.title = 'x'; return null; }
  assert.throws(() => html(<Mutate title="a" />), /Cannot assign to read only property 'title'/); // props frozen in dev
  assert.equal(html(<div><profile /></div>), '<div><profile></profile></div>');   // lowercase = HTML tag
});

// ---------------------------------------------------------------------------- 16.3 Hooks
await check('16.3 hooks table (identity, lazy init, memo caches, useId, deferred value)', async () => {
  let inits = 0, setters = new Set(), dispatches = new Set(), refs = new Set(), cbs = new Set(), memoRuns = 0, ids = new Set();
  const init = () => { inits++; return 0; };
  function H({ a, b }) {
    const [, setN] = useState(init);                     // lazy initialiser: runs once
    const [, dispatch] = useReducer((s) => s, 0);
    const ref = useRef(null);
    const cb = useCallback(() => a, [a]);
    useMemo(() => { memoRuns++; return a * 2; }, [a]);
    ids.add(useId());
    setters.add(setN); dispatches.add(dispatch); refs.add(ref); cbs.add(cb);
    return <p>{b}</p>;
  }
  const { rerender } = render(<H a={1} b={1} />);
  rerender(<H a={1} b={2} />);
  rerender(<H a={1} b={3} />);
  assert.equal(inits, 1);
  assert.equal(setters.size, 1); assert.equal(dispatches.size, 1); assert.equal(refs.size, 1);   // stable identities
  assert.equal(cbs.size, 1); assert.equal(memoRuns, 1);                                           // deps unchanged -> cached
  assert.equal(ids.size, 1);                                                                      // useId stable per instance
  rerender(<H a={2} b={3} />);
  assert.equal(cbs.size, 2); assert.equal(memoRuns, 2);                                           // dep changed -> recomputed
  cleanup();
  // two instances get different ids
  const got = [];
  function Id() { got.push(useId()); return null; }
  render(<><Id /><Id /></>);
  assert.notEqual(got[0], got[1]);
  cleanup();
  // useDeferredValue(value, initialValue): the first render uses initialValue, then catches up
  const shown = [];
  function Preview({ text }) { shown.push(useDeferredValue(text, '')); return null; }
  await act(async () => { render(<Preview text="hello" />); });
  assert.deepEqual(shown.filter((v, i) => shown.indexOf(v) === i), ['', 'hello']);
});

// ---------------------------------------------------------------------------- 16.4 State update rules
await check('16.4 state: snapshot, updaters, batching, bail-out, immutability, position & key', async () => {
  let renders = 0, seenInHandler;
  function Counter() {
    const [n, setN] = useState(0);
    renders++;
    return (
      <>
        <output>{n}</output>
        <button onClick={() => { setN(n + 1); setN(n + 1); setN(n + 1); seenInHandler = n; }}>replace</button>
        <button onClick={() => { setN((m) => m + 1); setN((m) => m + 1); setN((m) => m + 1); }}>updater</button>
        <button onClick={() => { setN(n + 5); setN((m) => m + 1); }}>plus5-then-updater</button>
        <button onClick={() => { setN((m) => m + 1); setN(n + 5); }}>updater-then-plus5</button>
        <button onClick={() => setTimeout(() => { setN((m) => m + 1); setN((m) => m + 1); }, 0)}>timeout</button>
        <button onClick={() => setN(n)}>same</button>
        <button onClick={() => setN(0)}>reset</button>
      </>
    );
  }
  render(<Counter />);
  const out = () => Number(screen.getByRole('status').textContent);
  const click = (name) => fireEvent.click(screen.getByText(name));
  let before = renders; click('replace');
  assert.equal(out(), 1); assert.equal(seenInHandler, 0); assert.equal(renders - before, 1);   // one render per event
  before = renders; click('updater'); assert.equal(out(), 4); assert.equal(renders - before, 1);
  click('reset'); click('plus5-then-updater'); assert.equal(out(), 6);
  click('reset'); click('updater-then-plus5'); assert.equal(out(), 5);
  before = renders; click('timeout'); await act(() => sleep(10));
  assert.equal(out(), 7); assert.equal(renders - before, 1);                                     // batched in a timeout too
  before = renders; click('same'); assert.equal(renders - before <= 1, true);                   // bail-out (may call once)
  before = renders; click('same'); assert.equal(renders - before, 0);
  cleanup();

  // mutating an object and setting the same reference: no re-render
  function Person() {
    const [p, setP] = useState({ name: 'Ada' });
    return (
      <>
        <p>{p.name}</p>
        <button onClick={() => { p.name = 'MUTATED'; setP(p); }}>mutate</button>
        <button onClick={() => setP({ ...p, name: 'Grace' })}>copy</button>
      </>
    );
  }
  render(<Person />);
  fireEvent.click(screen.getByText('mutate'));
  assert.equal(screen.getByText(/Ada|MUTATED|Grace/).textContent, 'Ada');                      // stale screen
  fireEvent.click(screen.getByText('copy'));
  assert.equal(screen.getByText(/Ada|MUTATED|Grace/).textContent, 'Grace');
  cleanup();

  // state belongs to a position: same type keeps it, a different key resets it
  function Score({ who }) { const [s, setS] = useState(0); return <button onClick={() => setS(s + 1)}>{who}:{s}</button>; }
  const { rerender } = render(<div><Score who="Taylor" /></div>);
  fireEvent.click(screen.getByRole('button'));
  rerender(<div><Score who="Sarah" /></div>);
  assert.equal(screen.getByRole('button').textContent, 'Sarah:1');                              // kept
  rerender(<div><Score key="sarah" who="Sarah" /></div>);
  assert.equal(screen.getByRole('button').textContent, 'Sarah:0');                              // new key -> reset
  rerender(<section><Score key="sarah" who="Sarah" /></section>);
  assert.equal(screen.getByRole('button').textContent, 'Sarah:0');                              // different parent type -> new subtree
});

// ---------------------------------------------------------------------------- 16.5 Lifecycle order
await check('16.5 render/commit lifecycle order', () => {
  const log = [];
  function Child({ v }) {
    log.push(`render child v=${v}`);
    useLayoutEffect(() => { log.push(`child layout setup v=${v}`); return () => log.push(`child layout cleanup v=${v}`); }, [v]);
    useEffect(() => { log.push(`child effect setup v=${v}`); return () => log.push(`child effect cleanup v=${v}`); }, [v]);
    return null;
  }
  function Parent({ v }) {
    log.push(`render parent v=${v}`);
    useEffect(() => { log.push(`parent effect setup v=${v}`); return () => log.push(`parent effect cleanup v=${v}`); }, [v]);
    return <Child v={v} />;
  }
  const { rerender } = render(<Parent v={1} />);
  assert.deepEqual(log.splice(0), ['render parent v=1', 'render child v=1', 'child layout setup v=1', 'child effect setup v=1', 'parent effect setup v=1']);
  rerender(<Parent v={2} />);
  assert.deepEqual(log.splice(0), [
    'render parent v=2', 'render child v=2',
    'child layout cleanup v=1', 'child layout setup v=2',                         // layout: before paint
    'child effect cleanup v=1', 'parent effect cleanup v=1',                       // all passive cleanups first...
    'child effect setup v=2', 'parent effect setup v=2',                           // ...then setups, child before parent
  ]);
});

// ---------------------------------------------------------------------------- 16.6 Effects
await check('16.6 effects: dependency arrays, cleanup, StrictMode double run', () => {
  const log = [];
  function E({ a, b }) {
    useEffect(() => { log.push('every'); });
    useEffect(() => { log.push('mount'); return () => log.push('unmount'); }, []);
    useEffect(() => { log.push(`a=${a}`); return () => log.push(`cleanup a=${a}`); }, [a]);
    const opts = { a };                                                   // new object every render
    useEffect(() => { log.push('object dep'); }, [opts]);
    return <p>{b}</p>;
  }
  const { rerender, unmount } = render(<E a={1} b={1} />);
  assert.deepEqual(log.splice(0), ['every', 'mount', 'a=1', 'object dep']);
  rerender(<E a={1} b={2} />);
  assert.deepEqual(log.splice(0), ['every', 'object dep']);              // [a] unchanged; object dep re-runs anyway
  rerender(<E a={2} b={2} />);
  assert.deepEqual(log.splice(0), ['cleanup a=1', 'every', 'a=2', 'object dep']);   // cleanup sees the OLD value
  unmount();
  assert.deepEqual(log.splice(0), ['unmount', 'cleanup a=2']);
  // StrictMode (development only): body twice, effects setup -> cleanup -> setup, updaters and initialisers twice
  let bodies = 0, initialisers = 0, updaters = 0;
  function S() {
    bodies++;
    const [n, setN] = useState(() => { initialisers++; return 0; });
    useEffect(() => { log.push('setup'); return () => log.push('cleanup'); }, []);
    return <button onClick={() => setN((m) => { updaters++; return m + 1; })}>{n}</button>;
  }
  render(<StrictMode><S /></StrictMode>);
  assert.equal(bodies, 2); assert.equal(initialisers, 2);
  assert.deepEqual(log.splice(0), ['setup', 'cleanup', 'setup']);
  fireEvent.click(screen.getByRole('button'));
  assert.equal(updaters, 2); assert.equal(screen.getByRole('button').textContent, '1');       // result used once
});

// ---------------------------------------------------------------------------- 16.7 Refs
await check('16.7 refs: no re-render, null during first render, callback cleanup, ref as a prop', () => {
  let renders = 0, duringRender = 'unset';
  function R() {
    const count = useRef(0);
    const input = useRef(null);
    if (duringRender === 'unset') duringRender = input.current;
    renders++;
    return (<>
      <input ref={input} aria-label="name" />
      <button onClick={() => { count.current++; }}>ref++</button>
      <button onClick={() => input.current.focus()}>focus</button>
    </>);
  }
  render(<R />);
  assert.equal(duringRender, null);                                        // DOM node not attached yet
  fireEvent.click(screen.getByText('ref++')); fireEvent.click(screen.getByText('ref++'));
  assert.equal(renders, 1);                                                // mutating a ref never re-renders
  fireEvent.click(screen.getByText('focus'));
  assert.equal(document.activeElement, screen.getByLabelText('name'));
  cleanup();
  // React 19: a ref callback may return a cleanup; `ref` is an ordinary prop for function components
  const events = [];
  function Field({ label, ref }) { return <label>{label}<input ref={ref} /></label>; }
  function Host({ show }) {
    const r = useRef(null);
    return (<>
      {show && <b ref={(node) => { events.push(`attach ${node.tagName}`); return () => events.push('detach'); }}>x</b>}
      <Field label="Email" ref={r} />
      <i data-ok={String(true)} ref={() => { events.push(`field ref -> ${r.current?.tagName}`); }} />
    </>);
  }
  const { rerender } = render(<Host show />);
  rerender(<Host show={false} />);
  assert.deepEqual(events.filter((e) => !e.startsWith('field')), ['attach B', 'detach']);
  assert.ok(events.includes('field ref -> INPUT'));
});

// ---------------------------------------------------------------------------- 16.8 Forms & Actions
await check('16.8 forms: FormData rules, form actions, useActionState, useFormStatus, useOptimistic', async () => {
  const form = document.createElement('form');
  form.innerHTML = '<input name="age" value="36"><input type="checkbox" name="gift"><input type="checkbox" name="t" value="a" checked><input type="checkbox" name="t" value="b" checked><input name="noname-skipped">';
  form.querySelector('[name="noname-skipped"]').removeAttribute('name');
  const fd = new FormData(form);
  assert.equal(fd.get('age'), '36');                                       // strings, never numbers
  assert.equal(fd.has('gift'), false);                                     // unchecked box: absent
  assert.deepEqual(fd.getAll('t'), ['a', 'b']);
  assert.deepEqual(Object.fromEntries(fd), { age: '36', t: 'b' });        // last value wins

  const seen = [];
  function Signup() {
    const [state, formAction, isPending] = useActionState(async (prev, formData) => {
      seen.push({ prevCount: prev.count, email: formData.get('email') });
      await sleep(20);
      return { count: prev.count + 1, msg: `saved ${formData.get('email')}` };
    }, { count: 0, msg: '' });
    return (
      <form action={formAction}>
        <input name="email" aria-label="email" defaultValue="" />
        <SubmitButton />
        <p>{isPending ? 'pending' : state.msg}</p>
      </form>
    );
  }
  function SubmitButton() { const { pending } = useFormStatus(); return <button>{pending ? 'Saving…' : 'Save'}</button>; }
  const user = userEvent.setup();
  render(<Signup />);
  await user.type(screen.getByLabelText('email'), 'ada@example.com');
  await user.click(screen.getByRole('button'));
  assert.equal(screen.getByRole('button').textContent, 'Saving…');       // useFormStatus in a CHILD of the form
  assert.equal(screen.getByText('pending').tagName, 'P');                // isPending from useActionState
  await act(() => sleep(40));
  assert.deepEqual(seen, [{ prevCount: 0, email: 'ada@example.com' }]);  // (prevState, formData)
  assert.equal(screen.getByText(/saved/).textContent, 'saved ada@example.com');
  assert.equal(screen.getByLabelText('email').value, '');                // uncontrolled field reset after success
  cleanup();

  // useOptimistic: shows the expected value during the Action, then rolls back on its own if nothing changed
  const shown = [];
  function Likes({ fail }) {
    const [likes, setLikes] = useState(10);
    const [optimistic, addOptimistic] = useOptimistic(likes, (cur, d) => cur + d);
    shown.push(optimistic);
    return <button onClick={() => startTransition(async () => {
      addOptimistic(1);
      await sleep(20);
      if (!fail) startTransition(() => setLikes((l) => l + 1));
    })}>{optimistic}</button>;
  }
  render(<Likes fail />);
  await act(async () => { fireEvent.click(screen.getByRole('button')); });
  assert.equal(screen.getByRole('button').textContent, '11');            // instant
  await act(() => sleep(40));
  assert.equal(screen.getByRole('button').textContent, '10');            // automatic rollback
  cleanup();
  render(<Likes />);
  await act(async () => { fireEvent.click(screen.getByRole('button')); });
  await act(() => sleep(40));
  assert.equal(screen.getByRole('button').textContent, '11');            // real state took over
});

// ---------------------------------------------------------------------------- 16.9 Context
await check('16.9 context: default, nearest provider, memo does not block it, use() may be conditional', () => {
  const Theme = createContext('light');
  function Show() { return <i>{useContext(Theme)}</i>; }
  function Maybe({ read }) { if (!read) return <i>-</i>; return <i>{use(Theme)}</i>; }   // use() after an early return
  assert.equal(html(<Show />), '<i>light</i>');                                           // no provider -> default
  assert.equal(html(<Theme value="dark"><Show /><Theme value="hc"><Show /></Theme><Maybe read /></Theme>), '<i>dark</i><i>hc</i><i>dark</i>');
  assert.equal(html(<Theme value={undefined}><Show /></Theme>), '<i></i>');               // undefined value != "no provider"
  let consumerRenders = 0, plainRenders = 0;
  const Plain = memo(function Plain() { plainRenders++; return null; });
  const Ctx = createContext(null);
  const C2 = memo(function C2() { useContext(Ctx); consumerRenders++; return null; });
  function Root() {
    const [, setTick] = useState(0);
    return <Ctx value={{ mode: 'dark' }}><button onClick={() => setTick((t) => t + 1)}>tick</button><C2 /><Plain /></Ctx>;
  }
  render(<Root />);
  fireEvent.click(screen.getByText('tick'));
  assert.equal(consumerRenders, 2);                                                        // new object value -> re-render through memo
  assert.equal(plainRenders, 1);                                                           // memo child without context skipped
});

// ---------------------------------------------------------------------------- 16.10 Router & Query
await check('16.10 React Router params and ranking; TanStack Query defaults and de-duplication', async () => {
  function Product() { const { id } = useParams(); return <h1>{typeof id}:{id}</h1>; }
  const app = (url) => (
    <MemoryRouter initialEntries={[url]}>
      <Routes>
        <Route path="/products/:id" element={<Product />} />
        <Route path="/products/new" element={<h1>new form</h1>} />
        <Route path="*" element={<h1>404</h1>} />
      </Routes>
    </MemoryRouter>
  );
  render(app('/products/42')); assert.equal(screen.getByRole('heading').textContent, 'string:42'); cleanup();
  render(app('/products/new')); assert.equal(screen.getByRole('heading').textContent, 'new form'); cleanup();   // static beats :param
  render(app('/nope')); assert.equal(screen.getByRole('heading').textContent, '404'); cleanup();

  const client = new QueryClient();
  let calls = 0;
  const fetchTodos = async () => { calls++; await sleep(5); return ['a']; };
  function Count() { const { data, isPending } = useQuery({ queryKey: ['todos'], queryFn: fetchTodos }); return <p>{isPending ? 'pending' : data.length}</p>; }
  render(<QueryClientProvider client={client}><Count /><Count /></QueryClientProvider>);
  assert.equal(screen.getAllByText('pending').length, 2);
  await act(() => sleep(30));
  assert.equal(calls, 1);                                                   // two components, one request
  const q = client.getQueryCache().find({ queryKey: ['todos'] });
  assert.equal(q.isStale(), true);                                          // staleTime default 0
  assert.equal(q.gcTime, 5 * 60 * 1000);                                    // gcTime default 5 minutes
  cleanup(); client.clear();
  // retry default in the browser: 3 retries = 4 attempts
  const c2 = new QueryClient({ defaultOptions: { queries: { retryDelay: 0, gcTime: Infinity } } });
  let attempts = 0;
  function Fails() { const { status } = useQuery({ queryKey: ['x'], queryFn: async () => { attempts++; throw new Error('boom'); } }); return <p>{status}</p>; }
  render(<QueryClientProvider client={c2}><Fails /></QueryClientProvider>);
  await act(() => sleep(100));
  assert.equal(attempts, 4);
  assert.equal(screen.getByText('error').textContent, 'error');
  cleanup(); c2.clear();                                                   // (gcTime Infinity: no 5-minute timer keeps Node alive)
});

// ---------------------------------------------------------------------------- 16.11 Performance
await check('16.11 performance: memo, unstable props, children from above, useMemo, lazy', async () => {
  const count = {};
  const hit = (k) => { count[k] = (count[k] ?? 0) + 1; };
  const Row = memo(function Row() { hit('memoRow'); return null; });
  function Plain() { hit('plain'); return null; }
  function Shell({ children }) { const [, s] = useState(0); hit('shell'); return <><button onClick={() => s((x) => x + 1)}>shell</button>{children}</>; }
  function Page() {
    const [, s] = useState(0);
    const stable = useCallback(() => {}, []);
    return (<>
      <button onClick={() => s((x) => x + 1)}>page</button>
      <Plain />
      <Row onAdd={stable} />
      <Row onAdd={() => {}} style={{ padding: 0 }} />
    </>);
  }
  render(<><Page /><Shell><Plain /></Shell></>);
  for (const k in count) count[k] = 0;
  fireEvent.click(screen.getByText('page'));
  assert.deepEqual(count, { memoRow: 1, plain: 1, shell: 0 });   // stable-props memo row skipped; inline-props one did not; Plain re-rendered
  for (const k in count) count[k] = 0;
  fireEvent.click(screen.getByText('shell'));
  assert.deepEqual(count, { memoRow: 0, plain: 0, shell: 1 });   // children passed from above were not re-rendered
  cleanup();
  // lazy: the module needs a default export; the component suspends until its chunk arrives
  const Chart = lazy(() => sleep(10).then(() => ({ default: () => <p>chart</p> })));
  render(<Suspense fallback={<p>loading chart</p>}><Chart /></Suspense>);
  assert.ok(screen.getByText('loading chart'));
  assert.ok(await screen.findByText('chart'));
});

// ---------------------------------------------------------------------------- 16.13 Testing
await check('16.14 testing: get/query/find semantics, role + name, user-event vs fireEvent', async () => {
  render(<><button aria-label="Remove item">🗑</button><button disabled>Pay</button></>);
  assert.ok(screen.getByRole('button', { name: 'Remove item' }));
  assert.throws(() => screen.getByRole('button', { name: 'Buy now' }), /Unable to find an accessible element with the role "button" and name "Buy now"/);
  assert.equal(screen.queryByRole('alert'), null);
  assert.throws(() => screen.getByRole('button'), /Found multiple elements/);
  await assert.rejects(screen.findByText('never', {}, { timeout: 50 }), /Unable to find an element with the text: never/);
  cleanup();
  const typed = [];
  render(<input aria-label="box" onKeyDown={() => typed.push('keydown')} onChange={() => typed.push('change')} />);
  fireEvent.change(screen.getByLabelText('box'), { target: { value: 'hi' } });
  assert.deepEqual(typed.splice(0), ['change']);                             // fireEvent: one event
  await userEvent.setup().type(screen.getByLabelText('box'), 'hi');
  assert.deepEqual(typed.splice(0), ['keydown', 'change', 'keydown', 'change']);   // user-event: the full sequence
  cleanup();
  let clicks = 0;
  render(<button disabled onClick={() => clicks++}>Pay</button>);
  await userEvent.setup().click(screen.getByRole('button'));
  assert.equal(clicks, 0);                                                   // disabled buttons ignore real clicks
});

// ---------------------------------------------------------------------------- 16.15 Server rendering
await check('16.15 server rendering: markers, no effects on the server, suspended boundaries, hydration reuse, use(browser()), resume APIs', async () => {
  let effectRan = false;
  function Greeting({ name }) { const [likes] = useState(3); useEffect(() => { effectRan = true; }); return <p>Hello, {name}! Likes: {likes}</p>; }
  assert.equal(renderToString(<Greeting name="Ada" />), '<p>Hello, <!-- -->Ada<!-- -->! Likes: <!-- -->3</p>');
  assert.equal(renderToStaticMarkup(<Greeting name="Ada" />), '<p>Hello, Ada! Likes: 3</p>');
  assert.equal(effectRan, false);                                            // Effects never run on the server
  const never = new Promise(() => {});
  function Slow() { return <b>{use(never)}</b>; }
  assert.match(renderToString(<main><Suspense fallback={<i>loading</i>}><Slow /></Suspense></main>), /<!--\$!-->.*<i>loading<\/i><!--\/\$-->/s);
  // hydration adopts the existing DOM node and attaches handlers
  function Cart() { const [n, setN] = useState(0); return <button onClick={() => setN(n + 1)}>Cart: {n}</button>; }
  const c = document.createElement('div'); c.innerHTML = renderToString(<Cart />); document.body.append(c);
  const node = c.querySelector('button');
  await act(async () => { hydrateRoot(c, <Cart />); });
  await act(async () => node.click());
  assert.equal(c.querySelector('button'), node);
  assert.equal(node.textContent, 'Cart: 1');
  c.remove();
  // use(browser()) (19.3): suspends only on the server -> client-rendered fallback, real content in the browser
  function Welcome() { use(browser()); return <p>Welcome back!</p>; }
  const banner = <Suspense fallback={<p>Hello, guest</p>}><Welcome /></Suspense>;
  assert.match(renderToString(banner), /<!--\$!-->.*Hello, guest.*<!--\/\$-->/s);
  const { container: bc } = await mountRoot(banner);
  assert.equal(bc.textContent, 'Welcome back!');
  // the per-request and build-time APIs listed in the table exist
  for (const f of ['renderToPipeableStream', 'renderToReadableStream', 'resume', 'resumeToPipeableStream']) assert.equal(typeof server[f], 'function', f);
  for (const f of ['prerender', 'prerenderToNodeStream', 'resumeAndPrerender', 'resumeAndPrerenderToNodeStream']) assert.equal(typeof staticApi[f], 'function', f);
});

// ---------------------------------------------------------------------------- 16.16 Patterns
await check('16.16 patterns: portals bubble through React, boundaries catch render errors only, HOC in render remounts', async () => {
  let parentClicks = 0;
  render(<div data-testid="p" onClick={() => parentClicks++}>{createPortal(<button>in portal</button>, document.body)}</div>);
  const btn = screen.getByText('in portal');
  assert.equal(screen.getByTestId('p').contains(btn), false);               // DOM: outside the parent
  fireEvent.click(btn);
  assert.equal(parentClicks, 1);                                            // React: still bubbles to the parent
  cleanup();
  caught = null;
  function Bomb() { throw new Error('render failed'); }
  render(<Catch><Bomb /></Catch>);
  assert.equal(caught, 'render failed');
  assert.ok(screen.getByText('fallback'));
  cleanup();
  // a component (or HOC result) created during render is a new type each time -> state lost
  function Counter() { const [n, setN] = useState(0); return <button onClick={() => setN(n + 1)}>{n}</button>; }
  const withNothing = (C) => (props) => <C {...props} />;
  const Stable = withNothing(Counter);
  function Page({ bad }) { const [, f] = useState(0); const W = bad ? withNothing(Counter) : Stable; return <><W /><i onClick={() => f((x) => x + 1)}>rerender</i></>; }
  for (const [bad, expected] of [[false, '2'], [true, '0']]) {
    render(<Page bad={bad} />);
    fireEvent.click(screen.getByRole('button')); fireEvent.click(screen.getByRole('button'));
    fireEvent.click(screen.getByText('rerender'));
    assert.equal(screen.getByRole('button').textContent, expected);
    cleanup();
  }
});

// ---------------------------------------------------------------------------- 16.18 Common errors
const seenMessages = [];
await check('16.18 common errors: the exact messages React 19.3 produces', async () => {
  const expectThrown = async (el, re) => { caught = null; await mountRoot(<Catch>{el}</Catch>); assert.match(caught ?? '', re); seenMessages.push(caught.split('. ')[0] + '.'); };
  const expectLogged = async (fn, re) => { errors.length = 0; await fn(); const m = errors.find((e) => re.test(e)); assert.ok(m, `no warning matching ${re}: ${errors.join(' | ')}`); seenMessages.push(m.split('. ')[0] + (m.includes('. ') ? '.' : '')); };

  function Loop() { const [n, setN] = useState(0); setN(n + 1); return n; }
  await expectThrown(<Loop />, /^Too many re-renders\. React limits the number of renders to prevent an infinite loop\.$/);
  await expectThrown(<p>{{ name: 'Ada' }}</p>, /^Objects are not valid as a React child \(found: object with keys \{name\}\)/);
  let flip = false;
  function Greeting() { const [a] = useState(1); if (!flip) return <p>{a}</p>; const [b] = useState(2); return <p>{a + b}</p>; }
  { caught = null; const { root } = await mountRoot(<Catch><Greeting /></Catch>); flip = true; await act(async () => root.render(<Catch><Greeting /></Catch>)); assert.match(caught, /^Rendered more hooks than during the previous render\.$/); seenMessages.push(caught); }
  flip = true;
  { caught = null; const { root } = await mountRoot(<Catch><Greeting /></Catch>); flip = false; await act(async () => root.render(<Catch><Greeting /></Catch>)); assert.match(caught, /^Rendered fewer hooks than expected\. This may be caused by an accidental early return statement\.$/); seenMessages.push(caught); }
  assert.throws(() => useState(0), /^Error: Invalid hook call\. Hooks can only be called inside of the body of a function component\./);
  seenMessages.push('Invalid hook call. Hooks can only be called inside of the body of a function component.');
  const store = { state: { items: 3 }, subscribe: () => () => {} };
  function BadSnapshot() { return useSyncExternalStore(store.subscribe, () => ({ ...store.state })).items; }
  await expectLogged(() => mountRoot(<Catch><BadSnapshot /></Catch>), /^The result of getSnapshot should be cached to avoid an infinite loop$/);
  assert.match(caught, /^Maximum update depth exceeded\./); seenMessages.push('Maximum update depth exceeded.');
  const Lazy = lazy(() => Promise.resolve({ Named: () => null }));
  caught = null; await mountRoot(<Catch><Suspense fallback="…"><Lazy /></Suspense></Catch>); await act(() => sleep(20));
  assert.match(caught, /^Element type is invalid\. Received a promise that resolves to: undefined\. Lazy element type must resolve to a class or function\.$/);
  seenMessages.push(caught);
  await expectLogged(() => mountRoot(<ul>{[1, 2].map((i) => <li>{i}</li>)}</ul>), /^Each child in a list should have a unique "key" prop\./);
  await expectLogged(() => mountRoot(<ul>{[1, 1].map((i) => <li key={i}>{i}</li>)}</ul>), /^Encountered two children with the same key, `1`\./);
  await expectLogged(async () => { const { root } = await mountRoot(<input value={undefined} onChange={() => {}} />); await act(async () => root.render(<input value="a" onChange={() => {}} />)); }, /^A component is changing an uncontrolled input to be controlled\./);
  await expectLogged(() => mountRoot(<input value="x" />), /^You provided a `value` prop to a form field without an `onChange` handler\./);
  await expectLogged(() => mountRoot(<div class="box" />), /^Invalid DOM property `class`\. Did you mean `className`\?$/);
  await expectLogged(() => mountRoot(<profile />), /^The tag <profile> is unrecognized in this browser\./);
  await expectLogged(() => mountRoot(<p style={{ '@media (max-width: 600px)': { display: 'none' } }} />), /^Unsupported style property @media/);
  function Uncached() { return use(Promise.resolve('x')); }                  // a NEW promise on every render
  await expectLogged(async () => { await mountRoot(<Suspense fallback="…"><Uncached /></Suspense>); await act(() => sleep(20)); }, /^A component was suspended by an uncached promise\./);
  const { container: jsUrl } = await mountRoot(<a href="javascript:alert(1)">x</a>);
  assert.match(jsUrl.innerHTML, /React has blocked a javascript: URL as a security precaution\./);   // href rewritten, no warning
  seenMessages.push('React has blocked a javascript: URL as a security precaution.');
  let setX;
  function T() { const [x, s] = useState(0); setX = s; return x; }
  await expectLogged(async () => { await mountRoot(<T />); setX(1); await sleep(10); }, /^An update to T inside a test was not wrapped in act\(\.\.\.\)\.$/);
  // hydration mismatch (reported through onRecoverableError, then client-rendered)
  let inBrowser = false;
  function Banner() { return <p>{inBrowser ? 'Welcome back!' : 'Hello, guest'}</p>; }
  const c = document.createElement('div'); c.innerHTML = renderToString(<Banner />); document.body.append(c); inBrowser = true;
  const recovered = [];
  await act(async () => { hydrateRoot(c, <Banner />, { onRecoverableError: (e) => recovered.push(e.message.split('\n')[0]) }); });
  assert.match(recovered[0], /^Hydration failed because the server rendered text didn't match the client\./);
  assert.equal(c.textContent, 'Welcome back!');
  seenMessages.push(recovered[0].split('. ')[0] + '.');
});

console.error = realError;
console.log(`all ${passed} cheat-sheet checks passed (React ${(await import('react')).version})`);
console.log('\nmessages seen in 16.18:\n' + seenMessages.map((m) => '  ' + m).join('\n'));
