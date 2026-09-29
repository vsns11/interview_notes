// useDebounce: wait until the user stops typing before using a value.
import '../../../../tools_dom.mjs';
import { useState, useEffect } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

export function useDebounce(value, delayMs = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delayMs);  // schedule
    return () => clearTimeout(id);           // a newer value cancels the pending one
  }, [value, delayMs]);
  return debounced;
}

function Search() {
  const [text, setText] = useState('');
  const query = useDebounce(text, 100);
  useEffect(() => { if (query) console.log(`  -> would search for "${query}"`); }, [query]);
  return <input aria-label="search" value={text} onChange={e => setText(e.target.value)} />;
}

const sleep = (n) => act(() => new Promise(r => setTimeout(r, n)));
render(<Search />);
for (const partial of ['r', 're', 'rea', 'reac', 'react']) {   // typing: one key every 30 ms
  fireEvent.change(screen.getByLabelText('search'), { target: { value: partial } });
  console.log(`typed "${partial}"`);
  await sleep(30);
}
console.log('(pause 150 ms)');
await sleep(150);                                               // user pauses
fireEvent.change(screen.getByLabelText('search'), { target: { value: 'react hooks' } });
console.log(`typed "react hooks"`);
console.log('(pause 150 ms)');
await sleep(150);
