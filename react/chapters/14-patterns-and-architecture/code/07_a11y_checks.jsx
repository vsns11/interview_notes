// Accessibility checks with Testing Library role queries: the same UI written badly and well.
import '../../../../tools_dom.mjs';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { computeAccessibleName, computeAccessibleDescription } from 'dom-accessibility-api';
import { useId, useState } from 'react';

function BadCheckout({ onBuy }) {
  return (
    <div>
      <div className="title">Checkout</div>                       {/* looks like a heading, isn't one */}
      <img src="mug.png" />                                       {/* no alt */}
      <input placeholder="Email" />                               {/* placeholder is not a label */}
      <div className="btn" onClick={onBuy}>Buy now</div>          {/* not focusable, no role */}
      <button><svg aria-hidden="true" /></button>                 {/* icon-only: no name */}
    </div>
  );
}

function GoodCheckout({ onBuy }) {
  const emailId = useId(), errId = useId();
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);
  function submit(e) {
    e.preventDefault();
    const ok = e.currentTarget.elements.namedItem('email').value.includes('@');
    setError(ok ? '' : 'Enter a valid email');
    if (ok) { onBuy(); setDone(true); }
  }
  return (
    <form onSubmit={submit} noValidate>                           {/* our own messages instead of the browser bubbles */}
      <h1>Checkout</h1>
      <img src="mug.png" alt="Trail Mug, blue enamel" />
      <label htmlFor={emailId}>Email</label>
      <input id={emailId} name="email" type="email" aria-invalid={!!error} aria-describedby={error ? errId : undefined} />
      {error && <p id={errId}>{error}</p>}
      <button type="submit">Buy now</button>
      <button type="button" aria-label="Remove item"><svg aria-hidden="true" /></button>
      <p role="status">{done ? 'Order placed' : ''}</p>          {/* live region: screen readers announce changes */}
    </form>
  );
}

const user = userEvent.setup();
const find = (fn) => { try { return fn(); } catch { return null; } };

function audit(name, ui) {
  let bought = 0;
  render(ui(() => bought++));
  console.log(`--- ${name}`);
  console.log('heading "Checkout"      :', !!find(() => screen.getByRole('heading', { name: 'Checkout' })));
  console.log('image with alt text     :', find(() => screen.getByRole('img').getAttribute('alt')) ?? 'missing', );
  console.log('textbox named "Email"   :', !!find(() => screen.getByRole('textbox', { name: 'Email' })));
  console.log('button named "Buy now"  :', !!find(() => screen.getByRole('button', { name: 'Buy now' })));
  console.log('button names            :', JSON.stringify(screen.getAllByRole('button').map(b => computeAccessibleName(b))));
  return () => bought;
}

audit('bad', (onBuy) => <BadCheckout onBuy={onBuy} />);
const stops = [];
for (let i = 0; i < 3; i++) { await user.tab(); stops.push(document.activeElement.tagName.toLowerCase()); }
console.log('Tab key visits          :', stops.join(' -> '), '(the "Buy now" div is never reached)');
cleanup();

const bought = audit('good', (onBuy) => <GoodCheckout onBuy={onBuy} />);
await user.type(screen.getByRole('textbox', { name: 'Email' }), 'asha.example.com');
await user.keyboard('{Enter}');                                   // keyboard submit works for free with <form>
const email = screen.getByRole('textbox', { name: 'Email' });
console.log('invalid email -> aria-invalid:', email.getAttribute('aria-invalid'), '| description:', JSON.stringify(computeAccessibleDescription(email)));
await user.clear(email); await user.type(email, 'asha@example.com{Enter}');
console.log('valid email   -> bought:', bought(), '| aria-invalid:', email.getAttribute('aria-invalid'), '| status:', screen.getByRole('status').textContent);
