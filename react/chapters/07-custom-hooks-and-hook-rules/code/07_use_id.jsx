// useId: unique, stable IDs for accessibility attributes (label/for, aria-describedby).
import '../../../../tools_dom.mjs';
import { useId } from 'react';
import { renderToString } from 'react-dom/server';
import { hydrateRoot } from 'react-dom/client';
import { act } from 'react';
import { render, screen } from '@testing-library/react';

function PasswordField({ label }) {
  const id = useId();                       // one ID per component instance
  return (
    <div>
      <label htmlFor={`${id}-input`}>{label}</label>
      <input id={`${id}-input`} type="password" aria-describedby={`${id}-hint`} />
      <p id={`${id}-hint`}>At least 12 characters.</p>
    </div>
  );
}

function SignupForm() {
  return (<form><PasswordField label="Password" /><PasswordField label="Repeat password" /></form>);
}

const { container } = render(<SignupForm />);
for (const input of container.querySelectorAll('input')) {
  console.log(`input id=${input.id}  aria-describedby=${input.getAttribute('aria-describedby')}`);
}
// Because the IDs are wired correctly, accessible queries work:
console.log('label lookup finds the 2nd input:', screen.getByLabelText('Repeat password') === container.querySelectorAll('input')[1]);
console.log('its description:', screen.getByLabelText('Repeat password').getAttribute('aria-describedby'));

// Server rendering + hydration: the browser REUSES the server's IDs, so markup matches.
const html = renderToString(<SignupForm />);
console.log('server ids   :', [...html.matchAll(/ id="([^"]+)"/g)].map(m => m[1]).join(', '));
const box = document.createElement('div'); box.innerHTML = html; document.body.append(box);
await act(async () => { hydrateRoot(box, <SignupForm />); });
console.log('after hydrate:', [...box.querySelectorAll('[id]')].map(n => n.id).join(', '));
// Two React roots on one page? Give each a prefix so their IDs never collide.
const html2 = renderToString(<PasswordField label="PIN" />, { identifierPrefix: 'widget-' });
console.log('with identifierPrefix:', html2.match(/ id="([^"]+)"/)[1]);
