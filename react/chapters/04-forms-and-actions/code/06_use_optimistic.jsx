// useOptimistic: show the expected result immediately; React swaps in the real state (or rolls back) when the Action ends.
import '../../../../tools_dom.mjs';
import { useState, useOptimistic, startTransition } from 'react';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

globalThis.FormData = window.FormData; // use jsdom's FormData (Node's own can't read a <form>)
const wait = (ms) => new Promise((r) => setTimeout(r, ms));

async function sendToServer(text) {                  // slow, and rejects some messages
  await wait(50);
  if (text.includes('spam')) throw new Error('Message rejected');
  return { id: crypto.randomUUID(), text };
}

function Chat() {
  const [messages, setMessages] = useState([{ id: 'm1', text: 'Hi!' }]);
  const [error, setError] = useState(null);
  // optimisticMessages === messages when no Action is running
  const [optimisticMessages, addOptimistic] = useOptimistic(
    messages,
    (current, text) => [...current, { id: 'temp-' + text, text, sending: true }],   // pure reducer
  );

  async function sendAction(formData) {               // runs inside a Transition (form action)
    const text = formData.get('text');
    setError(null);
    addOptimistic(text);                              // 1. appears instantly, marked "sending"
    try {
      const saved = await sendToServer(text);         // 2. wait for the server
      startTransition(() => setMessages((ms) => [...ms, saved]));  // 3. real state (after await -> wrap)
    } catch (e) {
      setError(e.message);                            // optimistic item disappears by itself
    }
  }

  return (
    <>
      <ul>
        {optimisticMessages.map((m) => <li key={m.id}>{m.text}{m.sending && ' (sending…)'}</li>)}
      </ul>
      {error && <p role="alert">{error}</p>}
      <form action={sendAction}>
        <input name="text" aria-label="Message" />
        <button>Send</button>
      </form>
    </>
  );
}

const user = userEvent.setup();
const items = () => screen.getAllByRole('listitem').map((li) => li.textContent);
render(<Chat />);

await user.type(screen.getByLabelText('Message'), 'See you at 6');
await user.click(screen.getByRole('button'));
console.log('instantly      :', items());
await act(() => wait(80));
console.log('server said OK :', items());

await user.clear(screen.getByLabelText('Message'));
await user.type(screen.getByLabelText('Message'), 'buy spam now');
await user.click(screen.getByRole('button'));
console.log('instantly      :', items());
await act(() => wait(80));
console.log('server said NO :', items(), '|', screen.getByRole('alert').textContent);
