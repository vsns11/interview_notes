// tools_run: skip  (browser entry for gallery.html)
import { createRoot } from 'react-dom/client';
import { GlobalCssCard, ModuleCard, InlineCard, TailwindCard, CssInJsCard } from './cards.jsx';

const cards = [
  ['Global CSS + BEM', GlobalCssCard], ['CSS Module', ModuleCard], ['Inline style', InlineCard],
  ['Tailwind', TailwindCard], ['CSS-in-JS idea', CssInJsCard],
];
createRoot(document.getElementById('root')).render(
  <main className="gallery">
    {cards.map(([label, Card]) => (
      <figure key={label}><figcaption>{label}</figcaption><Card name="Grace Hopper" role="Rear Admiral, COBOL" /></figure>
    ))}
  </main>
);
