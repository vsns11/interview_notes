// Render props: a component shares behaviour by CALLING a function you pass, instead of fixing the markup.
import '../../../../tools_dom.mjs';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';

// 1. "Function as children": the component owns state, the caller owns the UI.
function Disclosure({ defaultOpen = false, children }) {
  const [open, setOpen] = useState(defaultOpen);
  return children({ open, toggle: () => setOpen(o => !o) });
}

// 2. A named render prop: a list that knows about loading/empty states but not how an item looks.
function List({ items, renderItem, renderEmpty = () => <p>Nothing here.</p> }) {
  if (items.length === 0) return renderEmpty();
  return <ul>{items.map((item, i) => <li key={item.id}>{renderItem(item, i)}</li>)}</ul>;
}

// 3. The same behaviour as a custom Hook — today's usual choice.
function useDisclosure(defaultOpen = false) {
  const [open, setOpen] = useState(defaultOpen);
  return { open, toggle: () => setOpen(o => !o) };
}

const user = userEvent.setup();
const html = () => document.getElementById('root').innerHTML;
const mount = (ui) => { cleanup(); document.body.innerHTML = '<div id="root"></div>'; render(ui, { container: document.getElementById('root') }); };

mount(
  <Disclosure>
    {({ open, toggle }) => (
      <>
        <button aria-expanded={open} onClick={toggle}>Shipping details</button>
        {open && <p>Ships in 2 days.</p>}
      </>
    )}
  </Disclosure>,
);
console.log('render prop, closed :', html());
await user.click(screen.getByRole('button'));
console.log('render prop, open   :', html());

const products = [{ id: 1, name: 'Trail Mug', price: 18 }, { id: 2, name: 'Camp Stove', price: 45 }];
mount(<List items={products} renderItem={(p, i) => <>{i + 1}. {p.name} — ${p.price}</>} />);
console.log('renderItem          :', html());
mount(<List items={[]} renderItem={() => null} renderEmpty={() => <p>No products match.</p>} />);
console.log('renderEmpty         :', html());

function HookVersion() {
  const { open, toggle } = useDisclosure();
  return <><button aria-expanded={open} onClick={toggle}>Shipping details</button>{open && <p>Ships in 2 days.</p>}</>;
}
mount(<HookVersion />);
await user.click(screen.getByRole('button'));
console.log('same thing via Hook :', html());
