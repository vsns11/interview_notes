// Putting it together: useId + useDebounce + useFetch + useLocalStorage in one small feature.
import '../../../../tools_dom.mjs';
import http from 'node:http';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { UserSearch } from './UserSearch.jsx';

const PEOPLE = [
  { id: 1, name: 'Ada Lovelace', role: 'Analyst' }, { id: 2, name: 'Alan Turing', role: 'Cryptographer' },
  { id: 3, name: 'Grace Hopper', role: 'Admiral' }, { id: 4, name: 'Adele Goldberg', role: 'Smalltalk' },
];
let requests = [];
const server = http.createServer((req, res) => {
  const q = new URL(req.url, 'http://x').searchParams.get('q').toLowerCase();
  requests.push(q);
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(PEOPLE.filter(p => p.name.toLowerCase().includes(q))));
});
await new Promise(r => server.listen(0, '127.0.0.1', r));
const apiBase = `http://127.0.0.1:${server.address().port}`;

render(<UserSearch apiBase={apiBase} />);
const input = screen.getByLabelText('Find a person');           // works because of useId wiring
for (const t of ['a', 'ad', 'ada']) fireEvent.change(input, { target: { value: t } });  // fast typing
await waitFor(() => screen.getByText('Ada Lovelace'));
console.log('typed a, ad, ada quickly -> requests sent:', JSON.stringify(requests));
console.log('results:', screen.getAllByRole('listitem').map(li => li.textContent).join(' / '));

fireEvent.keyDown(input, { key: 'Enter' });
fireEvent.change(input, { target: { value: 'zed' } });
await waitFor(() => screen.getByText(/No one called/));
console.log('after "zed":', screen.getByText(/No one called/).textContent);
fireEvent.keyDown(input, { key: 'Enter' });
console.log(screen.getByText(/Recent/).textContent, '| stored:', localStorage.getItem('recent-searches'));
server.close();
