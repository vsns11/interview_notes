// tools_run: skip  (shared by 10_css_in_js.html and 10_css_in_js.mjs)
// A "component" whose styles live next to its markup, computed from props.
globalThis.Card = function Card({ name, price, featured }) {
  const { css } = globalThis.miniCss;
  const accent = featured ? '#d97706' : '#2563eb';
  const el = document.createElement('article');
  el.className = css({ width: '170px', padding: '14px', borderRadius: '12px',
    border: featured ? '2px solid #f59e0b' : '1px solid #cbd5e1', background: featured ? '#fffbeb' : '#fff' });
  el.innerHTML = `<h3 class="${css({ margin: 0, fontSize: '16px' })}">${name}</h3>
    <p class="${css({ margin: '4px 0 12px', color: '#475569' })}">${price}</p>
    <button class="${css({ width: '100%', padding: '8px', border: 0, borderRadius: '8px', background: accent,
      color: '#fff', font: 'inherit', '&:hover': { filter: 'brightness(0.9)' } })}">Add to cart</button>`;
  return el;
};
