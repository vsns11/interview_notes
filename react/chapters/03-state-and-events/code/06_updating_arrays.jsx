// Arrays in state: the non-mutating recipes, plus Immer for deep updates.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { produce } from 'immer';
import { render, screen, fireEvent, within } from '@testing-library/react';

let nextId = 4;
const initial = [
  { id: 1, title: 'Big Bellies', seen: false },
  { id: 2, title: 'Lunar Landscape', seen: false },
  { id: 3, title: 'Terracotta Army', seen: true },
];

function Bucket() {
  const [items, setItems] = useState(initial);
  const add = () => setItems([...items, { id: nextId++, title: 'Aurora', seen: false }]);          // add to end
  const addFirst = () => setItems([{ id: nextId++, title: 'Fjords', seen: false }, ...items]);     // add to start
  const insertAt1 = () => setItems([...items.slice(0, 1), { id: nextId++, title: 'Glacier', seen: false }, ...items.slice(1)]);
  const remove = (id) => setItems(items.filter((it) => it.id !== id));                               // remove
  const toggle = (id) => setItems(items.map((it) => (it.id === id ? { ...it, seen: !it.seen } : it))); // update one
  const sortAZ = () => setItems(items.toSorted((a, b) => a.title.localeCompare(b.title)));          // sort a copy
  const reverse = () => setItems([...items].reverse());                                              // copy, then reverse
  const immerToggle = (id) => setItems(produce((draft) => {                                          // Immer: "mutate" a draft
    const it = draft.find((d) => d.id === id); it.seen = !it.seen;
  }));
  return (
    <>
      <ul>{items.map((it) => (
        <li key={it.id}>{it.seen ? '[x]' : '[ ]'} {it.title}
          <button onClick={() => toggle(it.id)}>toggle</button>
          <button onClick={() => immerToggle(it.id)}>immer</button>
          <button onClick={() => remove(it.id)}>remove</button>
        </li>))}
      </ul>
      <button onClick={add}>add</button><button onClick={addFirst}>add first</button>
      <button onClick={insertAt1}>insert at 1</button><button onClick={sortAZ}>sort</button><button onClick={reverse}>reverse</button>
    </>
  );
}
render(<Bucket />);
const rows = () => screen.getAllByRole('listitem').map((li) => li.firstChild.textContent + li.childNodes[1].textContent + li.childNodes[2].textContent).join(', ');
const row = (title) => within(screen.getAllByRole('listitem').find((li) => li.textContent.includes(title)));
const step = (label, fn) => { fn(); console.log(label.padEnd(24), '->', rows()); };
console.log('initial'.padEnd(24), '->', rows());
step('add', () => fireEvent.click(screen.getByText('add')));
step('add first', () => fireEvent.click(screen.getByText('add first')));
step('insert at 1', () => fireEvent.click(screen.getByText('insert at 1')));
step('remove Lunar Landscape', () => fireEvent.click(row('Lunar').getByText('remove')));
step('toggle Big Bellies', () => fireEvent.click(row('Big Bellies').getByText('toggle')));
step('immer toggle Aurora', () => fireEvent.click(row('Aurora').getByText('immer')));
step('sort', () => fireEvent.click(screen.getByText('sort')));
step('reverse', () => fireEvent.click(screen.getByText('reverse')));
console.log('original array untouched:', initial.map((i) => `${i.seen ? '[x]' : '[ ]'} ${i.title}`).join(', '));
