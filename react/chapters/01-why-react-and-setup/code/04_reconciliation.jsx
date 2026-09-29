// React compares the new element tree with the previous one and touches ONLY what changed.
import '../../../../tools_dom.mjs';
import { createRoot } from 'react-dom/client';
import { act } from 'react';

function Clock({ time }) {
  return (
    <div className="clock">
      <h1>Hello, world!</h1>
      <p>It is {time}.</p>
    </div>
  );
}

const host = document.getElementById('root');
const root = createRoot(host);
await act(() => root.render(<Clock time="10:00:00" />));
const h1Before = host.querySelector('h1');
const pBefore = host.querySelector('p');

const changes = [];
const observer = new MutationObserver((records) =>
  records.forEach((r) => changes.push(`${r.type} on <${(r.target.parentNode ?? r.target).nodeName.toLowerCase()}>: "${r.oldValue}" -> "${r.target.textContent}"`)));
observer.observe(host, { subtree: true, childList: true, characterData: true, characterDataOldValue: true });

await act(() => root.render(<Clock time="10:00:01" />)); // re-render with a new prop
await new Promise((r) => setTimeout(r, 0));                  // let the observer report
console.log('DOM changes made by React:');
changes.forEach((c) => console.log('  ' + c));
console.log('same <h1> node kept?', host.querySelector('h1') === h1Before);
console.log('same <p> node kept? ', host.querySelector('p') === pBefore);
console.log('HTML now:', host.innerHTML);
