// Tests for wizard.jsx: per-step validation, error wiring, focus management, Back keeps data, submit states.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SignupWizard, { STEPS } from './wizard.jsx';

afterEach(cleanup);
const heading = () => screen.getByRole('heading', { level: 2 }).textContent;
const next = () => screen.getByRole('button', { name: 'Next' });

test('validators are pure functions: easy to unit-test on their own', () => {
  assert.deepEqual(STEPS[0].validate({ email: 'ada@', password: 'short' }), {
    email: 'Enter an email like name@example.com', password: 'Use at least 8 characters',
  });
  assert.deepEqual(STEPS[0].validate({ email: 'ada@example.com', password: 'longenough' }), {});
});

test('Next on an empty step shows errors, marks fields invalid and focuses the first one', async () => {
  const user = userEvent.setup();
  render(<SignupWizard onSubmit={async () => {}} />);
  await user.click(next());
  assert.equal(heading(), 'Step 1 of 3: Account');                   // did not advance
  const email = screen.getByLabelText('Email');
  assert.equal(email.getAttribute('aria-invalid'), 'true');
  assert.equal(screen.getByText('Email is required').id, email.getAttribute('aria-describedby'));
  assert.equal(document.activeElement, email);

  await user.type(email, 'ada');
  assert.equal(screen.queryByText('Email is required'), null);        // error clears while fixing
  await user.click(next());
  assert.equal(screen.getByText('Enter an email like name@example.com').tagName, 'P');
});

test('full journey: steps, Back keeps data, review, submitting state, success', async () => {
  const user = userEvent.setup();
  let finish;
  const submitted = [];
  const onSubmit = (data) => { submitted.push(data); return new Promise((r) => { finish = r; }); };
  render(<SignupWizard onSubmit={onSubmit} />);

  await user.type(screen.getByLabelText('Email'), 'ada@example.com');
  await user.type(screen.getByLabelText('Password'), 'correct horse');
  await user.click(next());
  assert.equal(heading(), 'Step 2 of 3: Profile');
  assert.equal(document.activeElement, screen.getByRole('heading', { level: 2 }));   // focus moved to the step
  assert.equal(screen.getByText('Profile').getAttribute('aria-current'), 'step');

  await user.click(screen.getByRole('button', { name: 'Back' }));
  assert.equal(screen.getByLabelText('Email').value, 'ada@example.com');   // nothing lost
  await user.click(next());

  await user.click(next());                                           // Profile is empty -> errors
  assert.equal(document.activeElement, screen.getByLabelText('Full name'));
  await user.type(screen.getByLabelText('Full name'), 'Ada Lovelace');
  await user.selectOptions(screen.getByLabelText('Plan'), 'pro');
  await user.click(next());

  assert.equal(heading(), 'Step 3 of 3: Review');
  console.log('    review:', [...document.querySelectorAll('dt')].map((dt) => `${dt.textContent}=${dt.nextSibling.textContent}`).join(', '));
  await user.click(screen.getByRole('button', { name: 'Create account' }));
  assert.equal(screen.getByRole('button', { name: 'Creating account…' }).disabled, true);   // no double submit
  assert.deepEqual(submitted, [{ email: 'ada@example.com', password: 'correct horse', name: 'Ada Lovelace', plan: 'pro' }]);
  finish();
  assert.equal((await screen.findByRole('heading', { name: 'Welcome, Ada Lovelace!' })).tagName, 'H2');
});

test('a failed submit shows an alert and lets the user retry', async () => {
  const user = userEvent.setup();
  let attempts = 0;
  const onSubmit = async () => { attempts++; if (attempts === 1) throw new Error('503'); };
  render(<SignupWizard onSubmit={onSubmit} initialStep={2} initialData={{ email: 'a@b.co', password: '12345678', name: 'Al', plan: 'free' }} />);
  await user.click(screen.getByRole('button', { name: 'Create account' }));
  assert.equal((await screen.findByRole('alert')).textContent, 'Sign-up failed. Please try again.');
  await user.click(screen.getByRole('button', { name: 'Create account' }));
  assert.equal((await screen.findByRole('heading', { name: 'Welcome, Al!' })).tagName, 'H2');
});
