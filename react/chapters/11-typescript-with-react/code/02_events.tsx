// Typing event handlers.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import type { ChangeEvent, SubmitEvent, MouseEvent, KeyboardEvent, ChangeEventHandler } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

function SignupForm({ onSubmit }: { onSubmit: (data: { email: string; plan: string }) => void }) {
  const [email, setEmail] = useState('');
  const [plan, setPlan] = useState('free');

  // 1. Inline handlers need no annotations: TypeScript infers e from the onChange prop's type.
  // 2. Handlers declared separately need the event type: <what happened><on which element>.
  function handleEmail(e: ChangeEvent<HTMLInputElement>) {
    setEmail(e.target.value);                       // e.target is an HTMLInputElement -> .value is a string
  }
  const handlePlan: ChangeEventHandler<HTMLSelectElement> = e => setPlan(e.target.value); // type the whole function
  function handleSubmit(e: SubmitEvent<HTMLFormElement>) {   // older code: FormEvent (now deprecated)
    e.preventDefault();
    console.log('submitted by the button:', e.submitter?.textContent);
    onSubmit({ email, plan });
  }
  function handleKey(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Escape') setEmail('');
  }
  function handleHelp(e: MouseEvent<HTMLButtonElement>) {
    console.log(`help clicked at button "${e.currentTarget.textContent}" (shift: ${e.shiftKey})`);
  }

  return (
    <form onSubmit={handleSubmit}>
      <input aria-label="email" value={email} onChange={handleEmail} onKeyDown={handleKey} />
      <select aria-label="plan" value={plan} onChange={handlePlan}>
        <option value="free">Free</option><option value="pro">Pro</option>
      </select>
      <button type="button" onClick={handleHelp}>Help</button>
      <button type="submit" onClick={e => console.log('inline handler, e is', e.type)}>Sign up</button>
    </form>
  );
}

render(<SignupForm onSubmit={data => console.log('submitted', JSON.stringify(data))} />);
fireEvent.change(screen.getByLabelText('email'), { target: { value: 'ada@example.com' } });
fireEvent.change(screen.getByLabelText('plan'), { target: { value: 'pro' } });
fireEvent.click(screen.getByText('Help'), { shiftKey: true });
fireEvent.click(screen.getByText('Sign up'));
fireEvent.keyDown(screen.getByLabelText('email'), { key: 'Escape' });
console.log('after Escape, email =', JSON.stringify((screen.getByLabelText('email') as HTMLInputElement).value));
