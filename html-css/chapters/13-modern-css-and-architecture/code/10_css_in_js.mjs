// Runs the mini CSS-in-JS library in jsdom: which class names and rules does rendering produce?
import '../../../../tools_dom.mjs';
import './10_mini_css_in_js.js';
import './10_card_component.js';

const a = Card({ name: 'Trail shoes', price: '$89' });
const b = Card({ name: 'Rain jacket', price: '$129', featured: true });
document.body.append(a, b);
console.log('card 1 classes:', a.className, [...a.children].map(c => c.className).join(' '));
console.log('card 2 classes:', b.className, [...b.children].map(c => c.className).join(' '));
console.log(`\nrules injected at runtime (${miniCss.rules().length}):`);
for (const r of miniCss.rules()) console.log('  ' + r.slice(0, 110));

// The classic runtime cost: a style that depends on a CONTINUOUS prop makes a new rule per value
const { css } = miniCss;
const before = miniCss.rules().length;
for (let pct = 0; pct <= 100; pct++) css({ width: pct + '%', height: '4px', background: '#2563eb' });
console.log(`\nprogress bar rendered at 0..100%: ${miniCss.rules().length - before} extra rules`);
// The fix: one static class + a CSS custom property set inline
const bar = css({ width: 'var(--pct)', height: '4px', background: '#2563eb' });
console.log(`with width: var(--pct) instead: 1 class (${bar}) + style="--pct: 42%"`);
