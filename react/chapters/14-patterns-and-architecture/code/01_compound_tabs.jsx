// Compound components: <Tabs> + <Tabs.List> + <Tabs.Tab> + <Tabs.Panel>, checked with RTL role queries.
import '../../../../tools_dom.mjs';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { Tabs } from './Tabs.jsx';

function ProductTabs() {
  return (
    <Tabs defaultValue="details">
      <Tabs.List label="Product information">
        <Tabs.Tab value="details">Details</Tabs.Tab>
        <Tabs.Tab value="reviews">Reviews</Tabs.Tab>
        <Tabs.Tab value="shipping">Shipping</Tabs.Tab>
      </Tabs.List>
      <Tabs.Panel value="details">Enamel mug, 350 ml.</Tabs.Panel>
      <Tabs.Panel value="reviews">4.5 stars from 12 reviews.</Tabs.Panel>
      <Tabs.Panel value="shipping">Ships in 2 days.</Tabs.Panel>
    </Tabs>
  );
}

const user = userEvent.setup();
const state = () => screen.getAllByRole('tab')
  .map(t => `${t.textContent}${t.getAttribute('aria-selected') === 'true' ? '*' : ''}`).join(' | ')
  + `  panel="${screen.getByRole('tabpanel').textContent}"`;

render(<ProductTabs />);
console.log('tablist name :', screen.getByRole('tablist').getAttribute('aria-label'));
console.log('start        :', state());
await user.click(screen.getByRole('tab', { name: 'Reviews' }));
console.log('click Reviews:', state());
await user.keyboard('{ArrowRight}');
console.log('ArrowRight   :', state(), ' focus:', document.activeElement.textContent);
await user.keyboard('{ArrowRight}');
console.log('ArrowRight   :', state(), ' (wrapped around)');
await user.keyboard('{End}');
console.log('End          :', state());
console.log('panel labelled by its tab:', screen.getByRole('tabpanel', { name: 'Shipping' }).textContent);
console.log('tab stops    :', screen.getAllByRole('tab').map(t => t.tabIndex).join(','));

// Controlled usage: the parent owns the value.
document.body.innerHTML = '<div id="root"></div>';
function Controlled() {
  const [tab, setTab] = useState('b');
  return (
    <>
      <p>parent state: {tab}</p>
      <Tabs value={tab} onValueChange={setTab}>
        <Tabs.List label="Demo"><Tabs.Tab value="a">A</Tabs.Tab><Tabs.Tab value="b">B</Tabs.Tab></Tabs.List>
        <Tabs.Panel value="a">Panel A</Tabs.Panel><Tabs.Panel value="b">Panel B</Tabs.Panel>
      </Tabs>
    </>
  );
}
render(<Controlled />);
await user.click(screen.getByRole('tab', { name: 'A' }));
console.log('controlled   :', screen.getByText(/parent state/).textContent, '|', screen.getByRole('tabpanel').textContent);

// Misuse is caught with a clear message.
const origError = console.error; console.error = () => {};
try { render(<Tabs.Tab value="x">Lonely</Tabs.Tab>); } catch (e) { console.log('misuse       :', e.message); }
console.error = origError;
