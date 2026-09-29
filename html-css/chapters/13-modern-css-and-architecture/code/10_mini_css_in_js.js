// tools_run: skip  (library: loaded by 10_css_in_js.html with <script> and by 10_css_in_js.mjs)
// A ~25-line "runtime CSS-in-JS" library, to show the mechanism behind styled-components / Emotion:
// styles are JS objects -> serialized -> hashed -> a generated class name -> a rule injected into a <style>.
(function () {
  const cache = new Map();                       // serialized CSS -> class name (inject each rule once)
  let sheet;
  const kebab = k => k.replace(/[A-Z]/g, c => '-' + c.toLowerCase());
  const hash = s => { let h = 5381; for (const c of s) h = (h * 33) ^ c.charCodeAt(0); return (h >>> 0).toString(36); };

  function css(styles) {
    const own = [], nested = [];
    for (const [k, v] of Object.entries(styles)) (typeof v === 'object' ? nested : own).push([k, v]);
    const body = own.map(([k, v]) => `${kebab(k)}:${v}`).join(';');
    const key = JSON.stringify(styles);
    if (cache.has(key)) return cache.get(key);
    const cls = 'css-' + hash(key);
    if (!sheet) { const s = document.createElement('style'); s.dataset.mini = ''; document.head.append(s); sheet = s.sheet; }
    sheet.insertRule(`.${cls}{${body}}`, sheet.cssRules.length);
    for (const [sel, obj] of nested)               // '&:hover': {...}  ->  .css-abc:hover {...}
      sheet.insertRule(`${sel.replace('&', '.' + cls)}{${Object.entries(obj).map(([k, v]) => `${kebab(k)}:${v}`).join(';')}}`, sheet.cssRules.length);
    cache.set(key, cls);
    return cls;
  }
  globalThis.miniCss = { css, rules: () => [...(sheet?.cssRules ?? [])].map(r => r.cssText) };
})();
