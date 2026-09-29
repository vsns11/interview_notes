// Async UI: findBy*, waitFor, waitForElementToBeRemoved.
import '../../../../tools_dom.mjs';
import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { useState, useEffect } from 'react';
import { render, screen, cleanup, waitFor, waitForElementToBeRemoved } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

// A fake API with a delay, so the component really is asynchronous.
const api = {
  loadOrders: () => new Promise(r => setTimeout(() => r([{ id: 1, total: 20 }, { id: 2, total: 35 }]), 50)),
  save: () => new Promise(r => setTimeout(r, 30)),
};

function Orders() {
  const [orders, setOrders] = useState(null);
  const [saved, setSaved] = useState(false);
  useEffect(() => { api.loadOrders().then(setOrders); }, []);
  if (!orders) return <p>Loading orders…</p>;
  return (
    <>
      <ul>{orders.map(o => <li key={o.id}>Order {o.id}: {o.total} EUR</li>)}</ul>
      <button onClick={async () => { await api.save(); setSaved(true); }}>Save</button>
      {saved && <p role="status">Saved!</p>}
    </>
  );
}

afterEach(cleanup);

test('right after render only the loading state exists', () => {
  render(<Orders />);
  assert.ok(screen.getByText('Loading orders…'));
  assert.equal(screen.queryByText(/Order 1/), null);
});

test('findBy waits (retries up to 1000 ms by default) until the element appears', async () => {
  render(<Orders />);
  const items = await screen.findAllByRole('listitem');
  assert.deepEqual(items.map(li => li.textContent), ['Order 1: 20 EUR', 'Order 2: 35 EUR']);
  assert.equal(screen.queryByText('Loading orders…'), null);
});

test('waitForElementToBeRemoved waits for the spinner to go away', async () => {
  render(<Orders />);
  await waitForElementToBeRemoved(() => screen.queryByText('Loading orders…'));
  assert.equal(screen.getAllByRole('listitem').length, 2);
});

test('waitFor retries any assertion until it passes', async () => {
  const user = userEvent.setup();
  render(<Orders />);
  await user.click(await screen.findByRole('button', { name: 'Save' }));
  await waitFor(() => assert.equal(screen.getByRole('status').textContent, 'Saved!'));
});

test('findBy gives up after its timeout with a clear error', async () => {
  render(<Orders />);
  const t0 = Date.now();
  await assert.rejects(screen.findByText('Order 99', {}, { timeout: 200 }), /Unable to find an element with the text: Order 99/);
  console.log(`    rejected after ~${Math.round((Date.now() - t0) / 100) * 100} ms`);
});
