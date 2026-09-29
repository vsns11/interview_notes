// Resolve every link in links.html as if the page lived at a real URL, and report what each one does.
import { JSDOM } from 'jsdom';
import fs from 'node:fs';

const { window } = new JSDOM(fs.readFileSync('links.html'), { url: 'https://club.example.org/guides/html/links.html' });
const doc = window.document;
console.log('attribute vs property:', doc.querySelector('a[href="text-blocks.html"]').getAttribute('href'), '->',
            doc.querySelector('a[href="text-blocks.html"]').href, '\n');
for (const a of doc.querySelectorAll('a')) {
  const text = a.textContent.trim().slice(0, 26).padEnd(26);
  if (!a.hasAttribute('href')) { console.log(text, '(no href: not in document.links =', [...doc.links].includes(a) + ')'); continue; }
  const notes = [a.protocol];
  if (a.target) notes.push('target=' + a.target);
  if (a.rel) notes.push('rel=' + a.rel);
  if (a.hasAttribute('download')) notes.push('download=' + a.download);
  if (a.hash && a.pathname === window.location.pathname) notes.push('same-page jump to ' + a.hash);
  console.log(text, a.href.padEnd(55), notes.join(' '));
}
