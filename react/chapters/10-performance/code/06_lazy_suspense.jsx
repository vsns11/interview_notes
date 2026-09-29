// Code splitting: lazy() + <Suspense>. The chart's code loads only when the user opens the chart tab.
import '../../../../tools_dom.mjs';
import { lazy, Suspense, useState, Component } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

const delay = ms => new Promise(r => setTimeout(r, ms));        // pretend network latency

// 1. Declare lazy components at MODULE level (never inside a component).
const HeavyChart = lazy(() => delay(100).then(() => import('./heavy_chart.jsx')));
// lazy() needs a default export; for a named export, map it to { default }.
const Legend = lazy(() => import('./heavy_chart.jsx').then(m => ({ default: m.Legend })));
// A chunk that fails to load (e.g. a deploy replaced the file, or the user went offline).
const Broken = lazy(() => delay(10).then(() => { throw new Error('Failed to fetch dynamically imported module'); }));

class ChunkErrorBoundary extends Component {                   // error boundaries are still classes
  state = { error: null };
  static getDerivedStateFromError(error) { return { error }; }
  render() { return this.state.error ? <p role="alert">Could not load: {this.state.error.message}</p> : this.props.children; }
}

function Dashboard() {
  const [tab, setTab] = useState('summary');
  return (
    <main>
      <button onClick={() => setTab('chart')}>chart tab</button>
      <button onClick={() => setTab('broken')}>broken tab</button>
      {tab === 'summary' && <p>summary text</p>}
      <Suspense fallback={<p>Loading chart...</p>}>
        {tab === 'chart' && <><HeavyChart points={[1, 2, 3]} /><Legend /></>}
      </Suspense>
      <ChunkErrorBoundary>
        <Suspense fallback={<p>Loading...</p>}>{tab === 'broken' && <Broken />}</Suspense>
      </ChunkErrorBoundary>
    </main>
  );
}

const origError = console.error; console.error = () => {};     // hide React's dev log for the thrown error
const show = when => console.log(`${when.padEnd(24)} -> ` +       // print what the user sees (minus the buttons)
  [...screen.getByRole('main').children].filter(e => e.tagName !== 'BUTTON').map(e => e.textContent).join(' | '));
render(<Dashboard />);
show('first render');
fireEvent.click(screen.getByText('chart tab'));
show('clicked chart tab');
await screen.findByText(/chart with/);                          // wait for the chunk + render
show('after the chunk loaded');
fireEvent.click(screen.getByText('broken tab'));
await screen.findByRole('alert');
show('broken tab');
console.error = origError;
