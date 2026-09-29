// 14.13 One feature (a tiny cart) built four ways: MVC, MVP, MVVM and unidirectional (reducer) data flow.
// The user-visible result must be identical; what changes is WHERE logic lives and what you can test without a UI.
import '../../../../tools_dom.mjs';
import { render, screen, cleanup, act, renderHook } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useReducer, useState, useSyncExternalStore, useCallback } from 'react';
import assert from 'node:assert/strict';

const PRICES = { Mug: 8.5, Tent: 120 };
const money = (n) => `$${n.toFixed(2)}`;

// ---------- Shared MODEL (domain data + rules, no UI) --------------------------------------
class CartModel {
  items = []; #listeners = new Set();
  add(name) { this.items = [...this.items, { name, price: PRICES[name] }]; this.#listeners.forEach(l => l()); }
  total() { return this.items.reduce((s, i) => s + i.price, 0); }
  subscribe = (l) => { this.#listeners.add(l); return () => this.#listeners.delete(l); };
}

// A dumb, props-only view reused by MVP, MVVM and the reducer version.
function CartView({ lines, totalText, onAdd }) {
  return (
    <section aria-label="cart">
      <button onClick={() => onAdd('Mug')}>Add Mug</button>
      <button onClick={() => onAdd('Tent')}>Add Tent</button>
      <ul>{lines.map((l, i) => <li key={i}>{l}</li>)}</ul>
      <p role="status">Total: {totalText}</p>
    </section>
  );
}

// ---------- 1. MVC: controller handles input and updates the model; the view observes the model -----
function makeMvc() {
  const model = new CartModel();
  const controller = { onAdd: (name) => model.add(name) };           // input -> model
  function MvcView() {                                                  // model -> view (observer)
    useSyncExternalStore(model.subscribe, () => model.items);
    return <CartView lines={model.items.map(i => `${i.name} ${money(i.price)}`)}
                     totalText={money(model.total())} onAdd={controller.onAdd} />;
  }
  return MvcView;
}

// ---------- 2. MVP: a presenter (plain JS) formats everything and pushes it into a passive view ----
class CartPresenter {
  constructor(model, view) { this.model = model; this.view = view; }  // view = anything with show(viewState)
  add(name) { this.model.add(name); this.present(); }
  present() {
    this.view.show({ lines: this.model.items.map(i => `${i.name} ${money(i.price)}`), totalText: money(this.model.total()) });
  }
}
function MvpScreen() {
  const [vs, setVs] = useState({ lines: [], totalText: money(0) });
  const [presenter] = useState(() => new CartPresenter(new CartModel(), { show: setVs }));
  return <CartView {...vs} onAdd={(n) => presenter.add(n)} />;
}

// ---------- 3. MVVM: a view model exposes bindable state + commands; the view just binds ----------
function useCartViewModel() {
  const [items, setItems] = useState([]);
  const add = useCallback((name) => setItems(xs => [...xs, { name, price: PRICES[name] }]), []);
  return {
    lines: items.map(i => `${i.name} ${money(i.price)}`),               // derived, display-ready state
    totalText: money(items.reduce((s, i) => s + i.price, 0)),
    add,                                                                // a "command"
  };
}
function MvvmScreen() { const vm = useCartViewModel(); return <CartView lines={vm.lines} totalText={vm.totalText} onAdd={vm.add} />; }

// ---------- 4. Unidirectional (Flux/Redux/Elm style): action -> pure reducer -> new state -> view --
function cartReducer(state, action) {
  switch (action.type) {
    case 'added': return { items: [...state.items, { name: action.name, price: PRICES[action.name] }] };
    default: return state;
  }
}
function ReducerScreen() {
  const [state, dispatch] = useReducer(cartReducer, { items: [] });
  return <CartView lines={state.items.map(i => `${i.name} ${money(i.price)}`)}
                   totalText={money(state.items.reduce((s, i) => s + i.price, 0))}
                   onAdd={(name) => dispatch({ type: 'added', name })} />;
}

// ---------- Same clicks, same screen ----------------------------------------------------------------
const user = userEvent.setup();
const results = {};
for (const [name, Screen] of [['MVC', makeMvc()], ['MVP', MvpScreen], ['MVVM', MvvmScreen], ['Reducer', ReducerScreen]]) {
  render(<Screen />);
  await user.click(screen.getByRole('button', { name: 'Add Mug' }));
  await user.click(screen.getByRole('button', { name: 'Add Tent' }));
  results[name] = `${screen.getAllByRole('listitem').map(li => li.textContent).join(', ')} | ${screen.getByRole('status').textContent}`;
  console.log(`${name.padEnd(8)}: ${results[name]}`);
  cleanup();
}
assert.equal(new Set(Object.values(results)).size, 1, 'all four architectures render the same UI');

// ---------- What each one lets you test WITHOUT rendering a component ---------------------------------
const shown = [];
const p = new CartPresenter(new CartModel(), { show: (vs) => shown.push(vs) });   // fake view
p.add('Tent');
console.log('MVP presenter test, fake view got   :', JSON.stringify(shown.at(-1)));
assert.equal(shown.at(-1).totalText, '$120.00');

const { result } = renderHook(() => useCartViewModel());                       // no view at all
act(() => result.current.add('Mug'));
console.log('MVVM view-model test, state         :', JSON.stringify({ lines: result.current.lines, totalText: result.current.totalText }));
assert.equal(result.current.totalText, '$8.50');

const s = [{ type: 'added', name: 'Mug' }, { type: 'added', name: 'Mug' }].reduce(cartReducer, { items: [] }); // pure function
console.log('Reducer test, replaying 2 actions   :', s.items.length, 'items');
assert.equal(s.items.length, 2);
console.log('all assertions passed');
