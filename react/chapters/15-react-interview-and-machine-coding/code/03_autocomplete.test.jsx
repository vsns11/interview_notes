// Tests for autocomplete.jsx: debounce, abort of stale requests, keyboard navigation, ARIA state.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Autocomplete from './autocomplete.jsx';

afterEach(cleanup);
const FRUITS = ['Apple', 'Apricot', 'Banana', 'Grape', 'Grapefruit', 'Pineapple'];

// A fake API whose requests we resolve by hand, so the test controls the timing exactly.
function fakeSearch() {
  const calls = [];
  const search = (q, { signal }) => new Promise((resolve, reject) => {
    const call = { q, signal, resolve: () => resolve(FRUITS.filter((f) => f.toLowerCase().includes(q.toLowerCase()))) };
    signal.addEventListener('abort', () => reject(new DOMException('aborted', 'AbortError')));
    calls.push(call);
  });
  return { search, calls };
}
const options = () => screen.queryAllByRole('option').map((o) => o.textContent);

test('debounce: typing quickly sends ONE request, for the final text', async () => {
  const user = userEvent.setup();
  const { search, calls } = fakeSearch();
  render(<Autocomplete search={search} delay={100} label="Fruit" />);
  await user.type(screen.getByRole('combobox', { name: 'Fruit' }), 'gra');   // 3 keystrokes, a few ms apart
  await waitFor(() => assert.equal(calls.length, 1));
  assert.equal(calls[0].q, 'gra');
  calls[0].resolve();
  assert.deepEqual(await screen.findAllByRole('option').then((o) => o.map((x) => x.textContent)), ['Grape', 'Grapefruit']);
  assert.equal(screen.getByRole('combobox').getAttribute('aria-expanded'), 'true');
});

test('abort: a newer query cancels the older request, so stale results never show', async () => {
  const user = userEvent.setup();
  const { search, calls } = fakeSearch();
  render(<Autocomplete search={search} delay={50} label="Fruit" />);
  const box = screen.getByRole('combobox');
  await user.type(box, 'ap');
  await waitFor(() => assert.equal(calls.length, 1));      // request #1 ("ap") is in flight
  await user.type(box, 'r');
  await waitFor(() => assert.equal(calls.length, 2));      // request #2 ("apr") started
  assert.equal(calls[0].signal.aborted, true);             // ...and #1 was aborted by the effect cleanup
  calls[1].resolve();                                      // the answers arrive OUT of order:
  await waitFor(() => assert.deepEqual(options(), ['Apricot']));
  calls[0].resolve();                                      // the late answer for "ap" must be ignored
  await new Promise((r) => setTimeout(r, 20));
  assert.deepEqual(options(), ['Apricot']);
});

test('keyboard: arrows move (and wrap), Enter selects, Escape closes', async () => {
  const user = userEvent.setup();
  const { search, calls } = fakeSearch();
  const picked = [];
  render(<Autocomplete search={search} delay={10} label="Fruit" onSelect={(v) => picked.push(v)} />);
  const box = screen.getByRole('combobox');
  await user.type(box, 'ap');
  await waitFor(() => assert.equal(calls.length, 1));
  calls[0].resolve();
  await screen.findAllByRole('option');
  assert.deepEqual(options(), ['Apple', 'Apricot', 'Grape', 'Grapefruit', 'Pineapple']);

  await user.keyboard('{ArrowDown}{ArrowDown}');
  const second = screen.getAllByRole('option')[1];
  assert.equal(box.getAttribute('aria-activedescendant'), second.id);   // focus stays in the input
  assert.equal(second.getAttribute('aria-selected'), 'true');
  await user.keyboard('{ArrowUp}{ArrowUp}');                // 1 -> 0 -> wraps to the last
  assert.equal(screen.getAllByRole('option')[4].getAttribute('aria-selected'), 'true');

  await user.keyboard('{ArrowDown}{Enter}');                // wraps to the first, selects it
  assert.deepEqual(picked, ['Apple']);
  assert.equal(box.value, 'Apple');
  assert.equal(screen.queryByRole('listbox'), null);

  await user.type(box, '{Backspace}');                     // reopen by typing...
  assert.notEqual(screen.queryByRole('listbox'), null);
  await user.keyboard('{Escape}');                          // ...and Escape closes
  assert.equal(screen.queryByRole('listbox'), null);
  assert.equal(box.getAttribute('aria-expanded'), 'false');
});

test('mouse: clicking an option selects it (mousedown does not blur the input first)', async () => {
  const user = userEvent.setup();
  const { search, calls } = fakeSearch();
  const picked = [];
  render(<Autocomplete search={search} delay={10} onSelect={(v) => picked.push(v)} />);
  await user.type(screen.getByRole('combobox'), 'ban');
  await waitFor(() => assert.equal(calls.length, 1));
  calls[0].resolve();
  await user.click(await screen.findByRole('option', { name: 'Banana' }));
  assert.deepEqual(picked, ['Banana']);
  assert.equal(document.activeElement, screen.getByRole('combobox'));
});

test('empty and error states', async () => {
  const user = userEvent.setup();
  let fail = false;
  const search = async (q) => { if (fail) throw new Error('500'); return []; };
  render(<Autocomplete search={search} delay={10} />);
  const box = screen.getByRole('combobox');
  await user.type(box, 'kiwi');
  assert.match((await screen.findByText(/No matches/)).textContent, /No matches for .kiwi./);
  fail = true;
  await user.type(box, 's');
  assert.equal((await screen.findByRole('alert')).textContent, 'Something went wrong. Keep typing to retry.');
});
