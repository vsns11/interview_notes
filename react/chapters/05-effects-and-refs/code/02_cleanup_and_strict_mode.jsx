// Cleanup undoes what setup did. StrictMode (development only) runs setup -> cleanup -> setup once to prove it.
import '../../../../tools_dom.mjs';
import { StrictMode, useState, useEffect } from 'react';
import { render, screen, fireEvent, act, cleanup } from '@testing-library/react';

// A fake external system: a chat server we connect to.
let open = 0;
function createConnection(roomId) {
  return {
    connect() { open++; console.log(`  connect    ${roomId} (open: ${open})`); },
    disconnect() { open--; console.log(`  disconnect ${roomId} (open: ${open})`); },
  };
}

function ChatRoom({ roomId }) {
  useEffect(() => {
    const connection = createConnection(roomId);   // setup
    connection.connect();
    return () => connection.disconnect();           // cleanup
  }, [roomId]);
  return <h2>Welcome to {roomId}</h2>;
}

function LeakyChatRoom({ roomId }) {
  useEffect(() => {
    createConnection(roomId).connect();             // no cleanup!
  }, [roomId]);
  return <h2>Leaky {roomId}</h2>;
}

function App({ Room }) {
  const [roomId, setRoomId] = useState('general');
  const [show, setShow] = useState(true);
  return (
    <>
      <button onClick={() => setRoomId('travel')}>travel</button>
      <button onClick={() => setShow(false)}>close</button>
      {show && <Room roomId={roomId} />}
    </>
  );
}

for (const [label, Room] of [['WITH cleanup', ChatRoom], ['WITHOUT cleanup', LeakyChatRoom]]) {
  open = 0;
  console.log(`=== ${label}, inside <StrictMode> (development)`);
  console.log('mount:');
  render(<StrictMode><App Room={Room} /></StrictMode>);
  console.log('switch room:');
  fireEvent.click(screen.getByText('travel'));
  console.log('close:');
  fireEvent.click(screen.getByText('close'));
  console.log(`connections still open: ${open}\n`);
  act(() => cleanup());
}

// Timers and listeners follow the same shape.
function Clock() {
  const [ticks, setTicks] = useState(0);
  useEffect(() => {
    const id = setInterval(() => setTicks((t) => t + 1), 10);
    const onKey = (e) => console.log('  key pressed:', e.key);
    window.addEventListener('keydown', onKey);
    return () => { clearInterval(id); window.removeEventListener('keydown', onKey); };
  }, []);
  return <p>ticks: {ticks}</p>;
}
const clock = render(<StrictMode><Clock /></StrictMode>);
await act(() => new Promise((r) => setTimeout(r, 55)));
fireEvent.keyDown(window, { key: 'Escape' });
console.log('clock shows', screen.getByText(/ticks/).textContent, '(one interval, one listener — the StrictMode duplicate was cleaned up)');
clock.unmount();
fireEvent.keyDown(window, { key: 'Enter' });
console.log('after unmount the listener is gone (no "key pressed: Enter" above)');
