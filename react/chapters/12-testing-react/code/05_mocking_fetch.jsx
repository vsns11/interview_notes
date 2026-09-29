// Mocking fetch: control what "the server" returns, so tests are fast, offline and deterministic.
import '../../../../tools_dom.mjs';
import { test, afterEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { useState, useEffect } from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

function UserProfile({ userId }) {
  const [state, setState] = useState({ status: 'loading' });
  useEffect(() => {
    const controller = new AbortController();
    setState({ status: 'loading' });
    fetch(`/api/users/${userId}`, { signal: controller.signal })
      .then(res => { if (!res.ok) throw new Error(`Server said ${res.status}`); return res.json(); })
      .then(user => setState({ status: 'done', user }))
      .catch(err => { if (err.name !== 'AbortError') setState({ status: 'error', message: err.message }); });
    return () => controller.abort();                   // cancel if userId changes or we unmount
  }, [userId]);

  if (state.status === 'loading') return <p>Loading…</p>;
  if (state.status === 'error') return <p role="alert">Could not load user: {state.message}</p>;
  return <h1>{state.user.name}</h1>;
}

// Helpers that build real Response objects (the Fetch API is built into Node 18+).
const json = (body, status = 200) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });

afterEach(() => { cleanup(); mock.restoreAll(); });    // put the real fetch back after every test

test('shows the user returned by the server, and calls the right URL', async () => {
  const fetchMock = mock.method(globalThis, 'fetch', async () => json({ id: 7, name: 'Ada Lovelace' }));
  render(<UserProfile userId={7} />);
  assert.equal((await screen.findByRole('heading')).textContent, 'Ada Lovelace');
  assert.equal(fetchMock.mock.callCount(), 1);
  assert.equal(fetchMock.mock.calls[0].arguments[0], '/api/users/7');
});

test('shows an error for an HTTP error status (fetch does not reject on 404)', async () => {
  mock.method(globalThis, 'fetch', async () => json({ error: 'not found' }, 404));
  render(<UserProfile userId={999} />);
  assert.equal((await screen.findByRole('alert')).textContent, 'Could not load user: Server said 404');
});

test('shows an error when the network fails', async () => {
  mock.method(globalThis, 'fetch', async () => { throw new TypeError('Failed to fetch'); });
  render(<UserProfile userId={1} />);
  assert.match((await screen.findByRole('alert')).textContent, /Failed to fetch/);
});

test('a slow old response cannot overwrite a newer one (race condition)', async () => {
  // user 1 answers after 100 ms, user 2 after 10 ms; honour the abort signal like real fetch does
  const delays = { '/api/users/1': 100, '/api/users/2': 10 };
  mock.method(globalThis, 'fetch', (url, { signal }) => new Promise((resolve, reject) => {
    const t = setTimeout(() => resolve(json({ name: `User from ${url}` })), delays[url]);
    signal.addEventListener('abort', () => { clearTimeout(t); reject(new DOMException('aborted', 'AbortError')); });
  }));
  const { rerender } = render(<UserProfile userId={1} />);
  rerender(<UserProfile userId={2} />);                   // user switches before the first answer
  assert.equal((await screen.findByRole('heading')).textContent, 'User from /api/users/2');
  await new Promise(r => setTimeout(r, 150));              // wait past the slow response
  assert.equal(screen.getByRole('heading').textContent, 'User from /api/users/2');
});

test('mock only the network call, then drive the UI as a user', async () => {
  let n = 0;
  mock.method(globalThis, 'fetch', async url => json({ name: `${url} #${++n}` }));
  function Picker() {
    const [id, setId] = useState(1);
    return <><button onClick={() => setId(i => i + 1)}>Next user</button><UserProfile userId={id} /></>;
  }
  const user = userEvent.setup();
  render(<Picker />);
  await screen.findByText('/api/users/1 #1');
  await user.click(screen.getByRole('button', { name: 'Next user' }));
  assert.ok(await screen.findByText('/api/users/2 #2'));
});
