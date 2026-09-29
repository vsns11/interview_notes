// Drives the city search: debounce, abort of stale requests, error state, global "/" shortcut, document.title.
import '../../../../tools_dom.mjs';
import { render, screen, act, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CitySearch, { createCityApi } from './citysearch.jsx';

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
// Let time pass in small act() steps so each timer's state update is rendered before the next timer fires.
const advance = async (ms) => { for (let t = 0; t < ms; t += 10) await act(() => wait(10)); };
const api = createCityApi(200);
let requests = [];
const trackedApi = (q, opts) => {                     // records requests and cancellations of IN-FLIGHT requests
  let settled = false;
  requests.push(q);
  opts.signal.addEventListener('abort', () => { if (!settled) requests.push(`aborted "${q}"`); });
  return api(q, opts).finally(() => { settled = true; });
};

const user = userEvent.setup();
render(<CitySearch searchCities={trackedApi} delay={50} />);
const box = screen.getByLabelText('Search cities');
const status = () => screen.getByRole('status').textContent;
const cities = () => screen.queryAllByRole('listitem').map((li) => li.querySelector('strong').textContent);

console.log('on open, focused:', document.activeElement === box, '|', status());
await user.type(box, 'o');
await user.type(box, 'sa');                          // typed quickly: one debounced request, not three
await advance(70);
console.log('typed "osa"  ->', status(), '| requests so far:', requests);
await advance(300);
console.log('             ->', status(), cities(), '| title:', document.title);

requests = [];
await user.clear(box); await user.type(box, 'to');
await advance(100);                           // "to" request now in flight (slow)…
await user.type(box, 'r');                            // …user keeps typing
await advance(300);
console.log('"to" then "tor" ->', status(), cities(), '| requests:', requests);

await user.clear(box); await user.type(box, 'error');
await advance(300);
console.log('"error"      ->', status());

act(() => box.blur()); fireEvent.keyDown(window, { key: '/' });
console.log('pressed "/"  -> focused:', document.activeElement === box);
