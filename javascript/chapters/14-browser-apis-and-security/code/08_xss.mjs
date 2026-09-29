import '../../../../tools_dom.mjs';
// Cross-site scripting (XSS): untrusted text ends up interpreted as HTML/JS.
const comment = `Nice post! <img src="x" onerror="stealCookies()"> <script>stealCookies()</script>`;
const box = document.createElement('div');

// 1) UNSAFE: innerHTML parses the string and creates live elements with event-handler attributes
box.innerHTML = comment;
const img = box.querySelector('img');
console.log('1) innerHTML created <img> with onerror handler:', img?.getAttribute('onerror'));
console.log('   <script> inserted via innerHTML exists but never runs:', box.querySelector('script') !== null,
  '(the <img onerror> is what attackers use instead)');

// 2) SAFE: textContent never parses
box.textContent = comment;
console.log('2) textContent -> elements created:', box.children.length, '| shown literally:', box.textContent.slice(0, 22) + '...');

// 3) Escaping when you must build an HTML string (e.g. server-side templates)
const escapeHtml = (s) => String(s).replace(/[&<>"']/g, (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch]);
console.log('3) escaped:', escapeHtml('<img src=x onerror="go()">'));
box.innerHTML = `<p class="comment">${escapeHtml(comment)}</p>`;
console.log('   elements inside <p>:', box.querySelector('p').children.length);

// 4) Escaping is context-specific: an escaped value is still dangerous inside href
const website = 'javascript:alert(document.cookie)';
const a = document.createElement('a');
a.href = escapeHtml(website);                        // escaping does nothing useful here
console.log('4) href after escaping :', a.href, '<- still runs script when clicked');
const safe = (() => { try { const u = new URL(website); return ['http:', 'https:'].includes(u.protocol) ? u.href : '#'; } catch { return '#'; } })();
console.log('   href after allow-list:', safe);

// 5) Other dangerous sinks (all parse or execute strings)
const sinks = ['el.innerHTML = s', 'el.outerHTML = s', 'el.insertAdjacentHTML(pos, s)', 'document.write(s)',
  'eval(s) / new Function(s)', "setTimeout('code string')", 'el.setAttribute("on...", s)', 'a.href = s (javascript:)'];
console.log('5) sinks to avoid with untrusted data:\n   - ' + sinks.join('\n   - '));
