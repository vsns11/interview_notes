// Typing custom Hooks and Context.
import '../../../../tools_dom.mjs';
import { useState, useEffect, useCallback, createContext, useContext } from 'react';
import type { ReactNode } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// 1. A Hook returning a tuple: `as const` makes it readonly [boolean, () => void]
//    instead of (boolean | (() => void))[].
function useToggle(initial = false) {
  const [on, setOn] = useState(initial);
  const toggle = useCallback(() => setOn(v => !v), []);
  return [on, toggle] as const;
}

// 2. A generic Hook: the caller chooses T, and the result is a discriminated union,
//    so `data` exists only when status is 'success'.
type FetchState<T> =
  | { status: 'loading' }
  | { status: 'error'; error: string }
  | { status: 'success'; data: T };

function useJson<T>(url: string): FetchState<T> {
  const [state, setState] = useState<FetchState<T>>({ status: 'loading' });
  useEffect(() => {
    let ignore = false;
    setState({ status: 'loading' });
    fetch(url)
      .then(r => (r.ok ? (r.json() as Promise<T>) : Promise.reject(new Error(`HTTP ${r.status}`))))
      .then(data => { if (!ignore) setState({ status: 'success', data }); })
      .catch((e: unknown) => { if (!ignore) setState({ status: 'error', error: e instanceof Error ? e.message : String(e) }); });
    return () => { ignore = true; };
  }, [url]);
  return state;
}
// NOTE: `as Promise<T>` is a promise to the compiler, not a check. Validate real API data (e.g. with Zod).

// 3. Context whose value may be missing: default null + a Hook that throws a helpful error.
type Auth = { user: string; logout: () => void };
const AuthContext = createContext<Auth | null>(null);

function useAuth(): Auth {                       // callers get Auth, never null
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState('ada');
  // (in a real app, memoize this object - see Chapter 10, section 10.10)
  return <AuthContext value={{ user, logout: () => setUser('guest') }}>{children}</AuthContext>;
}

type Todo = { id: number; title: string };
function TodoTitle() {
  const result = useJson<Todo>('/api/todos/1');
  const [details, toggle] = useToggle();
  const { user, logout } = useAuth();
  if (result.status === 'loading') return <p>loading...</p>;
  if (result.status === 'error') return <p>error: {result.error}</p>;
  // Here TypeScript knows result.data is a Todo.
  return (
    <div>
      <p>{user}: {result.data.title}{details && ` (#${result.data.id})`}</p>
      <button onClick={toggle}>details</button><button onClick={logout}>logout</button>
    </div>
  );
}

// A fake fetch so the example runs offline.
globalThis.fetch = (async (url: string) =>
  new Response(JSON.stringify({ id: 1, title: `loaded from ${url}` }), { status: 200 })) as typeof fetch;

render(<AuthProvider><TodoTitle /></AuthProvider>);
console.log(document.body.textContent);
await screen.findByText(/loaded from/);
fireEvent.click(screen.getByText('details'));
fireEvent.click(screen.getByText('logout'));
console.log(screen.getByText(/loaded/).textContent);

// Outside the provider: the Hook's error explains the mistake.
const quiet = console.error; console.error = () => {};
try { render(<TodoTitle />); } catch (e) { console.log('without provider ->', (e as Error).message); }
console.error = quiet;
