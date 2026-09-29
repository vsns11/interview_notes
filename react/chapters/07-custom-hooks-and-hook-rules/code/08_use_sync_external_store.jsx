// useSyncExternalStore: read data that lives OUTSIDE React (browser APIs, your own stores).
import '../../../../tools_dom.mjs';
import { useSyncExternalStore } from 'react';
import { render, screen, act } from '@testing-library/react';

// ---- 1. A browser API: navigator.onLine + 'online'/'offline' events ----
function subscribeOnline(callback) {          // React passes a callback; call it on every change
  window.addEventListener('online', callback);
  window.addEventListener('offline', callback);
  return () => {                               // and return an unsubscribe function
    window.removeEventListener('online', callback);
    window.removeEventListener('offline', callback);
  };
}
function useOnlineStatus() {
  return useSyncExternalStore(
    subscribeOnline,                           // defined OUTSIDE the component: same function every render
    () => navigator.onLine,                    // getSnapshot (client)
    () => true                                 // getServerSnapshot (used during SSR and hydration)
  );
}
function StatusBar() { return <p>{useOnlineStatus() ? 'Online' : 'Offline - changes will sync later'}</p>; }

let online = true;                             // jsdom has no network switch, so fake one
Object.defineProperty(navigator, 'onLine', { get: () => online, configurable: true });
render(<StatusBar />);
console.log('start        :', screen.getByText(/line/).textContent);
act(() => { online = false; window.dispatchEvent(new window.Event('offline')); });
console.log('wifi drops   :', screen.getByText(/line/).textContent);
act(() => { online = true; window.dispatchEvent(new window.Event('online')); });
console.log('wifi returns :', screen.getByText(/line/).textContent);

// ---- 2. Your own tiny store (the pattern Redux/Zustand use internally) ----
function createStore(initial) {
  let state = initial;
  const listeners = new Set();
  return {
    getState: () => state,
    setState: (update) => { state = { ...state, ...update }; listeners.forEach(l => l()); },
    subscribe: (l) => { listeners.add(l); return () => listeners.delete(l); },
  };
}
const cart = createStore({ items: 0 });
function useCartCount() { return useSyncExternalStore(cart.subscribe, () => cart.getState().items); }
function Badge() { return <span>badge: {useCartCount()}</span>; }
function Header() { return <h1>header says {useCartCount()} items</h1>; }
const r = render(<><Header /><Badge /></>);
act(() => cart.setState({ items: 3 }));        // updated from plain JS, outside any component
console.log('store update :', r.container.querySelector('h1').textContent, '|', r.container.querySelector('span').textContent);

// ---- 3. The classic bug: getSnapshot returns a NEW object every time ----
const origError = console.error;
console.error = (m) => { if (String(m).includes('getSnapshot')) console.log('dev warning  :', String(m).split('\n')[0]); };
function BadCart() {
  const snap = useSyncExternalStore(cart.subscribe, () => ({ ...cart.getState() })); // new object each call
  return <p>{snap.items}</p>;
}
try { render(<BadCart />); } catch (e) { console.log('bad snapshot :', e.message.split('\n')[0]); }
console.error = origError;
