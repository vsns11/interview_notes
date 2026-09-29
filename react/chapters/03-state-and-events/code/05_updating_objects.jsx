// Objects in state: mutation does not re-render; create a new object instead.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

let renders = 0;
function Form() {
  const [person, setPerson] = useState({
    name: 'Niki de Saint Phalle',
    artwork: { title: 'Blue Nana', city: 'Hamburg' },
  });
  renders++;
  return (
    <>
      <p>{person.name} — {person.artwork.title} ({person.artwork.city})</p>
      <button onClick={() => { person.name = 'MUTATED'; setPerson(person); }}>mutate</button>
      <button onClick={() => setPerson({ ...person, name: 'Niki' })}>copy name</button>
      <button onClick={() => setPerson({ ...person, artwork: { ...person.artwork, city: 'Berlin' } })}>copy nested city</button>
    </>
  );
}
render(<Form />);
const show = (label) => console.log(`${label.padEnd(17)} -> "${screen.getByRole('paragraph').textContent}" | renders so far: ${renders}`);
show('initial');
fireEvent.click(screen.getByText('mutate'));            show('mutate + set same');
fireEvent.click(screen.getByText('copy name'));         show('copy name');
fireEvent.click(screen.getByText('copy nested city'));  show('copy nested city');
