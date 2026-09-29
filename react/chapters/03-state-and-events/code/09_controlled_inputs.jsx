// Controlled inputs: React state is the single source of truth for what the input shows.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

function Profile() {
  const [name, setName] = useState('');
  const [bio, setBio] = useState('');
  const [plan, setPlan] = useState('free');
  const [agree, setAgree] = useState(false);
  return (
    <form onSubmit={(e) => { e.preventDefault(); console.log('  submit ->', JSON.stringify({ name, bio, plan, agree })); }}>
      <label>Username <input value={name} onChange={(e) => setName(e.target.value.toLowerCase().replace(/\s/g, ''))} /></label>
      <label>Bio <textarea value={bio} onChange={(e) => setBio(e.target.value.slice(0, 20))} /></label>
      <label>Plan
        <select value={plan} onChange={(e) => setPlan(e.target.value)}>
          <option value="free">Free</option><option value="pro">Pro</option>
        </select>
      </label>
      <label><input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} /> I agree</label>
      <p>{name ? `@${name}` : 'pick a username'} · {bio.length}/20</p>
      <button disabled={!agree || !name}>Create</button>
    </form>
  );
}

const user = userEvent.setup();
render(<Profile />);
await user.type(screen.getByLabelText('Username'), 'Ada Love Lace');
await user.type(screen.getByLabelText('Bio'), 'First programmer, 1843 notes');
await user.selectOptions(screen.getByLabelText('Plan'), 'pro');
console.log('username field shows:', screen.getByLabelText('Username').value);
console.log('bio field shows     :', screen.getByLabelText('Bio').value);
console.log('status line         :', screen.getByRole('paragraph').textContent);
console.log('Create disabled?    :', screen.getByRole('button').disabled);
await user.click(screen.getByLabelText('I agree'));
await user.click(screen.getByRole('button', { name: 'Create' }));
cleanup();

// value without onChange = read-only input (React warns).
const warnings = []; const orig = console.error;
console.error = (...a) => warnings.push(String(a[0]).split('\n')[0]);
render(<input aria-label="frozen" value="fixed" />);
await user.type(screen.getByLabelText('frozen'), 'xyz');
console.error = orig;
console.log('\nvalue without onChange -> field shows:', screen.getByLabelText('frozen').value);
console.log('warning:', warnings[0]?.slice(0, 110) + '...');
