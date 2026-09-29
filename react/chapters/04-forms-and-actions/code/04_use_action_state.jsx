// useActionState: the result of the last submission (errors, success) + a pending flag, in one Hook.
import '../../../../tools_dom.mjs';
import { useActionState, startTransition } from 'react';
import { render, screen, act, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

globalThis.FormData = window.FormData; // use jsdom's FormData (Node's own can't read a <form>)
const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const taken = new Set(['ada']);

// Pretend server call: validates, may fail, returns a result object.
async function registerUser(prevState, formData) {
  const username = String(formData.get('username') ?? '').trim();
  const age = Number(formData.get('age'));
  await wait(30);
  const errors = {};
  if (username.length < 3) errors.username = 'At least 3 characters';
  else if (taken.has(username)) errors.username = `"${username}" is taken`;
  if (!(age >= 13)) errors.age = 'You must be 13 or older';
  if (Object.keys(errors).length) return { errors, values: { username, age: formData.get('age') }, attempts: prevState.attempts + 1 };
  taken.add(username);
  return { message: `Welcome, ${username}!`, errors: {}, values: { username: '', age: '' }, attempts: prevState.attempts + 1 };
}

const initialState = { errors: {}, values: { username: '', age: '' }, attempts: 0 };

function Register() {
  const [state, formAction, isPending] = useActionState(registerUser, initialState);
  return (
    <form action={formAction}>
      {/* defaultValue from the last result: after the automatic reset the fields show what the user typed */}
      <input name="username" aria-label="Username" defaultValue={state.values.username} />
      {state.errors.username && <p role="alert">{state.errors.username}</p>}
      <input name="age" aria-label="Age" inputMode="numeric" defaultValue={state.values.age} />
      {state.errors.age && <p role="alert">{state.errors.age}</p>}
      <button disabled={isPending}>{isPending ? 'Registering…' : 'Register'}</button>
      {state.message && <p role="status">{state.message}</p>}
      <small>attempts: {state.attempts}</small>
    </form>
  );
}

// Calling the dispatch function yourself: wrap it in startTransition. Calls are queued in order.
function Stock() {
  const [count, dispatch, isPending] = useActionState(async (prev, delta) => { await wait(20); return prev + delta; }, 0);
  return (
    <p>
      <button onClick={() => startTransition(() => dispatch(+1))}>+1</button>
      <output>{count}</output>{isPending && ' (saving…)'}
    </p>
  );
}

const user = userEvent.setup();
const alerts = () => screen.queryAllByRole('alert').map((a) => a.textContent);
const field = (l) => screen.getByLabelText(l).value;

render(<Register />);
await user.type(screen.getByLabelText('Username'), 'ada');
await user.type(screen.getByLabelText('Age'), '9');
await user.click(screen.getByRole('button'));
console.log('pending   : button =', JSON.stringify(screen.getByRole('button').textContent), '| disabled =', screen.getByRole('button').disabled);
await act(() => wait(60));
console.log('attempt 1 :', alerts(), '| fields kept:', field('Username'), field('Age'));
await user.clear(screen.getByLabelText('Username')); await user.type(screen.getByLabelText('Username'), 'grace');
await user.clear(screen.getByLabelText('Age')); await user.type(screen.getByLabelText('Age'), '36');
await user.click(screen.getByRole('button'));
await act(() => wait(60));
console.log('attempt 2 :', alerts(), '|', screen.getByRole('status').textContent, '| fields:', JSON.stringify(field('Username')), JSON.stringify(field('Age')), '|', screen.getByText(/attempts/).textContent);
cleanup();

render(<Stock />);
const plus = screen.getByRole('button', { name: '+1' });
await user.click(plus); await user.click(plus); await user.click(plus);
console.log('\nthree quick clicks -> shows', screen.getByRole('paragraph').textContent);
await act(() => wait(100));
console.log('after the queue ran  -> shows', screen.getByRole('paragraph').textContent);
