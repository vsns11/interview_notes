// Virtualization (windowing): 10,000 rows, but only ~20 DOM rows at a time.
import '../../../../tools_dom.mjs';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { VirtualList } from './virtual_list.jsx';

const ITEMS = Array.from({ length: 10_000 }, (_, i) => ({ id: i, name: `Customer #${i}` }));

// Naive version: every row is a real DOM node.
function NaiveList({ items }) {
  return <div role="list">{items.map(it => <div role="listitem" key={it.id}>{it.name}</div>)}</div>;
}

let t = performance.now();
render(<NaiveList items={ITEMS} />);
const naiveMs = performance.now() - t;
console.log(`naive list    : ${document.querySelectorAll('[role=listitem]').length} row nodes, ` +
  `${document.getElementsByTagName('*').length} elements in document`);
cleanup();

t = performance.now();
render(<VirtualList items={ITEMS} rowHeight={30} height={300} overscan={3}
                    renderRow={it => it.name} />);
const virtualMs = performance.now() - t;
const rowsInDom = () => screen.getAllByRole('listitem');
const range = () => { const r = rowsInDom(); return `${r[0].textContent} .. ${r.at(-1).textContent}`; };
console.log(`virtual list  : ${rowsInDom().length} row nodes, ` +
  `${document.getElementsByTagName('*').length} elements in document`);
console.log(`  at top          -> rows ${range()}`);

// Simulate the user scrolling. jsdom does no layout, so we set scrollTop ourselves and fire 'scroll'.
const viewport = screen.getByRole('list', { name: 'virtual list' });
for (const y of [3000, 150_000, 299_700]) {
  viewport.scrollTop = y;
  fireEvent.scroll(viewport);
  console.log(`  scrollTop=${String(y).padEnd(7)}-> rows ${range()}  (${rowsInDom().length} in DOM)`);
}
console.log(`\nmount time (varies per run): naive ${naiveMs.toFixed(0)} ms, virtual ${virtualMs.toFixed(0)} ms`);
