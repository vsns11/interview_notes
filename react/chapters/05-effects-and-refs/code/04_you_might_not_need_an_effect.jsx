// Effects that only transform data or respond to user events cost extra renders and cause bugs. Four before/after pairs.
import '../../../../tools_dom.mjs';
import { useState, useEffect, useLayoutEffect } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

let renders = 0; const painted = [];
// Records what the DOM shows after each commit (useLayoutEffect runs right after React updates the DOM).
function Probe({ id }) { useLayoutEffect(() => { painted.push(document.getElementById(id).textContent); }); return null; }
const reset = () => { renders = 0; painted.length = 0; };

// 1. Derived data: computing in an Effect = one extra render + one stale frame.
function FullNameEffect({ first, last }) {
  renders++;
  const [fullName, setFullName] = useState('');
  useEffect(() => setFullName(`${first} ${last}`), [first, last]);   // 🔴
  return <><p id="n">{fullName}</p><Probe id="n" /></>;
}
function FullNameRender({ first, last }) {
  renders++;
  const fullName = `${first} ${last}`;                                // ✅ just compute it
  return <><p id="n">{fullName}</p><Probe id="n" /></>;
}

// 2. Resetting state when a prop changes: Effect shows the OLD comment for one frame; a key doesn't.
function CommentBoxEffect({ userId }) {
  const [comment, setComment] = useState('');
  useEffect(() => setComment(''), [userId]);                          // 🔴
  return <><input aria-label="comment" value={comment} onChange={(e) => setComment(e.target.value)} /><p id="c">{comment || '(empty)'}</p><Probe id="c" /></>;
}
function CommentBox({ userId }) {
  const [comment, setComment] = useState('');
  return <><input aria-label="comment" value={comment} onChange={(e) => setComment(e.target.value)} /><p id="c">{comment || '(empty)'}</p><Probe id="c" /></>;
}
function Profile({ userId, useKey }) {
  return useKey ? <CommentBox key={userId} userId={userId} /> : <CommentBoxEffect userId={userId} />;   // ✅ key = new instance
}

// 3. Event-specific logic belongs in the event handler, not in an Effect watching state.
const toasts = [];
function ProductEffect({ initialInCart }) {
  const [inCart, setInCart] = useState(initialInCart);
  useEffect(() => { if (inCart) toasts.push('effect: "Added to cart!"'); }, [inCart]);      // 🔴 also fires on page load
  return <button onClick={() => setInCart(true)}>Buy</button>;
}
function ProductHandler({ initialInCart }) {
  const [inCart, setInCart] = useState(initialInCart);
  function handleBuy() { setInCart(true); toasts.push('handler: "Added to cart!"'); }      // ✅ only when the user buys
  return <button onClick={handleBuy}>Buy</button>;
}

// 4. Notifying the parent: from an Effect = a second render pass; from the handler = one pass.
function Toggle({ onChange, viaEffect }) {
  renders++;
  const [on, setOn] = useState(false);
  useEffect(() => { if (viaEffect) onChange(on); }, [on]);           // 🔴 (and runs on mount too)
  function handleClick() { const next = !on; setOn(next); if (!viaEffect) onChange(next); }   // ✅ same event
  return <button onClick={handleClick}>{on ? 'On' : 'Off'}</button>;
}
function Parent({ viaEffect }) {
  renders++;
  const [status, setStatus] = useState('?');
  return <><Toggle viaEffect={viaEffect} onChange={(v) => setStatus(v ? 'on' : 'off')} /><span>{status}</span></>;
}

// 5. Adjusting SOME state when a prop changes: best is to derive; else set state during render (no Effect).
function ListAdjust({ items }) {
  renders++;
  const [selection, setSelection] = useState(items[0]);
  const [prevItems, setPrevItems] = useState(items);
  if (items !== prevItems) {            // prop changed since last render -> fix up state right now
    setPrevItems(items);
    setSelection(null);                 // React re-runs this component immediately, before touching the DOM
  }
  return <p id="s">{selection ?? 'none'}</p>;
}
function ListDerived({ items }) {
  renders++;
  const [selectedId, setSelectedId] = useState('a');
  const selection = items.find((i) => i === selectedId) ?? null;      // ✅ derive: kept if still present
  return <p id="s">{selection ?? 'none'}</p>;
}

for (const [label, C] of [['Effect ', FullNameEffect], ['render ', FullNameRender]]) {
  reset(); const r = render(<C first="Ada" last="Lovelace" />);
  r.rerender(<C first="Grace" last="Hopper" />);
  console.log(`1 derived via ${label}: renders=${renders} | screen after each commit: ${JSON.stringify(painted)}`);
  cleanup();
}

for (const useKey of [false, true]) {
  const r = render(<Profile userId="ada" useKey={useKey} />);
  fireEvent.change(screen.getByLabelText('comment'), { target: { value: 'hi Ada' } });
  painted.length = 0;
  r.rerender(<Profile userId="grace" useKey={useKey} />);
  console.log(`2 reset via ${useKey ? 'key   ' : 'Effect'}: screen after switching user: ${JSON.stringify(painted)}`);
  cleanup();
}

for (const C of [ProductEffect, ProductHandler]) {
  toasts.length = 0;
  render(<C initialInCart={true} />);                                  // user reloads a page where the item is already in the cart
  console.log(`3 ${C.name.padEnd(14)}: toasts on page load = ${JSON.stringify(toasts)}`);
  cleanup();
}

for (const viaEffect of [true, false]) {
  render(<Parent viaEffect={viaEffect} />);
  reset(); fireEvent.click(screen.getByRole('button'));
  console.log(`4 notify via ${viaEffect ? 'Effect ' : 'handler'}: component renders for one click = ${renders} | parent shows ${screen.getByText(/^(on|off|\?)$/).textContent}`);
  cleanup();
}

for (const C of [ListAdjust, ListDerived]) {
  reset(); const r = render(<C items={['a', 'b']} />);
  r.rerender(<C items={['a', 'c']} />);
  console.log(`5 ${C.name.padEnd(11)}: renders=${renders} | selection after items changed = ${screen.getByRole('paragraph').textContent}`);
  cleanup();
}
