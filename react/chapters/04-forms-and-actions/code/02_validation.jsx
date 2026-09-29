// Validation: built-in HTML constraints + your own rules, shown accessibly and at the right moment.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

// 1. Built-in constraint validation: the browser blocks submit while a field is invalid.
function Native() {
  return (
    <form onSubmit={(e) => { e.preventDefault(); console.log('  native form submitted'); }}>
      <input aria-label="Nick" name="nick" required minLength={3} pattern="[a-z]+" />
      <button>Save</button>
    </form>
  );
}

// 2. Custom validation in React: one pure validate() function, errors shown after blur or submit.
function validate({ email, password, confirm }) {
  const errors = {};
  if (!email) errors.email = 'Email is required';
  else if (!/^\S+@\S+\.\S+$/.test(email)) errors.email = 'Enter a valid email';
  if (password.length < 8) errors.password = 'Use at least 8 characters';
  if (confirm !== password) errors.confirm = 'Passwords do not match';   // cross-field rule
  return errors;
}

function Field({ name, label, type = 'text', value, error, show, onChange, onBlur }) {
  const errorId = `${name}-error`;
  return (
    <div>
      <label htmlFor={name}>{label}</label>
      <input id={name} name={name} type={type} value={value} onChange={onChange} onBlur={onBlur}
        aria-invalid={show && !!error} aria-describedby={show && error ? errorId : undefined} />
      {show && error && <p id={errorId} role="alert">{error}</p>}
    </div>
  );
}

function SignupForm() {
  const [values, setValues] = useState({ email: '', password: '', confirm: '' });
  const [touched, setTouched] = useState({});          // which fields the user has left
  const [submitted, setSubmitted] = useState(false);
  const errors = validate(values);                      // derived — never stored (Chapter 3 §3.8)
  const show = (name) => submitted || touched[name];

  const handleChange = (e) => setValues({ ...values, [e.target.name]: e.target.value });
  const handleBlur = (e) => setTouched({ ...touched, [e.target.name]: true });
  function handleSubmit(e) {
    e.preventDefault();
    setSubmitted(true);
    const first = Object.keys(errors)[0];
    if (first) { document.getElementById(first).focus(); return; }   // move focus to the first problem
    console.log('  sending', values.email);
  }
  const props = (name) => ({ name, value: values[name], error: errors[name], show: show(name), onChange: handleChange, onBlur: handleBlur });
  return (
    <form noValidate onSubmit={handleSubmit}>
      <Field label="Email" type="email" {...props('email')} />
      <Field label="Password" type="password" {...props('password')} />
      <Field label="Confirm password" type="password" {...props('confirm')} />
      <button>Create account</button>
    </form>
  );
}

const user = userEvent.setup();
const alerts = () => screen.queryAllByRole('alert').map((a) => a.textContent);

render(<Native />);
await user.type(screen.getByLabelText('Nick'), 'Al');
await user.click(screen.getByRole('button', { name: 'Save' }));
const nick = screen.getByLabelText('Nick');
console.log('native: "Al" blocked | valid =', nick.validity.valid, '| patternMismatch =', nick.validity.patternMismatch);
await user.clear(nick); await user.type(nick, 'alan');
await user.click(screen.getByRole('button', { name: 'Save' }));
cleanup();

render(<SignupForm />);
await user.type(screen.getByLabelText('Email'), 'ada@');
console.log('while typing (not yet touched):', alerts());
await user.tab();                                          // blur -> email is now "touched"
console.log('after leaving Email           :', alerts());
await user.click(screen.getByRole('button', { name: 'Create account' }));
console.log('after submit                  :', alerts());
console.log('focused field                 :', document.activeElement.id, '| aria-invalid =', document.activeElement.getAttribute('aria-invalid'));
await user.type(screen.getByLabelText('Email'), 'example.com');
await user.type(screen.getByLabelText('Password'), 'correct-horse');
await user.type(screen.getByLabelText('Confirm password'), 'correct-horse');
console.log('after fixing                  :', alerts());
await user.click(screen.getByRole('button', { name: 'Create account' }));
