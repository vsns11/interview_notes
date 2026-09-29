// Event handlers: passing vs calling, arguments, custom event props, propagation, preventDefault.
import '../../../../tools_dom.mjs';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

// 1. Pass the function; do not call it.
function Right() { return <button onClick={() => console.log('  Right: clicked')}>right</button>; }
function Wrong() {
  const handleClick = () => console.log('  Wrong: ran during RENDER, not on click');
  return <button onClick={handleClick()}>wrong</button>; // calls it now; onClick receives undefined
}
console.log('rendering <Wrong/>:'); render(<Wrong />);
console.log('clicking wrong:');     fireEvent.click(screen.getByText('wrong'));
render(<Right />); console.log('clicking right:'); fireEvent.click(screen.getByText('right'));
cleanup();

// 2. Passing arguments + custom event props (onX / handleX naming).
function Toolbar({ onPlay, onUpload }) {
  return (
    <div>
      <button onClick={() => onPlay('Kiki')}>Play movie</button>
      <button onClick={onUpload}>Upload image</button>
    </div>
  );
}
function App() {
  function handlePlay(title) { console.log(`  Playing ${title}`); }
  return <Toolbar onPlay={handlePlay} onUpload={(e) => console.log('  Uploading... event type =', e.type)} />;
}
render(<App />);
fireEvent.click(screen.getByText('Play movie'));
fireEvent.click(screen.getByText('Upload image'));
cleanup();

// 3. Propagation: events bubble from child to parent; stopPropagation halts it.
function Panel({ stop }) {
  return (
    <div onClick={() => console.log('    parent <div> onClick')}>
      <button onClick={(e) => { if (stop) e.stopPropagation(); console.log('    child <button> onClick'); }}>
        {stop ? 'stop' : 'bubble'}
      </button>
    </div>
  );
}
render(<><Panel stop={false} /><Panel stop /></>);
console.log('click "bubble":'); fireEvent.click(screen.getByText('bubble'));
console.log('click "stop":');   fireEvent.click(screen.getByText('stop'));
cleanup();

// 4. preventDefault: stop the browser's default action (a form submit reloads the page).
function Signup() {
  return (
    <form onSubmit={(e) => { e.preventDefault(); console.log('  submitted without reload; defaultPrevented =', e.defaultPrevented); }}>
      <input aria-label="email" />
      <button>Send</button>
    </form>
  );
}
render(<Signup />);
fireEvent.click(screen.getByText('Send'));
