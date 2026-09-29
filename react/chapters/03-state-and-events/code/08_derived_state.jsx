// Keep the minimal state; calculate everything else during render.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

// 1. Redundant state: fullName can be computed from first + last.
function NameForm() {
  const [first, setFirst] = useState('Ada');
  const [last, setLast] = useState('Lovelace');
  const fullName = `${first} ${last}`;              // derived: no useState, no sync code
  return (
    <>
      <input aria-label="first" value={first} onChange={(e) => setFirst(e.target.value)} />
      <input aria-label="last" value={last} onChange={(e) => setLast(e.target.value)} />
      <p>Ticket for: {fullName}</p>
    </>
  );
}
render(<NameForm />);
fireEvent.change(screen.getByLabelText('last'), { target: { value: 'King' } });
console.log('derived fullName   :', screen.getByRole('paragraph').textContent);
cleanup();

// 2. Duplicated state: store the selected ID, not a copy of the selected object.
function Menu({ storeObject }) {
  const [items, setItems] = useState([{ id: 0, title: 'pretzels' }, { id: 1, title: 'seaweed' }]);
  const [selectedItem, setSelectedItem] = useState(items[0]);     // BAD: a copy
  const [selectedId, setSelectedId] = useState(0);                // GOOD: a reference by ID
  const selected = storeObject ? selectedItem : items.find((i) => i.id === selectedId);
  return (
    <>
      <button onClick={() => setItems(items.map((i) => (i.id === 0 ? { ...i, title: 'PRETZELS (renamed)' } : i)))}>rename first</button>
      <p>You picked: {selected.title}</p>
      <button onClick={() => { setSelectedItem(items[0]); setSelectedId(0); }}>pick</button>
    </>
  );
}
for (const storeObject of [true, false]) {
  render(<Menu storeObject={storeObject} />);
  fireEvent.click(screen.getByText('rename first'));
  console.log(storeObject ? 'store object copy  :' : 'store selected id  :', screen.getByRole('paragraph').textContent);
  cleanup();
}

// 3. Mirroring a prop in state ignores later prop changes.
function MessageMirror({ color }) { const [c] = useState(color); return <p style={{ color: c }}>mirror: {c}</p>; }
function MessageDirect({ color }) { return <p style={{ color }}>direct: {color}</p>; }
const { rerender } = render(<><MessageMirror color="blue" /><MessageDirect color="blue" /></>);
rerender(<><MessageMirror color="red" /><MessageDirect color="red" /></>);
console.log('parent changed to red ->', screen.getAllByRole('paragraph').map((p) => p.textContent).join(' | '));
