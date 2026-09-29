// A tiny HTML "linter": checks some of the things the W3C validator and accessibility tools flag.
// (It is a teaching aid, not a replacement for https://validator.w3.org/nu/.)
import { JSDOM } from 'jsdom';
import fs from 'node:fs';

function lint(file) {
  const src = fs.readFileSync(file, 'utf8');
  const doc = new JSDOM(src).window.document;
  const problems = [];
  if (!/^\s*<!doctype html>/i.test(src)) problems.push('missing <!DOCTYPE html> -> quirks mode');
  if (!doc.documentElement.hasAttribute('lang')) problems.push('<html> has no lang attribute');
  const charset = src.search(/<meta[^>]+charset/i);
  if (charset < 0 || charset > 1024) problems.push('no <meta charset> in the first 1024 bytes');
  if (!doc.querySelector('meta[name=viewport]')) problems.push('no viewport meta (page will not scale on phones)');
  if (!doc.title.trim()) problems.push('empty or missing <title>');
  const h1s = doc.querySelectorAll('h1').length;
  if (h1s !== 1) problems.push(`${h1s} <h1> elements (aim for exactly one)`);
  let prev = 0;
  for (const h of doc.querySelectorAll('h1,h2,h3,h4,h5,h6')) {
    const level = +h.localName[1];
    if (prev && level > prev + 1) problems.push(`heading jumps from h${prev} to h${level}: "${h.textContent}"`);
    prev = level;
  }
  const seen = new Set();
  for (const el of doc.querySelectorAll('[id]')) {
    if (seen.has(el.id)) problems.push(`duplicate id="${el.id}"`);
    seen.add(el.id);
  }
  for (const img of doc.querySelectorAll('img:not([alt])')) problems.push('<img> without alt');
  for (const a of doc.querySelectorAll('a[href="#"], a[href^="javascript:"]')) problems.push(`fake link "${a.textContent}" (use <button>)`);
  for (const a of doc.querySelectorAll('a[href]'))
    if (/^(click here|here|read more|more)$/i.test(a.textContent.trim())) problems.push(`vague link text "${a.textContent}"`);
  for (const list of doc.querySelectorAll('ul, ol'))
    for (const child of list.children)
      if (!['li', 'script', 'template'].includes(child.localName)) problems.push(`<${child.localName}> directly inside <${list.localName}>`);
  // Mis-nesting like <b><i></b></i> is repaired silently by the parser; detect it from the source:
  if (/<b>[^<]*<i>[^<]*<\/b>/i.test(src)) problems.push('mis-nested tags <b><i>...</b> (parser repaired it)');

  console.log(`${file}: ${problems.length ? problems.length + ' problem(s)' : 'OK'}`);
  for (const p of problems) console.log('  -', p);
}
lint('skeleton.html');
lint('broken.html');
