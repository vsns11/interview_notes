// Parse sloppy HTML the way a browser does (jsdom uses the WHATWG parsing algorithm) and print the DOM tree.
import { JSDOM } from 'jsdom';

const body = `<title>Parsing demo</title>
<p>First paragraph
<p>Second <b>bold <i>both</b> italic?</i>
<table><tr><td>cell</td></tr></table>`;

function show(node, depth = 0) {
  const pad = '  '.repeat(depth);
  if (node.nodeType === 1) console.log(pad + node.localName);
  else if (node.nodeType === 3 && node.data.trim()) console.log(pad + JSON.stringify(node.data.trim()));
  for (const child of node.childNodes) show(child, depth + 1);
}

const std = new JSDOM('<!DOCTYPE html>\n' + body).window.document;
console.log('Mode:', std.compatMode, '(CSS1Compat = standards mode)');
console.log('Source had no <html>, <head>, <body>, </p> or <tbody> — the parser added them:\n');
show(std.documentElement);
console.log('\nSerialized back:\n' + std.body.innerHTML);

const quirks = new JSDOM(body).window.document;             // same markup, no doctype
console.log('\nWithout <!DOCTYPE html> -> mode:', quirks.compatMode, '(BackCompat = quirks mode)');
console.log('Is the <table> inside the 2nd <p>?  standards:', !!std.querySelector('p > table'),
            '| quirks:', !!quirks.querySelector('p > table'));
