// useTransition: mark an update as NON-urgent so the old UI stays interactive meanwhile.
import '../../../../tools_dom.mjs';
import { useState, useTransition } from 'react';
import { render, screen, act } from '@testing-library/react';

const log = [];
function SlowPosts() {                              // pretend this tab is expensive to render
  const start = performance.now(); while (performance.now() - start < 30) { /* busy 30 ms */ }
  return <p>500 posts</p>;
}
const TABS = { about: () => <p>About me</p>, posts: SlowPosts };

function TabApp() {
  const [tab, setTab] = useState('about');
  const [isPending, startTransition] = useTransition();
  log.push(`render tab=${tab} isPending=${isPending}`);
  const Tab = TABS[tab];
  return (
    <div style={{ opacity: isPending ? 0.6 : 1 }}>
      <button onClick={() => startTransition(() => setTab('posts'))}>Posts</button>
      <Tab />
    </div>
  );
}

render(<TabApp />);
log.length = 0;
await act(async () => screen.getByText('Posts').click());
console.log('--- click "Posts" (update inside startTransition) ---');
console.log(log.join('\n'));

// React 19: the function passed to startTransition may be ASYNC (an "Action").
// isPending stays true until the whole async function finishes.
const saveToServer = (name) => new Promise(r => setTimeout(() => r(name.toUpperCase()), 50));
const seen = [];
function RenameForm() {
  const [name, setName] = useState('ada');
  const [isPending, startTransition] = useTransition();
  seen.push(`${isPending ? 'Saving…' : 'Idle   '} name=${name}`);
  function handleSave() {
    startTransition(async () => {
      const saved = await saveToServer('grace');
      startTransition(() => setName(saved));      // updates AFTER an await need their own startTransition
    });
  }
  return <button onClick={handleSave}>Save</button>;
}
render(<RenameForm />);
await act(async () => { screen.getByText('Save').click(); await new Promise(r => setTimeout(r, 80)); });
console.log('--- async action ---');
console.log(seen.join('\n'));
