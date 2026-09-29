// What does the browser learn from <head>? Parse skeleton.html and list its metadata.
import { JSDOM } from 'jsdom';
import fs from 'node:fs';

const source = fs.readFileSync('skeleton.html');
const { window } = new JSDOM(source, { url: 'https://club.example.org/guides/trail-running' });
const doc = window.document;

console.log('doctype   :', `<!DOCTYPE ${doc.doctype?.name}>`, '->', doc.compatMode);
console.log('lang      :', doc.documentElement.lang);
console.log('charset   :', doc.characterSet, `(declared at byte ${source.indexOf('charset')} — must be within the first 1024)`);
console.log('title     :', doc.title);
for (const m of doc.head.querySelectorAll('meta[name], meta[property]'))
  console.log('meta      :', (m.name || m.getAttribute('property')).padEnd(12), '=', m.content.slice(0, 60));
for (const l of doc.head.querySelectorAll('link'))
  console.log('link      :', l.rel.padEnd(12), '->', l.href.slice(0, 60));
const comments = [...doc.childNodes].filter(n => n.nodeType === window.Node.COMMENT_NODE);
console.log('comments  :', comments.map(c => c.data.trim()));
console.log('head children:', [...doc.head.children].map(e => e.localName).join(', '));
