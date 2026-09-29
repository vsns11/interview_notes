// useFetch: loading / error / data states, cancellation, and no race conditions.
// Talks to a tiny local HTTP server started in this same script (no internet needed).
import '../../../../tools_dom.mjs';
import http from 'node:http';
import { useState, useEffect } from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';

// ---- local API: /api/users/1 is SLOW (150 ms), /api/users/2 is fast (20 ms), others 404 ----
const USERS = { 1: { name: 'Ada' }, 2: { name: 'Grace' } };
const server = http.createServer((req, res) => {
  const id = req.url.split('/').pop();
  setTimeout(() => {
    if (!USERS[id]) { res.writeHead(404); return res.end(); }
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(USERS[id]));
  }, id === '1' ? 150 : 20);
});
await new Promise(r => server.listen(0, '127.0.0.1', r));
const BASE = `http://127.0.0.1:${server.address().port}`;

export function useFetch(url) {
  const [state, setState] = useState({ status: 'idle', data: null, error: null });
  useEffect(() => {
    if (!url) return;
    const controller = new AbortController();          // lets cleanup cancel the request
    setState(s => ({ ...s, status: 'loading', error: null }));
    fetch(url, { signal: controller.signal })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json(); })
      .then(data => setState({ status: 'success', data, error: null }))
      .catch(error => {
        if (error.name === 'AbortError') return;         // we cancelled it: not a real error
        setState({ status: 'error', data: null, error });
      });
    return () => controller.abort();                     // url changed or unmounted
  }, [url]);
  return state;
}

// The naive version: no cleanup, so whichever response arrives LAST wins.
function useFetchNaive(url) {
  const [data, setData] = useState(null);
  useEffect(() => { fetch(url).then(r => r.json()).then(setData); }, [url]);
  return data;
}
function NaiveProfile({ id }) {
  const data = useFetchNaive(`${BASE}/api/users/${id}`);
  return <p>Asked for user {id}, showing: {data ? data.name : '…'}</p>;
}

function Profile({ id }) {
  const { status, data, error } = useFetch(`${BASE}/api/users/${id}`);
  if (status === 'loading' && !data) return <p>Loading…</p>;
  if (status === 'error') return <p role="alert">Could not load user {id}: {error.message}</p>;
  if (!data) return null;
  return <p>User {id}: {data.name}{status === 'loading' ? ' (refreshing…)' : ''}</p>;
}

const naive = render(<NaiveProfile id={1} />);
naive.rerender(<NaiveProfile id={2} />);
await act(() => new Promise(r => setTimeout(r, 250)));
console.log('naive         :', naive.container.textContent, '<- WRONG user (race condition)');
naive.unmount();

const { rerender, container } = render(<Profile id={1} />);
console.log('initial       :', container.textContent);
rerender(<Profile id={2} />);                            // switch BEFORE the slow request finishes
await waitFor(() => screen.getByText(/Grace/));
console.log('switched to 2 :', container.textContent);
await act(() => new Promise(r => setTimeout(r, 200)));              // give the slow request time to "arrive"
console.log('200 ms later  :', container.textContent, '(the slow Ada response was aborted, not shown)');
rerender(<Profile id={99} />);
await waitFor(() => screen.getByRole('alert'));
console.log('missing user  :', container.textContent);
server.close();
