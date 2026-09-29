// A function passed to <form action> receives FormData, runs in a Transition, and resets uncontrolled fields.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

globalThis.FormData = window.FormData; // use jsdom's FormData (Node has its own, which can't read a <form>)

const wait = (ms) => new Promise((r) => setTimeout(r, ms));

function Newsletter() {
  const [log, setLog] = useState([]);
  async function subscribe(formData) {            // no event, no preventDefault
    const email = formData.get('email');
    const topics = formData.getAll('topic');       // several checkboxes with the same name
    await wait(50);                               // pretend network call
    setLog((l) => [...l, `${email} -> ${topics.join('+') || 'none'}`]);
  }
  function saveDraft(formData) {                  // a second button can run a different action
    setLog((l) => [...l, `draft saved for ${formData.get('email')}`]);
  }
  return (
    <form action={subscribe}>
      <input name="email" type="email" aria-label="Email" />
      <label><input type="checkbox" name="topic" value="react" /> React</label>
      <label><input type="checkbox" name="topic" value="css" /> CSS</label>
      <button type="submit">Subscribe</button>
      <button formAction={saveDraft}>Save draft</button>
      <ul>{log.map((line) => <li key={line}>{line}</li>)}</ul>
    </form>
  );
}

const user = userEvent.setup();
render(<Newsletter />);
await user.type(screen.getByLabelText('Email'), 'ada@example.com');
await user.click(screen.getByLabelText('React'));
await user.click(screen.getByLabelText('CSS'));
await user.click(screen.getByRole('button', { name: 'Subscribe' }));
console.log('right after click, email field =', JSON.stringify(screen.getByLabelText('Email').value));
await act(() => wait(80));
console.log('after the action finished:');
console.log('  log          =', screen.getAllByRole('listitem').map((li) => li.textContent));
console.log('  email field  =', JSON.stringify(screen.getByLabelText('Email').value), '(auto-reset)');
console.log('  React ticked =', screen.getByLabelText('React').checked);
await user.clear(screen.getByLabelText('Email')); // user-event doesn't notice form.reset(), so clear first
await user.type(screen.getByLabelText('Email'), 'bo@example.com');
await user.click(screen.getByRole('button', { name: 'Save draft' }));
await act(() => wait(10));
console.log('  log          =', screen.getAllByRole('listitem').map((li) => li.textContent));
