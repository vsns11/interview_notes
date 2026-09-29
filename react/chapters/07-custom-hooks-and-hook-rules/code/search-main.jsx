// tools_run: skip  (browser entry for search.html; fetch is mocked so the page needs no server)
import { createRoot } from 'react-dom/client';
import { UserSearch } from './UserSearch.jsx';
import './search.css';

const PEOPLE = [
  { id: 1, name: 'Ada Lovelace', role: 'Analyst' }, { id: 2, name: 'Alan Turing', role: 'Cryptographer' },
  { id: 3, name: 'Grace Hopper', role: 'Admiral' }, { id: 4, name: 'Adele Goldberg', role: 'Smalltalk' },
];
window.fetch = async (url) => {
  const q = new URL(url, location.href).searchParams.get('q').toLowerCase();
  await new Promise(r => setTimeout(r, 100));
  return new Response(JSON.stringify(PEOPLE.filter(p => p.name.toLowerCase().includes(q))));
};
localStorage.setItem('recent-searches', JSON.stringify(['grace', 'alan']));
createRoot(document.getElementById('root')).render(<UserSearch initialQuery="ad" />);
