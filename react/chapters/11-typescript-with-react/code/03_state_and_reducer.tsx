// Typing useState and useReducer.
import '../../../../tools_dom.mjs';
import { useState, useReducer } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

type User = { id: number; name: string };

function Profile() {
  const [count, setCount] = useState(0);                      // inferred: number
  const [user, setUser] = useState<User | null>(null);        // explicit: starts null, later a User
  const [tags, setTags] = useState<string[]>([]);             // [] alone would be never[]
  const [status, setStatus] = useState<'idle' | 'saving' | 'saved'>('idle');  // a union of literals

  return (
    <div>
      <p>{user ? `${user.name} (#${user.id})` : 'signed out'} | {count} | {tags.join(',')} | {status}</p>
      {/* user.name without the check would not compile: 'user' is possibly 'null' */}
      <button onClick={() => { setUser({ id: 7, name: 'Ada' }); setCount(c => c + 1); setTags(t => [...t, 'admin']); setStatus('saved'); }}>
        sign in
      </button>
    </div>
  );
}

// useReducer with a DISCRIMINATED UNION of actions: each action has a literal `type`,
// and TypeScript narrows the payload inside each `case`.
type CartItem = { sku: string; qty: number };
type CartState = { items: CartItem[]; coupon: string | null };
type CartAction =
  | { type: 'add'; sku: string }
  | { type: 'remove'; sku: string }
  | { type: 'applyCoupon'; code: string }
  | { type: 'clear' };

function cartReducer(state: CartState, action: CartAction): CartState {
  switch (action.type) {
    case 'add': {
      const found = state.items.find(i => i.sku === action.sku);   // action.sku is known to exist here
      return {
        ...state,
        items: found
          ? state.items.map(i => (i.sku === action.sku ? { ...i, qty: i.qty + 1 } : i))
          : [...state.items, { sku: action.sku, qty: 1 }],
      };
    }
    case 'remove':      return { ...state, items: state.items.filter(i => i.sku !== action.sku) };
    case 'applyCoupon': return { ...state, coupon: action.code };  // action.code exists only here
    case 'clear':       return { items: [], coupon: null };
    default: {
      const unreachable: never = action;                           // compile error if a case is missing
      return unreachable;
    }
  }
}

function Cart() {
  const [cart, dispatch] = useReducer(cartReducer, { items: [], coupon: null });
  return (
    <div>
      <p>{cart.items.map(i => `${i.sku}x${i.qty}`).join(' ') || 'empty'} coupon={cart.coupon ?? 'none'}</p>
      <button onClick={() => dispatch({ type: 'add', sku: 'MUG' })}>add mug</button>
      <button onClick={() => dispatch({ type: 'applyCoupon', code: 'SAVE10' })}>coupon</button>
      <button onClick={() => dispatch({ type: 'clear' })}>clear</button>
    </div>
  );
}

render(<><Profile /><Cart /></>);
const show = (label: string) => console.log(label.padEnd(14), [...document.querySelectorAll('p')].map(p => p.textContent).join('  ||  '));
show('initial');
fireEvent.click(screen.getByText('sign in'));
fireEvent.click(screen.getByText('add mug'));
fireEvent.click(screen.getByText('add mug'));
fireEvent.click(screen.getByText('coupon'));
show('after clicks');
fireEvent.click(screen.getByText('clear'));
show('after clear');
