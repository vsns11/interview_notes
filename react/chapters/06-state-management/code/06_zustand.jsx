// Zustand: a tiny store created with a function; components subscribe with selectors. No provider needed.
import '../../../../tools_dom.mjs';
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { useShallow } from 'zustand/react/shallow';
import { render, screen, fireEvent, act } from '@testing-library/react';

// ---- stores/useBearStore.js : state and the actions that change it live together
const useBearStore = create((set, get) => ({
  bears: 0,
  fish: 10,
  addBear: () => set((state) => ({ bears: state.bears + 1 })),     // set merges (shallowly) into the state
  eatFish: () => set((state) => ({ fish: Math.max(0, state.fish - get().bears) })),
  reset: () => set({ bears: 0, fish: 10 }),
}));

const renders = {};
const tick = (n) => { renders[n] = (renders[n] ?? 0) + 1; };

function BearCounter() { tick('BearCounter'); const bears = useBearStore((s) => s.bears); return <p>{bears} bears</p>; }
function FishCounter() { tick('FishCounter'); const fish = useBearStore((s) => s.fish); return <p>{fish} fish</p>; }
function Controls() {
  tick('Controls');
  // several values at once: useShallow compares the picked object's fields, not its identity
  const { addBear, eatFish } = useBearStore(useShallow((s) => ({ addBear: s.addBear, eatFish: s.eatFish })));
  return <><button onClick={addBear}>add bear</button><button onClick={eatFish}>eat</button></>;
}
function Everything() { tick('Everything(no selector)'); const s = useBearStore(); return <small>{s.bears}/{s.fish}</small>; }

render(<><BearCounter /><FishCounter /><Controls /><Everything /></>);
for (const k in renders) delete renders[k];
fireEvent.click(screen.getByText('add bear'));
fireEvent.click(screen.getByText('add bear'));
console.log('after 2 bears:', screen.getByText(/bears/).textContent, '|', screen.getByText(/fish/).textContent, '| renders:', JSON.stringify(renders));
for (const k in renders) delete renders[k];
fireEvent.click(screen.getByText('eat'));
console.log('after eating :', screen.getByText(/fish/).textContent, '| renders:', JSON.stringify(renders));

// Outside React: read, write, subscribe (tests, WebSocket handlers, other modules).
const unsub = useBearStore.subscribe((state, prev) => console.log(`  subscriber: bears ${prev.bears} -> ${state.bears}`));
act(() => useBearStore.getState().addBear());
act(() => useBearStore.setState({ fish: 99 }));
unsub();
console.log('getState()   :', JSON.stringify({ bears: useBearStore.getState().bears, fish: useBearStore.getState().fish }), '| screen:', screen.getByText(/fish/).textContent);

// ---- Middleware: persist to localStorage (jsdom provides one here).
const useSettings = create(persist(
  (set) => ({ theme: 'light', toggle: () => set((s) => ({ theme: s.theme === 'light' ? 'dark' : 'light' })) }),
  { name: 'settings', storage: createJSONStorage(() => localStorage) },
));
act(() => useSettings.getState().toggle());
console.log('persisted    :', localStorage.getItem('settings'));

// ---- Common mistake: a selector that builds a NEW object each call, without useShallow.
function Broken() { const { bears, fish } = useBearStore((s) => ({ bears: s.bears, fish: s.fish })); return <p>{bears}/{fish}</p>; }
const origError = console.error; console.error = () => {};
try { render(<Broken />); } catch (e) { console.log('new-object selector ->', e.message.slice(0, 40) + '…  (fix: useShallow)'); }
console.error = origError;
