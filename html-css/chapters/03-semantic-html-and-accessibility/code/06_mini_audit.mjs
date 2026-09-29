// A mini automated accessibility audit (the idea behind axe-core / Lighthouse), using jsdom and
// dom-accessibility-api, which implements the W3C "Accessible Name and Description Computation" spec.
import { JSDOM } from 'jsdom';
import fs from 'node:fs';
import { computeAccessibleName, getRole } from 'dom-accessibility-api';
import { contrast } from './contrast_lib.mjs';

function audit(file) {
  const { window } = new JSDOM(fs.readFileSync(file, 'utf8'));
  const doc = window.document;
  globalThis.window = window;                     // dom-accessibility-api calls getComputedStyle on window
  const issues = [];
  const add = (rule, wcag, el, msg) => issues.push(`${rule.padEnd(20)} ${wcag.padEnd(7)} <${el.localName}> ${msg}`);

  if (!doc.documentElement.lang) add('html-has-lang', '3.1.1', doc.documentElement, 'no lang attribute');
  for (const h of doc.querySelectorAll('h1,h2,h3,h4,h5,h6'))
    if (!h.textContent.trim()) add('empty-heading', 'BP', h, 'heading has no text');
  for (const img of doc.querySelectorAll('img'))
    if (!img.hasAttribute('alt')) add('image-alt', '1.1.1', img, 'missing alt (use alt="" if decorative)');
  const interactive = 'a[href], button, input:not([type=hidden]), select, textarea, [role=button], [role=link], [role=checkbox]';
  for (const el of doc.querySelectorAll(interactive)) {
    const name = computeAccessibleName(el);
    const role = getRole(el) ?? `${el.type} field`;          // <input type=password> has no ARIA role
    if (el.closest('[aria-hidden="true"]')) { add('aria-hidden-focus', '4.1.2', el, 'focusable element inside aria-hidden'); continue; }
    if (!name.trim() && el.placeholder) add('label', '3.3.2', el, `only a placeholder ("${el.placeholder}"), no label`);
    else if (!name.trim()) add('accessible-name', '4.1.2', el, `${role} has no accessible name`);
    if (el.tabIndex > 0) add('tabindex', '2.4.3', el, `tabindex="${el.tabIndex}" breaks the natural focus order`);
    if (el.matches('[role=button]:not([tabindex])')) add('focusable-role', '2.1.1', el, 'role="button" but not keyboard-focusable');
  }
  for (const el of doc.querySelectorAll('[aria-describedby], [aria-labelledby]'))
    for (const id of (el.getAttribute('aria-describedby') ?? el.getAttribute('aria-labelledby')).split(/\s+/))
      if (!doc.getElementById(id)) add('aria-valid-ref', '4.1.2', el, `references missing id "${id}"`);
  for (const lab of doc.querySelectorAll('label:not([for])'))
    if (!lab.querySelector('input,select,textarea')) add('orphan-label', '1.3.1', lab, `"${lab.textContent}" is not tied to any control`);
  for (const el of doc.querySelectorAll('[style*="color"]')) {
    const fg = el.style.color && '#' + el.style.color.match(/\d+/g).map(n => (+n).toString(16).padStart(2, '0')).join('');
    const r = contrast(fg, '#ffffff');
    if (r < 4.5) add('color-contrast', '1.4.3', el, `${fg} on #ffffff is ${r.toFixed(2)}:1 (needs 4.5:1)`);
  }
  console.log(`${file}: ${issues.length} issue(s)`);
  console.log('rule                 WCAG    element / problem');
  for (const i of issues) console.log(i);
}
audit('bad-a11y.html');
console.log();
audit('form-a11y.html');
