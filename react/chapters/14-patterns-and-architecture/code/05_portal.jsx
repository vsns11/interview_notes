// Portals: render children into a different DOM node while staying in the same React tree.
import '../../../../tools_dom.mjs';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createContext, useContext, useState } from 'react';
import { Dialog } from './Dialog.jsx';

const ThemeContext = createContext('light');
function ThemedText() { return <p>theme from context: {useContext(ThemeContext)}</p>; }

function ProductCard() {
  const [open, setOpen] = useState(false);
  const [bubbled, setBubbled] = useState(0);
  return (
    // overflow:hidden would clip an absolutely positioned popup rendered in here — a portal escapes it
    <div className="card" style={{ overflow: 'hidden' }} onClick={() => setBubbled(n => n + 1)}>
      <button onClick={() => setOpen(true)}>Delete product</button>
      <span data-testid="bubbled">{bubbled}</span>
      <Dialog open={open} onClose={() => setOpen(false)} title="Delete Trail Mug?">
        <p>This cannot be undone.</p>
        <ThemedText />
      </Dialog>
    </div>
  );
}

const user = userEvent.setup();
render(<ThemeContext value="dark"><ProductCard /></ThemeContext>);
const trigger = screen.getByRole('button', { name: 'Delete product' });

await user.click(trigger);
const dialog = screen.getByRole('dialog', { name: 'Delete Trail Mug?' });   // accessible name from aria-labelledby
console.log('dialog DOM parent        :', dialog.parentElement.parentElement.tagName, '(not inside .card)');
console.log('inside .card?            :', !!dialog.closest('.card'));
console.log('aria-modal               :', dialog.getAttribute('aria-modal'));
console.log('focus moved into dialog  :', document.activeElement === dialog);
console.log('context crosses portal   :', screen.getByText(/theme from context/).textContent);

const before = screen.getByTestId('bubbled').textContent;
await user.click(screen.getByText('This cannot be undone.'));   // click INSIDE the portal content
console.log(`React events bubble to the React parent: card onClick count ${before} -> ${screen.getByTestId('bubbled').textContent}`);

await user.keyboard('{Escape}');
console.log('Escape closes            :', screen.queryByRole('dialog') === null);
console.log('focus back on trigger    :', document.activeElement === trigger);
