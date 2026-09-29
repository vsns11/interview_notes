// Fetching in an Effect: without cleanup, a slow old response can overwrite a newer one (a race condition).
import '../../../../tools_dom.mjs';
import { useState, useEffect } from 'react';
import { render, screen, act, cleanup } from '@testing-library/react';

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const delays = { Alice: 60, Bob: 10, Nobody: 5 };
const events = [];
// Fake API: Alice is slow, Bob is fast, Nobody fails. Honors an AbortSignal like fetch() does.
function fetchBio(person, { signal } = {}) {
  return new Promise((resolve, reject) => {
    const t = setTimeout(() => (person === 'Nobody' ? reject(new Error('404 Not Found')) : resolve(`This is ${person}'s bio.`)), delays[person]);
    signal?.addEventListener('abort', () => { clearTimeout(t); events.push(`aborted ${person}`); reject(new DOMException('Aborted', 'AbortError')); });
  });
}

function BioNaive({ person }) {                               // 🔴 no cleanup
  const [bio, setBio] = useState(null);
  useEffect(() => { fetchBio(person).then(setBio); }, [person]);
  return <p>{bio ?? 'Loading…'}</p>;
}

function BioIgnore({ person }) {                              // ✅ ignore stale responses
  const [bio, setBio] = useState(null);
  useEffect(() => {
    let ignore = false;
    setBio(null);
    fetchBio(person).then((result) => { if (!ignore) setBio(result); });
    return () => { ignore = true; };                          // runs when person changes or on unmount
  }, [person]);
  return <p>{bio ?? 'Loading…'}</p>;
}

function BioAbort({ person }) {                               // ✅ cancel the request itself + loading/error states
  const [state, setState] = useState({ status: 'loading' });
  useEffect(() => {
    const controller = new AbortController();
    setState({ status: 'loading' });
    fetchBio(person, { signal: controller.signal })
      .then((bio) => setState({ status: 'success', bio }))
      .catch((err) => { if (err.name !== 'AbortError') setState({ status: 'error', message: err.message }); });
    return () => controller.abort();
  }, [person]);
  if (state.status === 'loading') return <p>Loading…</p>;
  if (state.status === 'error') return <p role="alert">Could not load: {state.message}</p>;
  return <p>{state.bio}</p>;
}

for (const Bio of [BioNaive, BioIgnore, BioAbort]) {
  events.length = 0;
  const r = render(<Bio person="Alice" />);
  await act(() => wait(2));
  r.rerender(<Bio person="Bob" />);                           // user quickly picks Bob
  await act(() => wait(100));
  console.log(`${Bio.name.padEnd(9)} selected Bob, screen shows: "${screen.getByRole('paragraph').textContent}"`, events.length ? events : '');
  cleanup();
}
render(<BioAbort person="Nobody" />);
await act(() => wait(20));
console.log('BioAbort  error state:', screen.getByRole('alert').textContent);
