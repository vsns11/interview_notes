// tools_run: skip  (library module: the custom Hooks of this chapter, imported by 12_user_search.jsx and search-main.jsx)
import { useState, useEffect, useDebugValue } from 'react';

export function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => {
    try { const raw = localStorage.getItem(key); return raw !== null ? JSON.parse(raw) : initialValue; }
    catch { return initialValue; }
  });
  useEffect(() => { try { localStorage.setItem(key, JSON.stringify(value)); } catch {} }, [key, value]);
  return [value, setValue];
}

export function useDebounce(value, delayMs = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(id);
  }, [value, delayMs]);
  return debounced;
}

export function useFetch(url) {
  const [state, setState] = useState({ status: url ? 'loading' : 'idle', data: null, error: null });
  useDebugValue(state.status);                 // label shown next to this Hook in React DevTools
  useEffect(() => {
    if (!url) { setState({ status: 'idle', data: null, error: null }); return; }
    const controller = new AbortController();
    setState(s => ({ ...s, status: 'loading', error: null }));
    fetch(url, { signal: controller.signal })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json(); })
      .then(data => setState({ status: 'success', data, error: null }))
      .catch(error => { if (error.name !== 'AbortError') setState({ status: 'error', data: null, error }); });
    return () => controller.abort();
  }, [url]);
  return state;
}
