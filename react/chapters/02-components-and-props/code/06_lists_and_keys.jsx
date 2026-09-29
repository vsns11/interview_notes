// Rendering lists with map/filter, and why keys must be stable IDs (not indexes).
import '../../../../tools_dom.mjs';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

const people = [
  { id: 0, name: 'Creola Katherine Johnson', profession: 'mathematician' },
  { id: 1, name: 'Mario José Molina-Pasquel Henríquez', profession: 'chemist' },
  { id: 2, name: 'Mohammad Abdus Salam', profession: 'physicist' },
  { id: 3, name: 'Percy Lavon Julian', profession: 'chemist' },
];

function ChemistList() {
  const chemists = people.filter((p) => p.profession === 'chemist');
  return <ul>{chemists.map((p) => <li key={p.id}>{p.name}</li>)}</ul>;
}
console.log(render(<ChemistList />).container.innerHTML.replaceAll('><', '>\n<'));

// Missing key -> React logs a warning in development.
const warnings = [];
const orig = console.error;
console.error = (...a) => warnings.push(String(a[0]).split('\n')[0]);
render(<ul>{people.map((p) => <li>{p.name}</li>)}</ul>);
console.error = orig;
console.log('\nwarning:', warnings[0].replaceAll('%s', ''));
cleanup(); // unmount everything rendered so far

// Why index keys break: each row has an uncontrolled <input> holding a user's note (DOM state).
function NoteList({ items, useIndexKey }) {
  return (
    <ul>
      {items.map((item, index) => (
        <li key={useIndexKey ? index : item.id}>
          {item.label}: <input aria-label={`note for ${item.label}`} defaultValue="" />
        </li>
      ))}
    </ul>
  );
}

for (const useIndexKey of [true, false]) {
  const first = [{ id: 'b', label: 'Bread' }, { id: 'm', label: 'Milk' }];
  const { rerender, unmount } = render(<NoteList items={first} useIndexKey={useIndexKey} />);
  fireEvent.change(screen.getByLabelText('note for Bread'), { target: { value: 'wholegrain' } });
  // Insert a new item at the TOP of the list
  rerender(<NoteList items={[{ id: 'e', label: 'Eggs' }, ...first]} useIndexKey={useIndexKey} />);
  const rows = screen.getAllByRole('listitem').map((li) => `${li.firstChild.textContent}"${li.querySelector('input').value}"`);
  console.log(`${useIndexKey ? 'key=index' : 'key=id   '} after prepend ->`, rows.join('  '));
  unmount();
}
