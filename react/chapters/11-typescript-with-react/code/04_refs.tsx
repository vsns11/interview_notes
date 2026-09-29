// Typing refs: DOM refs, value refs, ref as a prop (React 19), and useImperativeHandle.
import '../../../../tools_dom.mjs';
import { useRef, useEffect, useImperativeHandle, useState } from 'react';
import type { ComponentProps, Ref, RefObject } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// 1. A DOM ref: pass the element type and start with null. Type: RefObject<HTMLInputElement | null>.
function SearchBox() {
  const inputRef = useRef<HTMLInputElement>(null);
  useEffect(() => {
    inputRef.current?.focus();                  // ?. because current is null until React attaches it
  }, []);
  return <input ref={inputRef} aria-label="search" />;
}

// 2. A value ref (not attached to JSX): any type, you update .current yourself.
function Stopwatch() {
  const intervalId = useRef<ReturnType<typeof setInterval> | null>(null);  // read/written only in handlers
  const [running, setRunning] = useState(false);
  function toggle() {
    if (intervalId.current !== null) { clearInterval(intervalId.current); intervalId.current = null; setRunning(false); }
    else { intervalId.current = setInterval(() => {}, 1000); setRunning(true); }
  }
  return <button onClick={toggle}>{running ? 'stop' : 'start'} timer</button>;
}

// 3. React 19: `ref` is an ordinary prop for function components - no forwardRef needed.
//    ComponentProps<'input'> already includes `ref?: Ref<HTMLInputElement>`.
function TextField({ label, ...inputProps }: { label: string } & ComponentProps<'input'>) {
  return <label>{label} <input {...inputProps} /></label>;   // ref travels inside inputProps
}

// 4. Exposing a custom handle instead of the DOM node.
export type VideoHandle = { play: () => void; pause: () => void };
function Video({ ref, src }: { ref?: Ref<VideoHandle>; src: string }) {
  const [state, setState] = useState('paused');
  useImperativeHandle(ref, () => ({ play: () => setState('playing'), pause: () => setState('paused') }), []);
  return <p>video {src}: {state}</p>;
}

function App() {
  const nameRef = useRef<HTMLInputElement>(null);
  const videoRef = useRef<VideoHandle>(null);
  const logFocus = (r: RefObject<HTMLInputElement | null>) => console.log('nameRef.current is', r.current?.tagName, 'with placeholder', r.current?.placeholder);
  return (
    <>
      <SearchBox />
      <Stopwatch />
      <TextField label="Name" ref={nameRef} placeholder="Ada" />
      <Video ref={videoRef} src="intro.mp4" />
      <button onClick={() => { logFocus(nameRef); videoRef.current?.play(); }}>go</button>
    </>
  );
}

render(<App />);
console.log('focused after mount:', document.activeElement?.getAttribute('aria-label'));
fireEvent.click(screen.getByText(/start/));
console.log('stopwatch:', screen.getByText(/stop|start/).textContent);
fireEvent.click(screen.getByText('stop timer'));
fireEvent.click(screen.getByText('go'));
console.log(screen.getByText(/video/).textContent);
