// Same viewport, same component: the layout follows the CONTAINER's width, not the viewport's.
import { withPage } from './measure.mjs';

for (const width of [1000, 700]) {
  const rows = await withPage('04_container_queries.html', page => page.$$eval('.card-slot', slots => slots.map(s => {
    const card = s.querySelector('.product'), h3 = s.querySelector('h3');
    return { name: h3.firstChild.textContent, container: Math.round(s.getBoundingClientRect().width),
             columns: getComputedStyle(card).gridTemplateColumns, title: getComputedStyle(h3).fontSize,
             featured: getComputedStyle(h3, '::after').content };
  })), { width });
  console.log(`viewport ${width}px`);
  for (const r of rows) console.log(`  ${r.name.padEnd(13)} container ${String(r.container).padStart(3)}px  columns ${r.columns.padEnd(14)} h3 ${r.title.padEnd(9)} ::after ${r.featured}`);
}
// Hand check of the h3 size: clamp(15px, 3cqi + 8px, 24px). 1cqi = 1% of the container width.
//   366px container: 3 * 3.66 + 8 = 18.98px.   220px container: 3 * 2.2 + 8 = 14.6px -> clamped up to 15px.
