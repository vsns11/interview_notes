// Tests for pagination.jsx: the pure range function (table of cases) and the UI.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { render, screen, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import OrdersTable, { getPageRange } from './pagination.jsx';

afterEach(cleanup);

test('getPageRange: first/last always shown, dots for gaps, constant length', () => {
  const cases = [
    [1, 5], [3, 7],                // few pages: no dots
    [1, 20], [4, 20], [5, 20], [10, 20], [16, 20], [17, 20], [20, 20],
  ];
  for (const [page, total] of cases) console.log(`    page ${String(page).padStart(2)} of ${total}:`, getPageRange(page, total).join(' '));
  assert.deepEqual(getPageRange(10, 20), [1, '…', 9, 10, 11, '…', 20]);
  assert.deepEqual(getPageRange(10, 20, 2), [1, '…', 8, 9, 10, 11, 12, '…', 20]);
  for (let p = 1; p <= 20; p++) assert.equal(getPageRange(p, 20).length, 7);   // nothing jumps
});

const orders = Array.from({ length: 47 }, (_, i) => ({ id: i + 1, customer: `Customer ${i + 1}`, total: 10 + i }));
const firstCell = () => within(screen.getAllByRole('row')[1]).getAllByRole('cell')[0].textContent;
const pages = () => [...screen.getByRole('navigation', { name: 'Pagination' }).querySelectorAll('li')].map((li) => li.textContent).join(' ');

test('Next / Previous / page buttons, aria-current and disabled ends', async () => {
  const user = userEvent.setup();
  render(<OrdersTable orders={orders} />);
  assert.equal(screen.getByRole('status').textContent, '1–10 of 47');
  assert.equal(screen.getByRole('button', { name: 'Previous page' }).disabled, true);
  assert.equal(screen.getByRole('button', { name: 'Page 1' }).getAttribute('aria-current'), 'page');

  await user.click(screen.getByRole('button', { name: 'Next page' }));
  assert.equal(firstCell(), '11');
  await user.click(screen.getByRole('button', { name: 'Page 5' }));
  assert.equal(screen.getByRole('status').textContent, '41–47 of 47');   // short last page
  assert.equal(screen.getByRole('button', { name: 'Next page' }).disabled, true);
  assert.equal(screen.getAllByRole('row').length, 1 + 7);                 // header + 7 rows
  console.log('    pager on page 5 of 5:', pages());
});

test('changing rows-per-page goes back to page 1', async () => {
  const user = userEvent.setup();
  render(<OrdersTable orders={orders} />);
  await user.click(screen.getByRole('button', { name: 'Page 3' }));
  await user.selectOptions(screen.getByRole('combobox', { name: 'Rows per page' }), '5');
  assert.equal(screen.getByRole('status').textContent, '1–5 of 47');
  console.log('    pager with 5 per page:', pages());
});

test('clamps the page when the data shrinks under it', async () => {
  const user = userEvent.setup();
  const { rerender } = render(<OrdersTable orders={orders} />);
  await user.click(screen.getByRole('button', { name: 'Page 5' }));
  rerender(<OrdersTable orders={orders.slice(0, 12)} />);                // e.g. a filter was applied
  assert.equal(screen.getByRole('status').textContent, '11–12 of 12');   // page 2, not an empty page 5
  rerender(<OrdersTable orders={[]} />);
  assert.equal(screen.getByRole('status').textContent, 'No orders');
  assert.equal(screen.queryByRole('navigation'), null);
});
