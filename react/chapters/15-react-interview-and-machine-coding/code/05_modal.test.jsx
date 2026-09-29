// Tests for modal.jsx: portal placement, initial focus, Tab trap, Escape, backdrop, focus restore.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DeleteProjectDemo from './modal.jsx';

afterEach(cleanup);
const focused = () => document.activeElement.textContent || document.activeElement.tagName.toLowerCase();

async function openModal() {
  const user = userEvent.setup();
  const { container } = render(<DeleteProjectDemo />);
  const trigger = screen.getByRole('button', { name: 'Delete project…' });
  await user.click(trigger);
  return { user, container, trigger, dialog: screen.getByRole('dialog', { name: 'Delete “Apollo”?' }) };
}

test('renders into document.body through a portal, labelled by its title', async () => {
  const { container, dialog } = await openModal();
  assert.equal(container.contains(dialog), false);        // not inside the component's own DOM
  assert.equal(document.body.contains(dialog), true);
  assert.equal(dialog.getAttribute('aria-modal'), 'true');
  assert.equal(document.body.style.overflow, 'hidden');    // background scroll locked
});

test('focus moves in, and Tab / Shift+Tab stay trapped inside', async () => {
  const { user } = await openModal();
  assert.equal(document.activeElement, screen.getByRole('textbox'));   // first focusable element
  await user.tab();
  assert.equal(focused(), 'Cancel');
  await user.tab();                                        // Delete is disabled, so Cancel is last:
  assert.equal(document.activeElement, screen.getByRole('textbox'));   // wraps to the first
  await user.tab({ shift: true });                         // Shift+Tab on the first wraps to the last
  assert.equal(focused(), 'Cancel');

  await user.type(screen.getByRole('textbox'), 'Apollo');  // now Delete is enabled and is the last one
  await user.tab(); await user.tab();
  assert.equal(focused(), 'Delete');
  await user.tab();
  assert.equal(document.activeElement, screen.getByRole('textbox'));
});

test('Escape closes and focus returns to the button that opened it', async () => {
  const { user, trigger } = await openModal();
  await user.keyboard('{Escape}');
  assert.equal(screen.queryByRole('dialog'), null);
  assert.equal(document.activeElement, trigger);
  assert.equal(document.body.style.overflow, '');          // scroll lock removed
});

test('clicking the backdrop closes; clicking inside does not', async () => {
  const { user } = await openModal();
  await user.click(screen.getByText(/You can’t undo this/));
  assert.notEqual(screen.queryByRole('dialog'), null);
  assert.equal(document.activeElement, screen.getByRole('dialog'));   // the click focused the dialog box
  await user.tab({ shift: true });                         // Shift+Tab from there must not leave the dialog
  assert.equal(focused(), 'Cancel');
  await user.click(screen.getByRole('dialog').parentElement);   // the backdrop
  assert.equal(screen.queryByRole('dialog'), null);
});

test('the confirm flow: Delete stays disabled until the name is typed', async () => {
  const { user, trigger } = await openModal();
  const del = screen.getByRole('button', { name: 'Delete' });
  assert.equal(del.disabled, true);
  await user.type(screen.getByRole('textbox'), 'Apollo');
  await user.click(del);
  assert.equal(screen.getByText('Project deleted.').tagName, 'P');
  assert.equal(document.activeElement, trigger);
});
