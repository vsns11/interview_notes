// Everything from this chapter in one small UI: props, children, composition, conditions, lists + keys.
import '../../../../tools_dom.mjs';
import { render, screen, within } from '@testing-library/react';
import { Catalog, PRODUCTS } from './catalog.jsx';

const { rerender } = render(<Catalog products={PRODUCTS} />);
console.log(screen.getByRole('heading', { level: 2 }).textContent);
for (const card of screen.getAllByRole('article')) {
  const c = within(card);
  const badges = [...card.querySelectorAll('.badge')].map((b) => b.textContent).join(', ') || '-';
  console.log(`  ${c.getByRole('heading').textContent.padEnd(20)} | ${card.querySelector('.price').textContent.padStart(7)} | badges: ${badges.padEnd(18)} | button: ${c.getByRole('button').textContent}${c.getByRole('button').disabled ? ' (disabled)' : ''}`);
}

rerender(<Catalog products={PRODUCTS} minRating={4.5} />);
console.log('\nminRating=4.5 ->', screen.getByRole('heading', { level: 2 }).textContent,
  screen.getAllByRole('article').map((a) => a.getAttribute('aria-label')));

rerender(<Catalog products={PRODUCTS} minRating={5} />);
console.log('minRating=5   ->', document.body.textContent);
