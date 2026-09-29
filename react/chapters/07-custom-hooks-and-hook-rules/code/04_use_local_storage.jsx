// useLocalStorage: state that survives reloads and stays in sync across tabs.
import '../../../../tools_dom.mjs';
import { useState, useEffect } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

export function useLocalStorage(key, initialValue) {
  // 1. Lazy initial state: read storage once, on the first render only.
  const [value, setValue] = useState(() => {
    try {
      const raw = window.localStorage.getItem(key);
      return raw !== null ? JSON.parse(raw) : initialValue;
    } catch {
      return initialValue;                   // corrupted JSON or storage blocked
    }
  });

  // 2. Write whenever the value (or key) changes.
  useEffect(() => {
    try { window.localStorage.setItem(key, JSON.stringify(value)); } catch { /* quota / private mode */ }
  }, [key, value]);

  // 3. Other tabs fire a "storage" event when they change the same key.
  useEffect(() => {
    function onStorage(e) {
      if (e.key === key && e.newValue !== null) setValue(JSON.parse(e.newValue));
    }
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, [key]);

  return [value, setValue];                  // same shape as useState, so updaters work too
}

function Settings() {
  const [fontSize, setFontSize] = useLocalStorage('fontSize', 16);
  return (
    <div>
      <p>font size: {fontSize}px</p>
      <button onClick={() => setFontSize(s => s + 2)}>bigger</button>
    </div>
  );
}

const first = render(<Settings />);
console.log('first visit     :', screen.getByText(/font size/).textContent);
fireEvent.click(screen.getByText('bigger'));
fireEvent.click(screen.getByText('bigger'));
console.log('after 2 clicks  :', screen.getByText(/font size/).textContent, '| stored =', localStorage.getItem('fontSize'));
first.unmount();                             // "close the page"

render(<Settings />);                        // "reload"
console.log('after reload    :', screen.getByText(/font size/).textContent);

act(() => {                                  // another tab changes the same key
  localStorage.setItem('fontSize', '24');
  window.dispatchEvent(new window.StorageEvent('storage', { key: 'fontSize', newValue: '24' }));
});
console.log('other tab -> 24 :', screen.getByText(/font size/).textContent);

localStorage.setItem('fontSize', '{broken');  // corrupted value
const again = render(<Settings />);
console.log('corrupted JSON  :', again.container.querySelector('p').textContent, '(fell back to the default)');
