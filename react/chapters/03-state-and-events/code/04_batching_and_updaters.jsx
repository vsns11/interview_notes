// React batches state updates made in one event; updater functions queue on the latest value.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

let renders = 0;
function Counter() {
  const [number, setNumber] = useState(0);
  const [clicks, setClicks] = useState(0);
  renders++;
  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => { setNumber(number + 1); setNumber(number + 1); setNumber(number + 1); setClicks(clicks + 1); }}>replace x3</button>
      <button onClick={() => { setNumber((n) => n + 1); setNumber((n) => n + 1); setNumber((n) => n + 1); }}>updater x3</button>
      <button onClick={() => { setNumber(number + 5); setNumber((n) => n + 1); }}>+5 then n=&gt;n+1</button>
      <button onClick={() => { setNumber(number + 5); setNumber((n) => n + 1); setNumber(42); }}>+5, n=&gt;n+1, 42</button>
      <button onClick={() => setTimeout(() => { setNumber((n) => n + 1); setClicks((c) => c + 1); }, 0)}>in timeout</button>
    </>
  );
}
render(<Counter />);
const h = () => screen.getByRole('heading').textContent;
async function press(label) {
  const before = renders;
  fireEvent.click(screen.getByText(label));
  await act(() => new Promise((r) => setTimeout(r, 5)));
  console.log(`${label.padEnd(16)} -> number = ${h().padStart(2)} | re-renders caused: ${renders - before}`);
}
await press('replace x3');
await press('updater x3');
await press('+5 then n=>n+1');
await press('+5, n=>n+1, 42');
await press('in timeout');
