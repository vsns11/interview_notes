// Trigger -> render -> commit, made visible.
// "render" = React calls your function. "commit" = React touches the DOM (only where output changed).
import '../../../../tools_dom.mjs';
import { useState, useEffect, useLayoutEffect } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

const log = (...a) => console.log(...a);

function Clock({ label }) {
  log(`  render  <Clock label="${label}">`);               // runs during the RENDER phase
  useLayoutEffect(() => log('  commit  React applied changes, then ran layout effects'));
  useEffect(() => log('  after   passive effects run (after paint)'));
  return <p>{label}</p>;
}

function App() {
  const [label, setLabel] = useState('hello');
  const [clicks, setClicks] = useState(0);
  return (
    <>
      <Clock label={label} />
      <button onClick={() => setLabel('world')}>change label</button>
      <button onClick={() => setClicks(c => c + 1)}>count {clicks}</button>
      <button onClick={() => setLabel(label)}>set same label</button>
    </>
  );
}

// Count real DOM mutations with a MutationObserver - this is what "commit" costs.
let mutations = 0;
const mo = new MutationObserver(list => { mutations += list.length; });
mo.observe(document.body, { subtree: true, childList: true, characterData: true, attributes: true });
const flushMO = () => { mutations += mo.takeRecords().length; const m = mutations; mutations = 0; return m; };

log('TRIGGER 1: initial render (createRoot().render)');
render(<App />);
log(`  DOM mutations: ${flushMO() > 0 ? 'many (whole tree inserted)' : 0}`);

log('\nTRIGGER 2: state change that changes Clock output');
fireEvent.click(screen.getByText('change label'));
log(`  DOM mutations: ${flushMO()}`);

log('\nTRIGGER 3: state change elsewhere - Clock re-renders, but its output is identical');
fireEvent.click(screen.getByText(/^count/));
log(`  DOM mutations: ${flushMO()}  (only the button text changed; <p> untouched)`);

log('\nTRIGGER 4: setState with the SAME value (Object.is equal)');
await act(async () => fireEvent.click(screen.getByText('set same label')));
log(`  DOM mutations: ${flushMO()}  (React bailed out: nothing rendered)`);
mo.disconnect();
