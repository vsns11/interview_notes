// Controlled vs uncontrolled inputs: who owns the value — React state or the DOM?
import '../../../../tools_dom.mjs';
import { useState, useRef } from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

globalThis.FormData = window.FormData; // use jsdom's FormData (Node's own can't read a <form>)
let renders = 0;

// 1. Uncontrolled: the DOM keeps the value. defaultValue/defaultChecked set only the START value.
function UncontrolledSignup() {
  renders++;
  function handleSubmit(e) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);          // read every named field at once
    console.log('  submit ->', JSON.stringify(Object.fromEntries(data)));
  }
  return (
    <form onSubmit={handleSubmit}>
      <input name="city" aria-label="City" defaultValue="Oslo" />
      <select name="size" aria-label="Size" defaultValue="m">
        <option value="s">S</option><option value="m">M</option><option value="l">L</option>
      </select>
      <label><input type="checkbox" name="gift" defaultChecked /> Gift wrap</label>
      <button>Order</button>
    </form>
  );
}

// 2. Uncontrolled + ref: read one field on demand without re-rendering.
function SearchBox() {
  const inputRef = useRef(null);
  return (
    <>
      <input ref={inputRef} aria-label="Search" />
      <button onClick={() => console.log('  ref reads:', inputRef.current.value)}>Go</button>
    </>
  );
}

// 3. Controlled: React state owns the value, so every keystroke re-renders.
function ControlledCity() {
  const [city, setCity] = useState('Oslo');
  renders++;
  return <input aria-label="Controlled city" value={city} onChange={(e) => setCity(e.target.value.toUpperCase())} />;
}

// 4. The classic warning: value starts undefined (uncontrolled) and later becomes a string (controlled).
function Flip() {
  const [v, setV] = useState();                          // undefined!
  return <input aria-label="Flip" value={v} onChange={(e) => setV(e.target.value)} />;
}

const user = userEvent.setup();
render(<UncontrolledSignup />);
await user.clear(screen.getByLabelText('City'));
await user.type(screen.getByLabelText('City'), 'Bergen');
await user.selectOptions(screen.getByLabelText('Size'), 'l');
await user.click(screen.getByLabelText('Gift wrap'));
await user.click(screen.getByRole('button', { name: 'Order' }));
console.log('uncontrolled renders while typing 6 letters:', renders);
cleanup();

render(<SearchBox />);
await user.type(screen.getByLabelText('Search'), 'hooks');
await user.click(screen.getByRole('button', { name: 'Go' }));
cleanup();

renders = 0;
render(<ControlledCity />);
await user.type(screen.getByLabelText('Controlled city'), 'ville');
console.log('controlled shows:', screen.getByLabelText('Controlled city').value, '| renders:', renders);
cleanup();

const errors = []; const orig = console.error;
console.error = (...a) => errors.push(String(a[0]).split('\n')[0]);
render(<Flip />);
await user.type(screen.getByLabelText('Flip'), 'a');
console.error = orig;
console.log('warning:', errors[0]?.slice(0, 90) + '...');
