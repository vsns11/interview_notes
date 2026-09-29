// tools_run: skip  (component module: used by 07_rsvp_app.jsx and the browser entry rsvp-main.jsx)
import { useState, useActionState, useOptimistic, startTransition } from 'react';
import { useFormStatus } from 'react-dom';

const wait = (ms) => new Promise((r) => setTimeout(r, ms));

// Validation lives in one pure function the "server" can also run.
export function validateRsvp({ name, email, guests }) {
  const errors = {};
  if (name.length < 2) errors.name = 'Please enter your name';
  if (!/^\S+@\S+\.\S+$/.test(email)) errors.email = 'Enter an email like ada@example.com';
  if (!(guests >= 1 && guests <= 4)) errors.guests = 'Choose 1 to 4 guests';
  return errors;
}

// Fake API with latency. Rejects duplicate emails so we can see a server-side error.
export function createApi(latency = 40) {
  const saved = new Set();
  return async function saveRsvp(rsvp) {
    await wait(latency);
    if (saved.has(rsvp.email)) throw new Error(`${rsvp.email} has already replied`);
    saved.add(rsvp.email);
    return { ...rsvp, id: saved.size };
  };
}

function SubmitButton() {
  const { pending } = useFormStatus();                 // reads the parent <form>
  return <button className="primary" disabled={pending}>{pending ? 'Sending…' : 'Send RSVP'}</button>;
}

function FieldError({ id, message }) {
  return message ? <p className="error" id={id} role="alert">{message}</p> : null;
}

export const emptyResult = { errors: {}, values: { name: '', email: '', guests: '1', diet: 'none' }, message: '' };

export default function Rsvp({ saveRsvp, initialGuests = [], initialResult = emptyResult }) {
  const [guests, setGuests] = useState(initialGuests);
  const [optimisticGuests, addOptimistic] = useOptimistic(guests, (list, g) => [...list, { ...g, id: 'pending', pending: true }]);

  async function rsvpAction(prev, formData) {
    const values = Object.fromEntries(formData);        // { name, email, guests, diet }
    const rsvp = { name: values.name.trim(), email: values.email.trim(), guests: Number(values.guests), diet: values.diet };
    const errors = validateRsvp(rsvp);
    if (Object.keys(errors).length) return { errors, values, message: '' };   // keep what they typed
    addOptimistic(rsvp);                                 // show in the list right away
    try {
      const saved = await saveRsvp(rsvp);
      startTransition(() => setGuests((gs) => [...gs, saved]));
      return { ...emptyResult, message: `Thanks ${saved.name}, see you there!` };
    } catch (e) {
      return { errors: { form: e.message }, values, message: '' };
    }
  }
  const [result, formAction] = useActionState(rsvpAction, initialResult);
  const { errors, values } = result;
  const total = optimisticGuests.reduce((n, g) => n + g.guests, 0);   // derived

  return (
    <div className="card">
      <h1>Team dinner — RSVP</h1>
      <form action={formAction} noValidate>
        <label htmlFor="name">Name</label>
        <input id="name" name="name" defaultValue={values.name} aria-invalid={!!errors.name} aria-describedby="name-err" />
        <FieldError id="name-err" message={errors.name} />

        <label htmlFor="email">Email</label>
        <input id="email" name="email" type="email" defaultValue={values.email} aria-invalid={!!errors.email} aria-describedby="email-err" />
        <FieldError id="email-err" message={errors.email} />

        <label htmlFor="guests">Guests (including you)</label>
        {/* key: React ignores a CHANGED defaultValue on <select>, so remount it with the new default */}
        <select key={values.guests} id="guests" name="guests" defaultValue={values.guests}>
          {[1, 2, 3, 4].map((n) => <option key={n}>{n}</option>)}
        </select>

        <fieldset>
          <legend>Dietary needs</legend>
          {['none', 'vegetarian', 'vegan'].map((d) => (
            <label key={d} className="inline"><input type="radio" name="diet" value={d} defaultChecked={values.diet === d} /> {d}</label>
          ))}
        </fieldset>

        <FieldError id="form-err" message={errors.form} />
        {result.message && <p className="ok" role="status">{result.message}</p>}
        <SubmitButton />
      </form>

      <h2>Coming ({total})</h2>
      <ul className="guests">
        {optimisticGuests.map((g) => (
          <li key={g.id} className={g.pending ? 'pending' : ''}>
            {g.name} · {g.guests} {g.guests === 1 ? 'seat' : 'seats'}{g.diet !== 'none' && ` · ${g.diet}`}{g.pending && ' (saving…)'}
          </li>
        ))}
      </ul>
    </div>
  );
}
