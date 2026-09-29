// tools_run: skip  (browser entry for machine-coding.html; open machine-coding.html#todo, #autocomplete, ...)
// Without a hash it shows all eight builds; with a hash it shows one and scripts it into an
// interesting state for the book's screenshots.
import { createRoot } from 'react-dom/client';
import TodoApp from './todo.jsx';
import Autocomplete from './autocomplete.jsx';
import InfiniteList from './infinite-list.jsx';
import DeleteProjectDemo from './modal.jsx';
import Tabs from './tabs.jsx';
import { ReviewDemo } from './star-rating.jsx';
import OrdersTable from './pagination.jsx';
import SignupWizard from './wizard.jsx';
import './mc.css';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const CITIES = ['Amsterdam', 'Ankara', 'Antwerp', 'Athens', 'Atlanta', 'Bangalore', 'Barcelona', 'Berlin', 'Canberra', 'Havana', 'Lausanne', 'Milan', 'Santander'];
const searchCities = (q, { signal }) => new Promise((resolve, reject) => {
  const t = setTimeout(() => resolve(CITIES.filter((c) => c.toLowerCase().includes(q.toLowerCase())).slice(0, 6)), 150);
  signal.addEventListener('abort', () => { clearTimeout(t); reject(new DOMException('aborted', 'AbortError')); });
});
const fetchPage = async (page) => {
  await sleep(400);
  const size = 8, total = 50;
  const items = Array.from({ length: size }, (_, i) => (page - 1) * size + i + 1).filter((n) => n <= total)
    .map((n) => ({ id: n, name: `Post #${n} — ${['Release notes', 'Weekly digest', 'Team update', 'Bug bash recap'][n % 4]}` }));
  return { items, hasMore: page * size < total };
};
const TABS = [
  { label: 'Overview', content: <p>Plan: <b>Pro</b> · 3 of 5 seats used · renews 1 Oct</p> },
  { label: 'Billing', content: <><p>Next invoice: <b>$48.00</b> on 1 October</p><p className="muted">Visa ending 4242 · billed monthly</p></> },
  { label: 'Audit log', content: <p>No events</p>, disabled: true },
  { label: 'Members', content: <p>Ada, Grace, Linus</p> },
];
const ORDERS = Array.from({ length: 47 }, (_, i) => ({ id: 1001 + i, customer: ['Ada L.', 'Grace H.', 'Linus T.', 'Barbara L.', 'Alan T.'][i % 5], total: 12.5 + ((i * 37) % 90) }));

// Helpers that act like a user in a real browser (React listens for the native events).
function typeInto(el, value) {
  const proto = el.tagName === 'SELECT' ? HTMLSelectElement.prototype : HTMLInputElement.prototype;
  Object.getOwnPropertyDescriptor(proto, 'value').set.call(el, value);
  el.dispatchEvent(new Event(el.tagName === 'SELECT' ? 'change' : 'input', { bubbles: true }));
}
const key = (el, k) => el.dispatchEvent(new KeyboardEvent('keydown', { key: k, bubbles: true }));
const $ = (sel) => document.querySelector(sel);

const BUILDS = {
  todo: {
    render: () => <TodoApp storageKey="mc-demo-todos" />,
    before: () => localStorage.setItem('mc-demo-todos', JSON.stringify([
      { id: 1, title: 'Read the interview Q&A', done: true },
      { id: 2, title: 'Build the autocomplete', done: false },
      { id: 3, title: 'Write the RTL tests', done: false },
      { id: 4, title: 'Practise explaining keys', done: true },
    ])),
  },
  autocomplete: {
    render: () => <Autocomplete label="City" search={searchCities} delay={200} />,
    script: async () => {
      const input = $('[role=combobox]');
      input.focus();
      typeInto(input, 'an');
      await sleep(700);
      key(input, 'ArrowDown'); key(input, 'ArrowDown');
    },
  },
  infinite: {
    render: () => <InfiniteList fetchPage={fetchPage} rootMargin="0px" />,
    script: async () => {
      const bottom = () => { const s = $('.scroller'); s.scrollTop = s.scrollHeight; };
      await sleep(900); bottom();                            // page 2 loads
      await sleep(900); bottom();                            // page 3 starts loading: "Loading more…"
    },
  },
  modal: { render: () => <DeleteProjectDemo defaultOpen />, script: async () => { typeInto($('.dialog input'), 'Apol'); } },
  tabs: { render: () => <Tabs label="Account" tabs={TABS} defaultIndex={1} /> },
  stars: { render: () => <ReviewDemo /> },
  pagination: {
    render: () => <OrdersTable orders={ORDERS} initialPageSize={5} />,
    script: async () => { $('[aria-label="Page 5"]').click(); },
  },
  wizard: {
    render: () => <SignupWizard onSubmit={async () => {}} />,
    script: async () => { typeInto($('#email'), 'ada@example'); await sleep(50); $('button[type=submit]').click(); },
  },
};

const which = location.hash.slice(1);
const root = createRoot(document.getElementById('root'));
if (BUILDS[which]) {
  BUILDS[which].before?.();
  root.render(BUILDS[which].render());
  setTimeout(() => BUILDS[which].script?.(), 100);
} else {
  root.render(
    <div className="gallery">
      {Object.entries(BUILDS).map(([name, b]) => (
        <div key={name}><h1>{name}</h1><div>{name === 'modal' ? <DeleteProjectDemo /> : b.render()}</div></div>
      ))}
    </div>,
  );
}
