// Machine coding 2 — autocomplete: debounced search, abortable requests, keyboard navigation,
// ARIA combobox pattern. `search(query, { signal })` is injected, so tests and the demo need no network.
import { useEffect, useId, useState } from 'react';

export function useDebouncedValue(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);                  // a new keystroke cancels the pending update
  }, [value, delay]);
  return debounced;
}

function Highlight({ text, query }) {
  const i = text.toLowerCase().indexOf(query.toLowerCase());
  if (i === -1 || !query) return text;
  return <>{text.slice(0, i)}<mark>{text.slice(i, i + query.length)}</mark>{text.slice(i + query.length)}</>;
}

export default function Autocomplete({ search, onSelect, label = 'Search', delay = 300, minChars = 1 }) {
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(-1);                 // highlighted option index, -1 = none
  const [result, setResult] = useState({ status: 'idle', items: [], for: '' });
  const debounced = useDebouncedValue(query.trim(), delay);
  const id = useId();
  const listId = `${id}-list`;

  useEffect(() => {
    if (debounced.length < minChars) return;
    const controller = new AbortController();
    setResult((r) => ({ ...r, status: 'loading' }));
    search(debounced, { signal: controller.signal })
      .then((items) => { setResult({ status: 'success', items, for: debounced }); setActive(-1); })
      .catch((err) => { if (err.name !== 'AbortError') setResult({ status: 'error', items: [], for: debounced }); });
    return () => controller.abort();                        // newer query (or unmount) cancels this one
  }, [debounced, minChars, search]);

  const tooShort = query.trim().length < minChars;
  const items = tooShort ? [] : result.items;
  const showList = open && !tooShort;

  function choose(item) {
    setQuery(item);
    setOpen(false);
    setActive(-1);
    onSelect?.(item);
  }

  function handleKeyDown(e) {
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault();                                   // keep the caret where it is
      setOpen(true);
      if (items.length === 0) return;
      const step = e.key === 'ArrowDown' ? 1 : -1;
      setActive((i) => (i + step + items.length) % items.length);   // wraps around both ends
    } else if (e.key === 'Enter' && showList && active >= 0) {
      e.preventDefault();                                   // don't submit a surrounding form
      choose(items[active]);
    } else if (e.key === 'Escape') {
      setOpen(false);
      setActive(-1);
    }
  }

  return (
    <div className="card ac">
      <label htmlFor={`${id}-input`}>{label}</label>
      <input
        id={`${id}-input`}
        role="combobox"
        aria-expanded={showList}
        aria-controls={listId}
        aria-autocomplete="list"
        aria-activedescendant={showList && active >= 0 ? `${id}-opt-${active}` : undefined}
        autoComplete="off"
        value={query}
        onChange={(e) => { setQuery(e.target.value); setOpen(true); setActive(-1); }}
        onKeyDown={handleKeyDown}
        onBlur={() => setOpen(false)}
      />
      {showList && (
        <div className="popup">
          {result.status === 'loading' && <p className="muted" role="status">Searching…</p>}
          {result.status === 'error' && <p className="error" role="alert">Something went wrong. Keep typing to retry.</p>}
          {result.status === 'success' && items.length === 0 && <p className="muted" role="status">No matches for “{result.for}”</p>}
          <ul id={listId} role="listbox" aria-label={`${label} suggestions`}>
            {items.map((item, i) => (
              <li
                key={item}
                id={`${id}-opt-${i}`}
                role="option"
                aria-selected={i === active}
                onMouseDown={(e) => e.preventDefault()}     // keep focus in the input so blur doesn't close first
                onMouseEnter={() => setActive(i)}
                onClick={() => choose(item)}
              >
                <Highlight text={item} query={result.for} />
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
