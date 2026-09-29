// React Router v8: route middleware (stable and on by default) — auth guard + timing, sharing data via context.
import '../../../../tools_dom.mjs';
import { createMemoryRouter, createContext, redirect, useLoaderData, Outlet } from 'react-router';
import { RouterProvider } from 'react-router/dom';
import { render, screen, waitFor } from '@testing-library/react';

let session = null;                                        // pretend auth state (a cookie/token in real life)
const userContext = createContext(null);                   // typed slot in the router context

async function logMiddleware({ request }, next) {
  const path = new URL(request.url).pathname;
  console.log(`  -> ${path}`);
  await next();                                            // run the rest: child middleware + loaders
  console.log(`  <- ${path} done`);
}
async function authMiddleware({ request, context }) {
  if (!session) {
    const next = new URL(request.url).pathname;
    console.log('  auth: no session, redirecting');
    throw redirect(`/login?next=${encodeURIComponent(next)}`);
  }
  context.set(userContext, session);                       // loaders below can read it
}

function Dashboard() { const { user } = useLoaderData(); return <h1>Dashboard for {user.name}</h1>; }
function Login() { return <h1>Please log in</h1>; }

const routes = [
  { path: '/', Component: Outlet, middleware: [logMiddleware], HydrateFallback: () => null, children: [
    { path: 'login', Component: Login },
    { path: 'dashboard', Component: Dashboard, middleware: [authMiddleware],
      loader: ({ context }) => ({ user: context.get(userContext) }) },
  ] },
];

console.log('signed out, open /dashboard:');
let router = createMemoryRouter(routes, { initialEntries: ['/dashboard'] });
let r = render(<RouterProvider router={router} />);
await waitFor(() => screen.getByRole('heading'));
console.log('  shows', JSON.stringify(screen.getByRole('heading').textContent), 'at', router.state.location.pathname + router.state.location.search);
r.unmount();

console.log('signed in, open /dashboard:');
session = { name: 'Ada' };
router = createMemoryRouter(routes, { initialEntries: ['/dashboard'] });
render(<RouterProvider router={router} />);
await waitFor(() => screen.getByRole('heading'));
console.log('  shows', JSON.stringify(screen.getByRole('heading').textContent));
