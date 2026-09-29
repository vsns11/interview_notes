// useDeferredValue: show the latest input immediately, let a slow list "lag behind".
import '../../../../tools_dom.mjs';
import { useState, useDeferredValue, memo } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

const log = [];
const WORDS = ['react', 'redux', 'router', 'reducer', 'ref', 'render', 'query', 'suspense'];

// memo is essential: it lets React skip SlowList when only the urgent `text` changed.
const SlowList = memo(function SlowList({ query }) {
  log.push(`  SlowList renders with query="${query}"`);
  const start = performance.now(); while (performance.now() - start < 20) { /* expensive */ }
  return <ul>{WORDS.filter(w => w.startsWith(query)).map(w => <li key={w}>{w}</li>)}</ul>;
});

function Search() {
  const [text, setText] = useState('');
  const deferred = useDeferredValue(text);           // lags behind `text` during typing
  const isStale = text !== deferred;
  log.push(`render text="${text}" deferred="${deferred}"${isStale ? '  (list is stale)' : ''}`);
  return (
    <>
      <input aria-label="q" value={text} onChange={e => setText(e.target.value)} />
      <div style={{ opacity: isStale ? 0.5 : 1 }}><SlowList query={deferred} /></div>
    </>
  );
}

render(<Search />);
log.length = 0;
await act(async () => { fireEvent.change(screen.getByLabelText('q'), { target: { value: 're' } }); });
console.log(log.join('\n'));
console.log('list now:', [...document.querySelectorAll('li')].map(li => li.textContent).join(', '));

// React 19: an initial value lets the FIRST render be cheap too.
log.length = 0;
function Preview({ text }) {
  const shown = useDeferredValue(text, '');           // first render uses '' then catches up
  log.push(`Preview render shown="${shown}"`);
  return <p>{shown}</p>;
}
await act(async () => { render(<Preview text="hello" />); });
console.log(log.join('\n'));
