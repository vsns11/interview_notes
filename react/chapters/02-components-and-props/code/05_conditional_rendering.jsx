// Four ways to render conditionally, and the `0 &&` trap.
import '../../../../tools_dom.mjs';
import { render } from '@testing-library/react';

// 1. if / early return (statements are allowed ABOVE the JSX)
function Item({ name, isPacked }) {
  if (isPacked) return <li className="item">{name} ✅</li>;
  return <li className="item">{name}</li>;
}
// 2. ternary inside JSX
function Item2({ name, isPacked }) {
  return <li className="item">{isPacked ? <del>{name}</del> : name}</li>;
}
// 3. && : render something or nothing
function Item3({ name, isPacked }) {
  return <li className="item">{name} {isPacked && '✅'}</li>;
}
// 4. store JSX in a variable
function Item4({ name, isPacked }) {
  let content = name;
  if (isPacked) content = <strong>{name} (packed)</strong>;
  return <li className="item">{content}</li>;
}
// returning null renders nothing at all
function Hidden({ show }) { return show ? <p>visible</p> : null; }

const html = (el) => render(el).container.innerHTML;
console.log('if/return :', html(<ul><Item name="Space suit" isPacked /><Item name="Photo" isPacked={false} /></ul>));
console.log('ternary   :', html(<ul><Item2 name="Helmet" isPacked /></ul>));
console.log('&&        :', html(<ul><Item3 name="Helmet" isPacked /><Item3 name="Map" isPacked={false} /></ul>));
console.log('variable  :', html(<ul><Item4 name="Helmet" isPacked /></ul>));
console.log('null      :', JSON.stringify(html(<Hidden show={false} />)));

// The trap: a number on the left of && is rendered when it is 0.
function Inbox({ messages }) {
  return <p>{messages.length && `You have ${messages.length} messages`}</p>;
}
function InboxFixed({ messages }) {
  return <p>{messages.length > 0 && `You have ${messages.length} messages`}</p>;
}
console.log('0 && bug  :', html(<Inbox messages={[]} />));
console.log('fixed     :', html(<InboxFixed messages={[]} />));
