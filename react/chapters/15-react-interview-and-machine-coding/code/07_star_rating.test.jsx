// Tests for star-rating.jsx: click, hover preview, keyboard (native radio arrows), clear, read-only.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { useState } from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import StarRating from './star-rating.jsx';

afterEach(cleanup);
// jsdom lacks CSS.escape, which user-event uses to find the other radios of a group. Minimal stand-in:
window.CSS ??= { escape: (s) => s.replace(/[^\w-]/g, (c) => `\\${c}`) };
const lit = () => document.querySelectorAll('fieldset .star.on').length;

function Harness({ initial = 0, log = [] }) {
  const [v, setV] = useState(initial);
  return <StarRating label="Food" value={v} onChange={(n) => { log.push(n); setV(n); }} />;
}

test('clicking a star selects it; the group is a labelled set of radios', async () => {
  const user = userEvent.setup();
  const log = [];
  render(<Harness log={log} />);
  assert.equal(screen.getByRole('group', { name: 'Food' }).tagName, 'FIELDSET');
  assert.equal(screen.getAllByRole('radio').length, 5);
  await user.click(screen.getByRole('radio', { name: '4 stars' }));
  assert.equal(screen.getByRole('radio', { name: '4 stars' }).checked, true);
  assert.equal(lit(), 4);
  assert.equal(screen.getByText('4 of 5').tagName, 'OUTPUT');
  assert.deepEqual(log, [4]);
});

test('hover previews without changing the value; leaving restores it', async () => {
  const user = userEvent.setup();
  render(<Harness initial={4} />);
  const labels = document.querySelectorAll('fieldset label');
  await user.hover(labels[1]);                               // pointer over star 2
  assert.equal(lit(), 2);
  assert.equal(screen.getByText('4 of 5').tagName, 'OUTPUT');   // value unchanged
  await user.unhover(screen.getByRole('group'));
  assert.equal(lit(), 4);
});

test('keyboard: arrow keys move between stars like any radio group', async () => {
  const user = userEvent.setup();
  render(<Harness initial={2} />);
  await user.tab();                                          // focus lands on the CHECKED radio
  assert.equal(document.activeElement, screen.getByRole('radio', { name: '2 stars' }));
  await user.keyboard('{ArrowRight}');
  assert.equal(screen.getByRole('radio', { name: '3 stars' }).checked, true);
  await user.keyboard('{ArrowLeft}{ArrowLeft}');
  assert.equal(lit(), 1);
});

test('Clear resets to 0; read-only mode is a single image with a text alternative', async () => {
  const user = userEvent.setup();
  render(<><Harness initial={3} /><StarRating readOnly label="Average" value={4} /></>);
  await user.click(screen.getByRole('button', { name: 'Clear' }));
  assert.equal(screen.getByText('Not rated').tagName, 'OUTPUT');
  assert.equal(screen.getByRole('img').getAttribute('aria-label'), 'Average: 4 out of 5');
});
