// user-event simulates what a real user does (focus, key presses, clicks...), not just one DOM event.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { useState } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

function SignupForm({ onSubmit }) {
  const [form, setForm] = useState({ name: '', plan: 'free', terms: false });
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));
  return (
    <form onSubmit={e => { e.preventDefault(); onSubmit(form); }}>
      <label>Name <input value={form.name} onChange={e => set('name', e.target.value)} /></label>
      <label>Plan
        <select value={form.plan} onChange={e => set('plan', e.target.value)}>
          <option value="free">Free</option><option value="pro">Pro</option>
        </select>
      </label>
      <label><input type="checkbox" checked={form.terms} onChange={e => set('terms', e.target.checked)} /> I accept the terms</label>
      <button type="submit" disabled={!form.terms || !form.name}>Create account</button>
    </form>
  );
}

afterEach(cleanup);

test('fireEvent vs userEvent: which DOM events does "typing" produce?', async () => {
  render(<input aria-label="box" />);
  const box = screen.getByLabelText('box');
  const seen = [];
  for (const type of ['focus', 'keydown', 'keypress', 'input', 'keyup', 'click'])
    box.addEventListener(type, () => seen.push(type));

  fireEvent.change(box, { target: { value: 'hi' } });
  console.log('    fireEvent.change   ->', seen.join(', ') || '(no listened-to events; only a synthetic change)');
  seen.length = 0; box.value = '';

  const user = userEvent.setup();
  await user.type(box, 'hi');
  console.log('    user.type(box, hi) ->', seen.join(', '));
  assert.equal(box.value, 'hi');
});

test('fills in and submits the whole form like a user', async () => {
  const user = userEvent.setup();                        // one `user` per test, before rendering is fine
  const submitted = [];
  render(<SignupForm onSubmit={data => submitted.push(data)} />);

  const button = screen.getByRole('button', { name: 'Create account' });
  assert.equal(button.disabled, true);

  await user.type(screen.getByRole('textbox', { name: 'Name' }), 'Ada');
  await user.selectOptions(screen.getByRole('combobox', { name: 'Plan' }), 'pro');
  await user.click(screen.getByRole('checkbox', { name: 'I accept the terms' }));  // clicking the label text works too
  assert.equal(button.disabled, false);

  await user.click(button);
  assert.deepEqual(submitted, [{ name: 'Ada', plan: 'pro', terms: true }]);
});

test('keyboard only: Tab moves focus in DOM order, Enter submits', async () => {
  const user = userEvent.setup();
  const submitted = [];
  render(<SignupForm onSubmit={data => submitted.push(data)} />);

  await user.tab();
  assert.equal(document.activeElement, screen.getByRole('textbox', { name: 'Name' }));
  await user.keyboard('Grace');
  await user.tab(); await user.tab();                    // skip the select, reach the checkbox
  assert.equal(document.activeElement, screen.getByRole('checkbox'));
  await user.keyboard(' ');                              // Space toggles a checkbox
  await user.click(screen.getByRole('textbox', { name: 'Name' }));
  await user.keyboard('{Enter}');                        // Enter in a text field submits the form
  assert.deepEqual(submitted, [{ name: 'Grace', plan: 'free', terms: true }]);
});

test('clicking a disabled button does nothing', async () => {
  const user = userEvent.setup();
  let clicks = 0;
  render(<button disabled onClick={() => clicks++}>Pay</button>);
  await user.click(screen.getByRole('button', { name: 'Pay' }));
  assert.equal(clicks, 0);
});
