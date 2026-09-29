// Context performance: every consumer re-renders when the provided value changes (by Object.is).
import '../../../../tools_dom.mjs';
import { createContext, useContext, useState, useMemo, useCallback, memo } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

const renders = {};
const count = (name) => { renders[name] = (renders[name] ?? 0) + 1; };
const report = (label) => { console.log(label.padEnd(44), JSON.stringify(renders)); for (const k in renders) delete renders[k]; };

// ---- 1. A new object as `value` on every render re-renders every consumer, even memo'd ones.
const AuthContext = createContext(null);

const UserBadge = memo(function UserBadge() {           // reads the user
  count('UserBadge'); const { user } = useContext(AuthContext); return <span>{user}</span>;
});
const LogoutButton = memo(function LogoutButton() {     // only needs the function
  count('LogoutButton'); const { logout } = useContext(AuthContext); return <button onClick={logout}>logout</button>;
});
const Sidebar = memo(function Sidebar() { count('Sidebar(no context)'); return <nav>links</nav>; });

function App({ memoValue }) {
  const [user, setUser] = useState('ada');
  const [clock, setClock] = useState(0);                // unrelated state in the same component
  const logout = useCallback(() => setUser(null), []);
  const literal = { user, logout };                      // 🔴 new object every render
  const memoised = useMemo(() => ({ user, logout }), [user, logout]);   // ✅ same object until user changes
  return (
    <AuthContext value={memoValue ? memoised : literal}>
      <UserBadge /><LogoutButton /><Sidebar />
      <button onClick={() => setClock(clock + 1)}>tick</button>
    </AuthContext>
  );
}

for (const memoValue of [false, true]) {
  render(<App memoValue={memoValue} />); report(`mount (${memoValue ? 'useMemo value' : 'literal value'})`);
  fireEvent.click(screen.getByText('tick')); report('  unrelated state change ->');
  fireEvent.click(screen.getByText('logout')); report('  user changed ->');
  cleanup();
}

// ---- 2. Split state and actions into two contexts: action-only consumers stop re-rendering.
const UserContext = createContext(null);
const AuthActionsContext = createContext(null);
const Badge2 = memo(function Badge2() { count('Badge2'); return <span>{useContext(UserContext)}</span>; });
const Logout2 = memo(function Logout2() {
  count('Logout2'); const { logout } = useContext(AuthActionsContext); return <button onClick={logout}>logout</button>;
});
function AuthProvider({ children }) {
  const [user, setUser] = useState('ada');
  const actions = useMemo(() => ({ logout: () => setUser(null), login: (u) => setUser(u) }), []);  // never changes
  return <UserContext value={user}><AuthActionsContext value={actions}>{children}</AuthActionsContext></UserContext>;
}
// `children` are created by the caller, so they don't re-render when AuthProvider's state changes.
render(<AuthProvider><Badge2 /><Logout2 /><Sidebar /></AuthProvider>); report('split contexts: mount');
fireEvent.click(screen.getByText('logout')); report('  user changed ->');
