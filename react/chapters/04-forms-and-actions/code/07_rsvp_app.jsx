// Drives the RSVP form like a user: validation errors, success, a server error, and the optimistic guest list.
import '../../../../tools_dom.mjs';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Rsvp, { createApi } from './rsvp.jsx';

globalThis.FormData = window.FormData; // use jsdom's FormData (Node's own can't read a <form>)
const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const user = userEvent.setup();
const guests = () => screen.getAllByRole('listitem').map((li) => li.textContent);
const alerts = () => screen.queryAllByRole('alert').map((a) => a.textContent);
async function fill(name, email) {
  for (const [label, v] of [['Name', name], ['Email', email]]) {
    await user.clear(screen.getByLabelText(label));
    if (v) await user.type(screen.getByLabelText(label), v);
  }
}

render(<Rsvp saveRsvp={createApi()} initialGuests={[{ id: 0, name: 'Host', email: 'host@x.io', guests: 1, diet: 'none' }]} />);

await fill('A', 'ada@');
await user.selectOptions(screen.getByLabelText('Guests (including you)'), '2');
await user.click(screen.getByLabelText('vegan'));
await user.click(screen.getByRole('button', { name: 'Send RSVP' }));
await act(() => wait(10));
console.log('1 invalid      :', alerts(), '| kept:', screen.getByLabelText('Name').value, screen.getByLabelText('Guests (including you)').value, screen.getByLabelText('vegan').checked);

await fill('Ada', 'ada@example.com');
await user.click(screen.getByRole('button', { name: 'Send RSVP' }));
await act(() => wait(5));
console.log('2 sending      :', guests(), '| button:', screen.getByRole('button').textContent);
await act(() => wait(80));
console.log('2 saved        :', guests(), '|', screen.getByRole('status').textContent, '| heading:', screen.getAllByRole('heading')[1].textContent);

await fill('Ada again', 'ada@example.com');
await user.click(screen.getByRole('button', { name: 'Send RSVP' }));
await act(() => wait(80));
console.log('3 server error :', alerts(), '| list:', guests());
