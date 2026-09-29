// Nested routes: layouts with <Outlet/>, index routes, relative links, route ranking.
import '../../../../tools_dom.mjs';
import { MemoryRouter, Routes, Route, Link, Outlet, useParams, useOutletContext } from 'react-router';
import { render, screen, fireEvent } from '@testing-library/react';

function AppLayout() {                               // renders on EVERY page
  return (<div><header>MyShop</header><main><Outlet /></main><footer>(c) 2026</footer></div>);
}
function AccountLayout() {                           // pathless layout: groups routes, adds no URL segment
  const user = { name: 'Ada' };
  return (<section><aside>Account menu</aside><Outlet context={user} /></section>);
}
function Settings() { const user = useOutletContext(); return <h1>Settings for {user.name}</h1>; }
function UsersLayout() {
  return (
    <div>
      <nav><Link to="new">New user</Link> <Link to="42">User 42</Link> <Link to="..">Up</Link></nav>  {/* relative links */}
      <Outlet />
    </div>
  );
}
function UsersIndex() { return <h1>Pick a user</h1>; }
function NewUser() { return <h1>Create a user</h1>; }
function User() { return <h1>User #{useParams().userId}</h1>; }

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>                          {/* layout route: no path */}
        <Route index element={<h1>Home</h1>} />               {/* matches "/" exactly */}
        <Route path="users" element={<UsersLayout />}>
          <Route index element={<UsersIndex />} />             {/* /users */}
          <Route path="new" element={<NewUser />} />           {/* /users/new - static beats dynamic */}
          <Route path=":userId" element={<User />} />          {/* /users/42 */}
        </Route>
        <Route element={<AccountLayout />}>
          <Route path="settings" element={<Settings />} />
        </Route>
      </Route>
    </Routes>
  );
}

const shape = () => {
  const c = document.body;
  return ['header', 'aside', 'nav'].filter(t => c.querySelector(t)).join('+') + '  h1="' + screen.getByRole('heading').textContent + '"';
};
for (const path of ['/', '/users', '/users/new', '/users/42', '/settings']) {
  const r = render(<MemoryRouter initialEntries={[path]}><App /></MemoryRouter>);
  console.log(path.padEnd(11), '->', shape());
  r.unmount();
}
render(<MemoryRouter initialEntries={['/users/42']}><App /></MemoryRouter>);
fireEvent.click(screen.getByText('New user'));   // relative to the /users route, not to /users/42
console.log('from /users/42 click "new" ->', shape());
fireEvent.click(screen.getByText('Up'));
console.log('click ".."              ->', shape());
