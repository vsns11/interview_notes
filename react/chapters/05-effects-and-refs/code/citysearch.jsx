// tools_run: skip  (component module: used by 09_city_search.jsx and the browser entry citysearch-main.jsx)
import { useState, useEffect, useLayoutEffect, useRef } from 'react';

export const CITIES = [
  { name: 'Oslo', country: 'Norway', population: 717_710 },
  { name: 'Osaka', country: 'Japan', population: 2_752_412 },
  { name: 'Ottawa', country: 'Canada', population: 1_017_449 },
  { name: 'Porto', country: 'Portugal', population: 231_962 },
  { name: 'Toronto', country: 'Canada', population: 2_794_356 },
  { name: 'Tokyo', country: 'Japan', population: 14_094_034 },
];

// Fake API with latency that honours AbortSignal (like fetch). Longer queries answer faster, to provoke races.
export function createCityApi(baseDelay = 60) {
  return function searchCities(q, { signal } = {}) {
    return new Promise((resolve, reject) => {
      const t = setTimeout(() => {
        if (q === 'error') return reject(new Error('Service unavailable'));
        resolve(CITIES.filter((c) => c.name.toLowerCase().includes(q.toLowerCase())));
      }, Math.max(5, baseDelay - q.length * 10));
      signal?.addEventListener('abort', () => { clearTimeout(t); reject(new DOMException('Aborted', 'AbortError')); });
    });
  };
}

// Effect 1 (timer): a debounced copy of a value — only updates after `delay` ms without changes.
function useDebounced(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);                   // a new keystroke cancels the pending update
  }, [value, delay]);
  return debounced;
}

// useLayoutEffect: measure the tooltip before paint and flip it below the row if it would overflow the top.
// (It lives in the row, not in the tooltip: a parent's layout effect runs after the refs of its children are attached.)
function CityRow({ city, showTip }) {
  const rowRef = useRef(null);
  const tipRef = useRef(null);
  const [hover, setHover] = useState(showTip);
  const [pos, setPos] = useState(null);                // null = not measured yet
  useLayoutEffect(() => {
    if (!hover) { setPos(null); return; }
    const row = rowRef.current.getBoundingClientRect();
    const tipHeight = tipRef.current.getBoundingClientRect().height;
    const below = row.top - tipHeight - 6 < 0;          // no room above -> show below
    setPos({ top: (below ? row.bottom + 6 : row.top - tipHeight - 6) + window.scrollY, left: row.left + 180, below });
  }, [hover]);
  return (
    <li ref={rowRef} tabIndex={0} onMouseEnter={() => setHover(true)} onMouseLeave={() => setHover(false)}
      onFocus={() => setHover(true)} onBlur={() => setHover(false)}>
      <strong>{city.name}</strong> <span>{city.country}</span>
      {hover && (
        <div ref={tipRef} role="tooltip" className={`tooltip${pos?.below ? ' below' : ''}`}
          style={{ top: pos?.top ?? 0, left: pos?.left ?? 0, visibility: pos ? 'visible' : 'hidden' }}>
          Population {city.population.toLocaleString('en-US')}
        </div>
      )}
    </li>
  );
}

export default function CitySearch({ searchCities, delay = 250, initialQuery = '', tipFor = null }) {
  const [query, setQuery] = useState(initialQuery);
  const [result, setResult] = useState({ status: 'idle', cities: [] });
  const inputRef = useRef(null);
  const debouncedQuery = useDebounced(query.trim(), delay);

  // Effect 2 (DOM): focus the box when the page opens.
  useEffect(() => { inputRef.current.focus(); }, []);

  // Effect 3 (global listener): press "/" anywhere to jump to the search box.
  useEffect(() => {
    function onKeyDown(e) {
      if (e.key === '/' && document.activeElement !== inputRef.current) { e.preventDefault(); inputRef.current.focus(); }
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  // Effect 4 (network): fetch for the debounced query; abort the previous request on change.
  useEffect(() => {
    if (!debouncedQuery) { setResult({ status: 'idle', cities: [] }); return; }
    const controller = new AbortController();
    setResult((r) => ({ ...r, status: 'loading' }));  // keep old results visible while loading
    searchCities(debouncedQuery, { signal: controller.signal })
      .then((cities) => setResult({ status: 'done', cities }))
      .catch((e) => { if (e.name !== 'AbortError') setResult({ status: 'error', cities: [], message: e.message }); });
    return () => controller.abort();
  }, [debouncedQuery, searchCities]);

  // Effect 5 (browser API): keep the tab title in sync.
  useEffect(() => {
    document.title = debouncedQuery ? `“${debouncedQuery}” – City search` : 'City search';
  }, [debouncedQuery]);

  const count = result.cities.length;                   // derived
  return (
    <div className="search">
      <h1>City search</h1>
      <input ref={inputRef} aria-label="Search cities" placeholder="Type a city…  (press / to focus)" value={query} onChange={(e) => setQuery(e.target.value)} />
      <p className="status" role="status">
        {result.status === 'idle' && 'Start typing to search.'}
        {result.status === 'loading' && 'Searching…'}
        {result.status === 'done' && `${count} ${count === 1 ? 'city' : 'cities'} for “${debouncedQuery}”`}
        {result.status === 'error' && `Error: ${result.message}`}
      </p>
      <ul>{result.cities.map((c) => <CityRow key={c.name} city={c} showTip={c.name === tipFor} />)}</ul>
    </div>
  );
}
