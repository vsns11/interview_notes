// tools_run: skip  (browser entry for virtual.html - screenshot in img/virtual.png)
import { createRoot } from 'react-dom/client';
import { VirtualList } from './virtual_list.jsx';

const ITEMS = Array.from({ length: 10_000 }, (_, i) => ({ id: i, name: `Customer #${i}`, city: ['Pune', 'Oslo', 'Lima', 'Kyoto'][i % 4] }));
createRoot(document.getElementById('root')).render(
  <VirtualList items={ITEMS} rowHeight={32} height={320} overscan={3}
    renderRow={(it, i) => <><span className="idx">{i}</span>{it.name}<span className="city">{it.city}</span></>} />
);
// Scroll half-way down, then report how many row elements really exist.
setTimeout(() => {
  const vp = document.querySelector('.viewport');
  vp.scrollTop = 32 * 5000;
  setTimeout(() => {
    const rows = document.querySelectorAll('.row');
    document.getElementById('stat').textContent =
      `${rows.length} row elements in the DOM for 10,000 items (showing ${rows[0].textContent.match(/#\d+/)[0]} to ${rows[rows.length - 1].textContent.match(/#\d+/)[0]})`;
  }, 100);
}, 50);
