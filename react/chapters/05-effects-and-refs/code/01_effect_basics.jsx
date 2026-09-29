// useEffect runs AFTER React has updated the screen. The dependency array decides WHEN it runs again.
import '../../../../tools_dom.mjs';
import { useState, useEffect } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

function Demo() {
  const [count, setCount] = useState(0);
  const [name, setName] = useState('Ada');
  console.log(`render      count=${count} name=${name} | DOM says: ${document.querySelector('h1')?.textContent ?? '(nothing yet)'}`);

  useEffect(() => {
    console.log(`  effect A (no array)   -> after EVERY render; DOM says: ${document.querySelector('h1').textContent}`);
  });
  useEffect(() => {
    console.log('  effect B ([])         -> once, after the first render (mount)');
    return () => console.log('  cleanup B            -> on unmount');
  }, []);
  useEffect(() => {
    console.log(`  effect C ([name])     -> name is now ${name}`);
    return () => console.log(`  cleanup C            -> cleans up the "${name}" effect`);
  }, [name]);

  return (
    <>
      <h1>{name}: {count}</h1>
      <button onClick={() => setCount((c) => c + 1)}>count</button>
      <button onClick={() => setName('Grace')}>name</button>
    </>
  );
}

console.log('--- mount');
const { unmount } = render(<Demo />);
console.log('--- click count (name unchanged)');
fireEvent.click(screen.getByText('count'));
console.log('--- click name');
fireEvent.click(screen.getByText('name'));
console.log('--- click name again (same value: nothing committed, so no effects)');
fireEvent.click(screen.getByText('name'));
console.log('--- unmount');
act(() => unmount());
