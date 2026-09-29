// memo / useCallback / useMemo: before and after, measured with <Profiler onRender> and render counters.
// Scenario: a page with a search box and a list of 300 products. Typing in the box must not re-render
// the list when the list's inputs did not change.
import '../../../../tools_dom.mjs';
import { useState, useCallback, useMemo, memo, Profiler } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

const PRODUCTS = Array.from({ length: 300 }, (_, i) => ({ id: i, name: `Product ${i}`, price: (i * 7) % 100 }));
let rowRenders = 0;
const commits = [];
const onRender = (id, phase, actualDuration, baseDuration) =>
  commits.push({ id, phase, actualDuration, baseDuration });

// A deliberately slow-ish row so the difference shows up in actualDuration too.
function Row({ product, onAdd }) {
  rowRenders++;
  let x = 0; for (let i = 0; i < 2000; i++) x += i;           // pretend work
  return <li>{product.name} ${product.price} <button onClick={() => onAdd(product.id)}>add</button></li>;
}
const MemoRow = memo(Row);

function List({ items, onAdd, RowComponent, style }) {
  return <ul style={style}>{items.map(p => <RowComponent key={p.id} product={p} onAdd={onAdd} />)}</ul>;
}
const MemoList = memo(List);

// Version 1 - no memoization at all.
function PageV1() {
  const [query, setQuery] = useState('');
  const [cart, setCart] = useState([]);
  const onAdd = id => setCart(c => [...c, id]);
  return (
    <>
      <input aria-label="search" value={query} onChange={e => setQuery(e.target.value)} />
      <Profiler id="list" onRender={onRender}>
        <List items={PRODUCTS} onAdd={onAdd} RowComponent={Row} style={{ padding: 0 }} />
      </Profiler>
      <p>cart: {cart.length}</p>
    </>
  );
}

// Version 2 - memo() on List and Row, but new function + new object props every render: memo is useless.
function PageV2() {
  const [query, setQuery] = useState('');
  const [cart, setCart] = useState([]);
  const onAdd = id => setCart(c => [...c, id]);                  // new function each render
  return (
    <>
      <input aria-label="search" value={query} onChange={e => setQuery(e.target.value)} />
      <Profiler id="list" onRender={onRender}>
        <MemoList items={PRODUCTS} onAdd={onAdd} RowComponent={MemoRow} style={{ padding: 0 }} />
      </Profiler>
      <p>cart: {cart.length}</p>
    </>
  );
}

// Version 3 - memo() + stable props: useCallback for the handler, useMemo (or a constant) for the object.
function PageV3() {
  const [query, setQuery] = useState('');
  const [cart, setCart] = useState([]);
  const onAdd = useCallback(id => setCart(c => [...c, id]), []);  // same function every render
  const style = useMemo(() => ({ padding: 0 }), []);              // same object every render
  return (
    <>
      <input aria-label="search" value={query} onChange={e => setQuery(e.target.value)} />
      <Profiler id="list" onRender={onRender}>
        <MemoList items={PRODUCTS} onAdd={onAdd} RowComponent={MemoRow} style={style} />
      </Profiler>
      <p>cart: {cart.length}</p>
    </>
  );
}

function measure(label, Page) {
  render(<Page />);
  rowRenders = 0; commits.length = 0;
  const box = screen.getByLabelText('search');
  for (const text of ['c', 'ch', 'cha', 'chai', 'chair']) fireEvent.change(box, { target: { value: text } });
  const typing = { rowRenders, listCommits: commits.length, ms: commits.reduce((s, c) => s + c.actualDuration, 0) };
  rowRenders = 0;
  fireEvent.click(screen.getAllByText('add')[0]);                   // a real change: cart grows
  console.log(`${label.padEnd(34)} typing 5 chars -> Row renders: ${String(typing.rowRenders).padStart(4)}, ` +
    `list profiler commits: ${typing.listCommits} | click add -> Row renders: ${rowRenders}`);
  cleanup();
  return typing.ms;
}

const t1 = measure('V1 no memo', PageV1);
const t2 = measure('V2 memo, unstable props', PageV2);
const t3 = measure('V3 memo + useCallback/useMemo', PageV3);
console.log(`\nactualDuration while typing (ms, varies per run): V1 ${t1.toFixed(1)}, V2 ${t2.toFixed(1)}, V3 ${t3.toFixed(1)}`);
