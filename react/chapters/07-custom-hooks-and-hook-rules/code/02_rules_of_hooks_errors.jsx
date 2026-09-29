// What real React does when the number of Hook calls changes between renders.
import '../../../../tools_dom.mjs';
import { useState, useEffect } from 'react';
import { render } from '@testing-library/react';

const origError = console.error;
console.error = (msg, ...rest) => {           // print only the first line of React's dev warnings
  const text = String(msg).split('\n')[0].replace('%s', rest[0]);
  if (text.includes('order of Hooks')) console.log('  [dev warning]', text);
};

// BAD: a Hook after an early return
function Greeting({ user }) {
  const [lang] = useState('en');
  if (!user) return <p>Please sign in ({lang})</p>;
  const [greeting] = useState('Hello');     // skipped when user is missing
  return <p>{greeting}, {user}</p>;
}

// BAD: a Hook inside a condition
function Tracker({ tracking }) {
  const [clicks] = useState(0);
  if (tracking) useEffect(() => {});
  return <p>clicks: {clicks}</p>;
}

function tryRender(label, ui, nextUi) {
  const { rerender, unmount } = render(ui);
  try { rerender(nextUi); console.log(label, 'OK?!'); }
  catch (e) { console.log(label, '->', e.message.split('\n')[0]); }
  finally { try { unmount(); } catch {} }
}

tryRender('Greeting: signed-out then signed-in', <Greeting />, <Greeting user="Ada" />);
tryRender('Greeting: signed-in then signed-out', <Greeting user="Ada" />, <Greeting />);
tryRender('Tracker : off then on              ', <Tracker tracking={false} />, <Tracker tracking />);

// GOOD: call every Hook unconditionally at the top, put the condition inside.
function GreetingFixed({ user }) {
  const [greeting] = useState('Hello');
  if (!user) return <p>Please sign in</p>;
  return <p>{greeting}, {user}</p>;
}
function TrackerFixed({ tracking }) {
  const [clicks] = useState(0);
  useEffect(() => { if (!tracking) return; /* start tracking */ }, [tracking]);
  return <p>clicks: {clicks}</p>;
}
const a = render(<GreetingFixed />); a.rerender(<GreetingFixed user="Ada" />);
console.log('GreetingFixed     ->', a.container.textContent);
const b = render(<TrackerFixed tracking={false} />); b.rerender(<TrackerFixed tracking />);
console.log('TrackerFixed      ->', b.container.textContent);
console.error = origError;
