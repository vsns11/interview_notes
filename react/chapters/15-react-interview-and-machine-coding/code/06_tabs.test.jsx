// Tests for tabs.jsx: roles/ARIA wiring, arrow keys with wrap, Home/End, disabled tabs, Tab key order.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Tabs from './tabs.jsx';

afterEach(cleanup);
const TABS = [
  { label: 'Overview', content: <p>Plan: Pro, 3 seats</p> },
  { label: 'Billing', content: <p>Next invoice: 1 Oct</p> },
  { label: 'Audit log', content: <p>No events</p>, disabled: true },
  { label: 'Members', content: <label>Invite <input /></label> },
];
const selectedTab = () => screen.getByRole('tab', { selected: true }).textContent;

test('ARIA wiring: tablist > tab[aria-selected], tabpanel labelled by its tab', () => {
  render(<Tabs tabs={TABS} label="Account" />);
  assert.equal(screen.getByRole('tablist', { name: 'Account' }).children.length, 4);
  const tab = screen.getByRole('tab', { name: 'Overview' });
  const panel = screen.getByRole('tabpanel');               // hidden panels are not in the a11y tree
  assert.equal(tab.getAttribute('aria-controls'), panel.id);
  assert.equal(screen.getByRole('tabpanel', { name: 'Overview' }), panel);
  assert.deepEqual(screen.getAllByRole('tab').map((t) => t.tabIndex), [0, -1, -1, -1]);
});

test('arrow keys move selection AND focus, wrap, and skip disabled tabs', async () => {
  const user = userEvent.setup();
  const changes = [];
  render(<Tabs tabs={TABS} label="Account" onChange={(i) => changes.push(i)} />);
  await user.click(screen.getByRole('tab', { name: 'Overview' }));
  await user.keyboard('{ArrowRight}');
  assert.equal(selectedTab(), 'Billing');
  assert.equal(document.activeElement.textContent, 'Billing');
  await user.keyboard('{ArrowRight}');                       // Audit log is disabled -> Members
  assert.equal(selectedTab(), 'Members');
  await user.keyboard('{ArrowRight}');                       // wraps to the start
  assert.equal(selectedTab(), 'Overview');
  await user.keyboard('{ArrowLeft}');                        // wraps to the end
  assert.equal(selectedTab(), 'Members');
  await user.keyboard('{Home}');
  assert.equal(selectedTab(), 'Overview');
  await user.keyboard('{End}');
  assert.equal(selectedTab(), 'Members');
  assert.deepEqual(changes, [0, 1, 3, 0, 3, 0, 3]);
  assert.equal(screen.getByRole('tabpanel').textContent, 'Invite ');
});

test('Tab key: one stop for the whole tablist, then into the panel', async () => {
  const user = userEvent.setup();
  render(<><button>before</button><Tabs tabs={TABS} label="Account" defaultIndex={1} /></>);
  await user.tab();                                          // "before"
  await user.tab();
  assert.equal(document.activeElement.textContent, 'Billing');   // the SELECTED tab, not the first
  await user.tab();
  assert.equal(document.activeElement.getAttribute('role'), 'tabpanel');
});

test('panels stay mounted, so typed text survives switching tabs', async () => {
  const user = userEvent.setup();
  render(<Tabs tabs={TABS} label="Account" defaultIndex={3} />);
  await user.type(screen.getByRole('textbox', { name: 'Invite' }), 'ada@example.com');
  await user.click(screen.getByRole('tab', { name: 'Overview' }));
  await user.click(screen.getByRole('tab', { name: 'Members' }));
  assert.equal(screen.getByRole('textbox', { name: 'Invite' }).value, 'ada@example.com');
});
