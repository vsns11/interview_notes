// A custom Hook that takes a callback: useInterval. useEffectEvent (stable since React 19.2)
// lets the Effect call the LATEST callback without restarting the timer on every render.
import '../../../../tools_dom.mjs';
import { useState, useEffect, useEffectEvent } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

let timersStarted = 0;
export function useInterval(callback, delayMs) {
  const onTick = useEffectEvent(callback);        // always calls the newest callback
  useEffect(() => {
    if (delayMs === null) return;                  // null = paused
    timersStarted++;
    const id = setInterval(() => onTick(), delayMs);
    return () => clearInterval(id);
  }, [delayMs]);                                   // onTick is deliberately NOT a dependency
}

// The tempting-but-wrong version: an empty dependency array freezes the FIRST callback.
function useIntervalStale(callback, delayMs) {
  useEffect(() => {
    const id = setInterval(() => callback(), delayMs);
    return () => clearInterval(id);
  }, []);                                          // the linter would warn: missing 'callback', 'delayMs'
}

function makeStopwatch(useIntervalImpl, stepsSeen) {
  return function Stopwatch() {
    const [step, setStep] = useState(1);
    const [running, setRunning] = useState(true);
    useIntervalImpl(() => stepsSeen.add(step), running ? 20 : null);
    return (
      <div>
        <button onClick={() => setStep(s => s + 1)}>step {step}</button>
        <button onClick={() => setRunning(r => !r)}>{running ? 'pause' : 'resume'}</button>
      </div>
    );
  };
}

const wait = (ms) => act(() => new Promise(r => setTimeout(r, ms)));
for (const [label, impl] of [['useInterval (useEffectEvent)', useInterval], ['useIntervalStale ([] deps)  ', useIntervalStale]]) {
  const stepsSeen = new Set();
  const Stopwatch = makeStopwatch(impl, stepsSeen);
  const { unmount } = render(<Stopwatch />);
  await wait(70);
  fireEvent.click(screen.getByText(/step/));      // step 2
  fireEvent.click(screen.getByText(/step/));      // step 3
  await wait(70);
  console.log(`${label}: ticks used step values ${[...stepsSeen].join(', ')}`);
  unmount();
}

// Pausing and resuming changes delayMs, which (correctly) restarts the timer.
timersStarted = 0;
const Stopwatch = makeStopwatch(useInterval, new Set());
const { unmount } = render(<Stopwatch />);
fireEvent.click(screen.getByText(/step/));
fireEvent.click(screen.getByText('pause'));
fireEvent.click(screen.getByText('resume'));
console.log('intervals started:', timersStarted, '(mount + resume; changing step never restarted it)');
unmount();
