// useMemo caches an expensive CALCULATION between renders; it re-runs only when a dependency changes.
import '../../../../tools_dom.mjs';
import { useState, useMemo } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

const TODOS = Array.from({ length: 5000 }, (_, i) => ({ id: i, text: `task ${i}`, done: i % 3 === 0 }));
let filterRuns = 0;
function filterTodos(todos, tab) {
  filterRuns++;
  const t0 = performance.now(); while (performance.now() - t0 < 2) { /* simulate 2 ms of work */ }
  return todos.filter(t => (tab === 'all' ? true : tab === 'done' ? t.done : !t.done));
}

function Plain() {
  const [tab, setTab] = useState('all');
  const [dark, setDark] = useState(false);              // unrelated state
  const visible = filterTodos(TODOS, tab);              // recomputed on EVERY render
  return <View {...{ visible, dark, setDark, setTab }} />;
}
function Memoized() {
  const [tab, setTab] = useState('all');
  const [dark, setDark] = useState(false);
  const visible = useMemo(() => filterTodos(TODOS, tab), [tab]);  // cached until `tab` changes
  return <View {...{ visible, dark, setDark, setTab }} />;
}
function View({ visible, dark, setDark, setTab }) {
  return (
    <div className={dark ? 'dark' : 'light'}>
      <button onClick={() => setDark(d => !d)}>toggle theme</button>
      <button onClick={() => setTab('done')}>done tab</button>
      <p>{visible.length} visible</p>
    </div>
  );
}

for (const [label, C] of [['without useMemo', Plain], ['with useMemo   ', Memoized]]) {
  filterRuns = 0;
  render(<C />);
  for (let i = 0; i < 4; i++) fireEvent.click(screen.getByText('toggle theme'));  // 4 unrelated re-renders
  fireEvent.click(screen.getByText('done tab'));                                   // 1 relevant change
  console.log(`${label}: 6 renders, filterTodos ran ${filterRuns}x, shows "${screen.getByText(/visible/).textContent}"`);
  cleanup();
}
