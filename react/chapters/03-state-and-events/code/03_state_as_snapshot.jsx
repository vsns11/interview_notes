// State behaves like a snapshot: inside one render, `number` never changes.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

function Counter() {
  const [number, setNumber] = useState(0);
  return (
    <>
      <h1>{number}</h1>
      <button onClick={() => {
        setNumber(number + 1);   // setNumber(0 + 1)
        setNumber(number + 1);   // setNumber(0 + 1)
        setNumber(number + 1);   // setNumber(0 + 1)
        console.log('  right after the three calls, number =', number);
      }}>+3</button>
    </>
  );
}
render(<Counter />);
fireEvent.click(screen.getByText('+3'));
console.log('heading after one click:', screen.getByRole('heading').textContent);

// Even a delayed callback sees the snapshot from the render it was created in.
function Delayed() {
  const [n, setN] = useState(0);
  return (
    <p>
      n is <output>{n}</output>{' '}
      <button onClick={() => { setN(n + 5); setTimeout(() => console.log('  timeout fired, n =', n), 10); }}>+5 later</button>
    </p>
  );
}
render(<Delayed />);
fireEvent.click(screen.getByText('+5 later'));
await act(() => new Promise((r) => setTimeout(r, 30)));
console.log('screen shows n =', screen.getByRole('status').textContent, '(the timeout logged the old snapshot)');
