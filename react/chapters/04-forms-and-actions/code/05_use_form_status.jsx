// useFormStatus: a component INSIDE a <form> reads that form's submission status — no props needed.
import '../../../../tools_dom.mjs';
import { useFormStatus } from 'react-dom';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

globalThis.FormData = window.FormData; // use jsdom's FormData (Node's own can't read a <form>)
const wait = (ms) => new Promise((r) => setTimeout(r, ms));

// Reusable: works in any form, because it asks its parent <form> for the status.
function SubmitButton({ children }) {
  const { pending, data, method } = useFormStatus();
  return (
    <button disabled={pending}>
      {pending ? `Saving "${data.get('title')}" (${method})…` : children}
    </button>
  );
}

// Wrong place: the Hook is in the SAME component that renders the <form>, so it sees no parent form.
function WrongPlace() {
  const { pending } = useFormStatus();
  return (
    <form action={() => wait(30)}>
      <input name="title" aria-label="Title 2" defaultValue="x" />
      <button>{pending ? 'pending' : 'never pending'}</button>
    </form>
  );
}

async function saveNote(formData) { await wait(30); console.log('  saved:', formData.get('title')); }

function NoteForm() {
  return (
    <form action={saveNote}>
      <input name="title" aria-label="Title" />
      <SubmitButton>Save note</SubmitButton>
    </form>
  );
}

const user = userEvent.setup();
render(<><NoteForm /><WrongPlace /></>);
const [good, wrong] = screen.getAllByRole('button');
await user.type(screen.getByLabelText('Title'), 'Groceries');
await user.click(good);
await user.click(wrong);
console.log('during  :', JSON.stringify(good.textContent), 'disabled =', good.disabled, '|', JSON.stringify(wrong.textContent));
await act(() => wait(60));
console.log('after   :', JSON.stringify(good.textContent), 'disabled =', good.disabled);
