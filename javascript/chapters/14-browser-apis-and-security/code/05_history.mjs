import '../../../../tools_dom.mjs';
// A tiny client-side router using the History API (what React Router etc. do underneath)
document.body.innerHTML = `
<nav><a href="/">Home</a> <a href="/products">Products</a> <a href="/products/42">Lamp</a> <a href="https://other.site/x">External</a></nav>
<main id="view"></main>`;

const routes = [
  [/^\/$/, () => 'Home page'],
  [/^\/products$/, () => 'All products'],
  [/^\/products\/(\d+)$/, (id) => `Product #${id}`],
];
function render() {
  const path = location.pathname;
  for (const [re, view] of routes) {
    const m = path.match(re);
    if (m) { document.getElementById('view').textContent = view(...m.slice(1)); return; }
  }
  document.getElementById('view').textContent = '404';
}
const show = (label) => console.log(label.padEnd(28), location.pathname.padEnd(14), '|', document.getElementById('view').textContent, '| state:', JSON.stringify(history.state), '| length:', history.length);

// Intercept same-origin link clicks (delegation) and navigate without reloading
document.addEventListener('click', (e) => {
  const a = e.target.closest('a');
  if (!a || a.origin !== location.origin || e.ctrlKey || e.metaKey) return;   // let the browser handle it
  e.preventDefault();
  history.pushState({ from: location.pathname }, '', a.href);                  // new history entry, no request
  render();
});
// Back/forward buttons fire popstate: re-render from the URL
window.addEventListener('popstate', () => { render(); show('popstate (back/fwd)'); });

render(); show('initial');
document.querySelector('a[href="/products"]').click(); show('click Products');
document.querySelector('a[href="/products/42"]').click(); show('click Lamp');
history.replaceState({ tab: 'reviews' }, '', '/products/42?tab=reviews'); show('replaceState (no new entry)');
history.back();
await new Promise((r) => setTimeout(r, 30));
const ext = document.querySelector('a[href^="https"]');
console.log('external link: same origin?', ext.origin === location.origin, '-> not intercepted');
