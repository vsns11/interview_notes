// Higher-order components: a function that takes a component and returns a new component.
import '../../../../tools_dom.mjs';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createContext, useContext, useState } from 'react';

const UserContext = createContext(null);

// HOC 1: guard — render the wrapped component only for signed-in users.
function withAuth(Component) {
  function WithAuth(props) {
    const user = useContext(UserContext);
    if (!user) return <p>Please sign in.</p>;
    return <Component {...props} user={user} />;       // pass all props through + inject one
  }
  WithAuth.displayName = `withAuth(${Component.displayName || Component.name})`; // nice name in DevTools
  return WithAuth;
}

// HOC 2: cross-cutting behaviour — log every render with the props received.
function withRenderLog(Component) {
  const name = Component.displayName || Component.name;
  function WithRenderLog(props) {
    console.log(`  [log] ${name} rendered with`, JSON.stringify(props));
    return <Component {...props} />;
  }
  WithRenderLog.displayName = `withRenderLog(${name})`;
  return WithRenderLog;
}

function OrderHistory({ user, limit }) { return <p>{user.name}'s last {limit} orders</p>; }

// Apply ONCE, at module level. HOCs compose from the inside out.
const ProtectedHistory = withRenderLog(withAuth(OrderHistory));
console.log('displayName:', ProtectedHistory.displayName);

render(<UserContext value={null}><ProtectedHistory limit={3} /></UserContext>);
console.log('signed out :', document.body.textContent);
cleanup();
render(<UserContext value={{ name: 'Asha' }}><ProtectedHistory limit={3} /></UserContext>);
console.log('signed in  :', document.body.textContent);
cleanup();

// PITFALL: creating the HOC during render makes a NEW component type every render -> state is lost.
function Counter() { const [n, setN] = useState(0); return <button onClick={() => setN(n + 1)}>count {n}</button>; }
const withNothing = (C) => (props) => <C {...props} />;
const StableCounter = withNothing(Counter);

function Page({ bad }) {
  const [, force] = useState(0);
  const Wrapped = bad ? withNothing(Counter) : StableCounter;   // bad: new type on every render
  return <><Wrapped /><button onClick={() => force(x => x + 1)}>re-render page</button></>;
}
const user = userEvent.setup();
for (const bad of [false, true]) {
  render(<Page bad={bad} />);
  await user.click(screen.getByText(/count/));
  await user.click(screen.getByText(/count/));
  const before = screen.getByText(/count/).textContent;
  await user.click(screen.getByText('re-render page'));
  console.log(`${bad ? 'HOC inside render' : 'HOC at module   '} : ${before} -> after parent re-render: ${screen.getByText(/count/).textContent}`);
  cleanup();
}
