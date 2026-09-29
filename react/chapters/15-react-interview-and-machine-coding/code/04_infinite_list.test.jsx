// Tests for infinite-list.jsx. jsdom has no layout, so it has no IntersectionObserver:
// we install a tiny fake whose `intersect()` lets the test say "the sentinel is now visible".
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InfiniteList from './infinite-list.jsx';

const observers = new Set();
class FakeIntersectionObserver {
  constructor(callback, options) { this.callback = callback; this.options = options; this.targets = []; }
  observe(el) { this.targets.push(el); observers.add(this); }
  unobserve(el) { this.targets = this.targets.filter((t) => t !== el); }
  disconnect() { this.targets = []; observers.delete(this); }
}
globalThis.IntersectionObserver = window.IntersectionObserver = FakeIntersectionObserver;

// "Scroll" until the sentinel is visible: call every live observer, then let promises settle.
async function intersect() {
  await act(async () => {
    for (const o of [...observers]) o.callback(o.targets.map((target) => ({ target, isIntersecting: true })), o);
    await new Promise((r) => setTimeout(r, 0));
  });
}

// A fake paginated API: 3 pages of 2 items; can be told to fail once.
function fakeApi() {
  const api = { calls: [], failNext: false };
  api.fetchPage = async (page) => {
    api.calls.push(page);
    if (api.failNext) { api.failNext = false; throw new Error('network'); }
    const items = [1, 2].map((i) => ({ id: (page - 1) * 2 + i, name: `Post ${(page - 1) * 2 + i}` }));
    return { items, hasMore: page < 3 };
  };
  return api;
}
const names = () => screen.queryAllByRole('listitem').map((li) => li.textContent);

afterEach(() => { cleanup(); observers.clear(); });

test('loads the next page each time the sentinel becomes visible, then stops', async () => {
  const api = fakeApi();
  render(<InfiniteList fetchPage={api.fetchPage} />);
  assert.deepEqual(names(), []);
  assert.equal(observers.size, 1);
  const [o] = observers;
  assert.equal(o.options.rootMargin, '200px');
  assert.equal(o.options.root, screen.getByLabelText('Feed items'));   // observes inside the scroll box

  await intersect();
  assert.deepEqual(names(), ['Post 1', 'Post 2']);
  await intersect();
  await intersect();
  assert.deepEqual(names(), ['Post 1', 'Post 2', 'Post 3', 'Post 4', 'Post 5', 'Post 6']);
  assert.equal(screen.getByText('You’ve reached the end.').tagName, 'P');
  assert.equal(observers.size, 0);                                     // nothing left to observe
  await intersect();
  assert.deepEqual(api.calls, [1, 2, 3]);                              // no request after the end
});

test('no duplicate requests while one is in flight', async () => {
  let release;
  const calls = [];
  const fetchPage = (page) => { calls.push(page); return new Promise((r) => { release = () => r({ items: [{ id: page, name: `P${page}` }], hasMore: true }); }); };
  render(<InfiniteList fetchPage={fetchPage} />);
  const [o] = observers;
  await act(async () => {                                             // the observer fires three times quickly
    for (let i = 0; i < 3; i++) o.callback([{ isIntersecting: true }], o);
  });
  assert.equal(screen.getByRole('status').textContent, 'Loading more…');
  assert.deepEqual(calls, [1]);
  await act(async () => release());
  assert.deepEqual(names(), ['P1']);
});

test('error shows Retry, and Retry resumes loading', async () => {
  const user = userEvent.setup();
  const api = fakeApi();
  api.failNext = true;
  render(<InfiniteList fetchPage={api.fetchPage} />);
  await intersect();
  assert.match(screen.getByRole('alert').textContent, /Couldn’t load more/);
  assert.equal(observers.size, 0);                                     // paused while in error
  await user.click(screen.getByRole('button', { name: 'Retry' }));
  await intersect();
  assert.deepEqual(names(), ['Post 1', 'Post 2']);
  assert.deepEqual(api.calls, [1, 1]);
});
