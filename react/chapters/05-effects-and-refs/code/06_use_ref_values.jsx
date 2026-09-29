// useRef: a box ({ current }) that survives re-renders and does NOT trigger one when changed.
import '../../../../tools_dom.mjs';
import { useState, useRef } from 'react';
import { render, screen, fireEvent, act, cleanup } from '@testing-library/react';

// 1. Ref vs state: changing a ref never re-renders; the screen shows a stale value until something else renders.
function Compare() {
  const [stateClicks, setStateClicks] = useState(0);
  const refClicks = useRef(0);
  return (
    <>
      <button onClick={() => { refClicks.current++; console.log('  ref is now', refClicks.current); }}>ref +1</button>
      <button onClick={() => setStateClicks(stateClicks + 1)}>state +1</button>
      <p>state: {stateClicks} | ref (as of last render): {refClicks.current}</p>
    </>
  );
}

// 2. Stopwatch: the interval ID is needed by a later event but never shown -> ref.
function Stopwatch() {
  const [startTime, setStartTime] = useState(null);
  const [now, setNow] = useState(null);
  const intervalRef = useRef(null);
  function handleStart() {
    setStartTime(Date.now()); setNow(Date.now());
    clearInterval(intervalRef.current);
    intervalRef.current = setInterval(() => setNow(Date.now()), 10);
  }
  function handleStop() { clearInterval(intervalRef.current); }
  const secondsPassed = startTime != null && now != null ? (now - startTime) / 1000 : 0;
  return (
    <>
      <output>{secondsPassed.toFixed(3)} s</output>
      <button onClick={handleStart}>Start</button>
      <button onClick={handleStop}>Stop</button>
    </>
  );
}

// 3. Lazy-initialise an expensive object once (the only OK "write during render" pattern).
let created = 0;
class VideoPlayerEngine { constructor() { created++; } }
function Player() {
  const engine = useRef(null);
  if (engine.current === null) engine.current = new VideoPlayerEngine();   // runs on the first render only
  const [, force] = useState(0);
  return <button onClick={() => force((n) => n + 1)}>re-render</button>;
}

render(<Compare />);
fireEvent.click(screen.getByText('ref +1')); fireEvent.click(screen.getByText('ref +1'));
console.log('after 2 ref clicks  :', screen.getByRole('paragraph').textContent);
fireEvent.click(screen.getByText('state +1'));
console.log('after 1 state click :', screen.getByRole('paragraph').textContent);
cleanup();

render(<Stopwatch />);
fireEvent.click(screen.getByText('Start'));
await act(() => new Promise((r) => setTimeout(r, 80)));
fireEvent.click(screen.getByText('Stop'));
const shown = screen.getByRole('status').textContent;
await act(() => new Promise((r) => setTimeout(r, 50)));
console.log('stopwatch ran        :', parseFloat(shown) >= 0.07, '| still frozen 50 ms after Stop:', screen.getByRole('status').textContent === shown);
cleanup();

render(<Player />);
for (let i = 0; i < 3; i++) fireEvent.click(screen.getByRole('button'));
console.log('engines created over 4 renders:', created);
