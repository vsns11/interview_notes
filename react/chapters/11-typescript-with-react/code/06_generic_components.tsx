// Generic components: one component, many item types, full type safety.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import type { ReactNode, Key } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// 1. A list that works for any item type T. TypeScript infers T from `items`.
type ListProps<T> = {
  items: T[];
  getKey: (item: T) => Key;
  renderItem: (item: T) => ReactNode;
  emptyText?: string;
};
function List<T>({ items, getKey, renderItem, emptyText = 'Nothing here' }: ListProps<T>) {
  if (items.length === 0) return <p>{emptyText}</p>;
  return <ul>{items.map(item => <li key={getKey(item)}>{renderItem(item)}</li>)}</ul>;
}
// (In an arrow function inside a .tsx file write <T,> so the parser doesn't read <T> as JSX:)
const Count = <T,>({ items }: { items: T[] }) => <small>{items.length} items</small>;

// 2. A select whose value type is a union of string literals, inferred from `options`.
type SelectProps<V extends string> = {
  label: string;
  value: V;
  options: readonly { value: V; label: string }[];
  onChange: (value: V) => void;
};
function Select<V extends string>({ label, value, options, onChange }: SelectProps<V>) {
  return (
    <label>{label}{' '}
      <select value={value} onChange={e => onChange(e.target.value as V)}>  {/* DOM gives string; we know it's V */}
        {options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </label>
  );
}

// 3. A generic table where columns can only name real keys of the row type.
type Column<Row> = { key: keyof Row & string; header: string };
function Table<Row extends { id: number }>({ rows, columns }: { rows: Row[]; columns: Column<Row>[] }) {
  return (
    <table><thead><tr>{columns.map(c => <th key={c.key}>{c.header}</th>)}</tr></thead>
      <tbody>{rows.map(r => <tr key={r.id}>{columns.map(c => <td key={c.key}>{String(r[c.key])}</td>)}</tr>)}</tbody>
    </table>
  );
}

type Book = { id: number; title: string; year: number };
const books: Book[] = [{ id: 1, title: 'Dune', year: 1965 }, { id: 2, title: 'Neuromancer', year: 1984 },
  { id: 3, title: 'Beloved', year: 1987 }];
const SORTS = [{ value: 'title', label: 'Title' }, { value: 'year', label: 'Year' }] as const;

function App() {
  const [sort, setSort] = useState<'title' | 'year'>('title');     // must be one of SORTS' values
  const sorted = [...books].sort((a, b) => (sort === 'year' ? a.year - b.year : a.title.localeCompare(b.title)));
  return (
    <>
      <Select label="Sort by" value={sort} options={SORTS} onChange={setSort} />
      <List items={sorted} getKey={b => b.id} renderItem={b => `${b.title} (${b.year})`} />
      <List items={[] as string[]} getKey={s => s} renderItem={s => s} emptyText="No tags" />
      <Count items={books} />
      <Table rows={books} columns={[{ key: 'title', header: 'Title' }, { key: 'year', header: 'Year' }]} />
    </>
  );
}

render(<App />);
console.log('list :', screen.getAllByRole('listitem').map(li => li.textContent).join(' | '));
fireEvent.change(screen.getByLabelText(/Sort by/), { target: { value: 'year' } });
console.log('year :', screen.getAllByRole('listitem').map(li => li.textContent).join(' | '));
console.log('other:', screen.getByText('No tags').textContent, '/', screen.getByText(/items$/).textContent);
console.log('table:', screen.getAllByRole('row').map(r => r.textContent).join(' | '));
