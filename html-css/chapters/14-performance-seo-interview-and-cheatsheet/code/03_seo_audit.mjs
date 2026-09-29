// A tiny on-page SEO checker (jsdom — parses the HTML like a crawler's first pass, no JavaScript run).
// Real tools: Lighthouse's SEO audits, Google Search Console, the Rich Results Test.
import { JSDOM } from 'jsdom';
import fs from 'node:fs';

function audit(file) {
  const doc = new JSDOM(fs.readFileSync(file, 'utf8')).window.document;
  const out = [];
  const check = (ok, msg) => out.push(`${ok ? 'PASS' : 'FAIL'}  ${msg}`);
  const meta = n => doc.querySelector(`meta[name="${n}"]`)?.content;

  check(/^<!doctype html>/i.test(fs.readFileSync(file, 'utf8').trim()), 'doctype present (standards mode)');
  check(!!doc.documentElement.lang, `<html lang> = ${JSON.stringify(doc.documentElement.lang || null)}`);
  const title = doc.title.trim();
  check(title.length >= 15 && title.length <= 65, `<title> ${title.length} chars: ${JSON.stringify(title)}`);
  const desc = meta('description');
  check(!!desc && desc.length >= 70 && desc.length <= 160, `meta description ${desc ? desc.length + ' chars' : 'missing'}`);
  check(!!meta('viewport'), 'meta viewport (mobile-friendly)');
  const robots = meta('robots') ?? '';
  check(!/noindex/i.test(robots), `robots: ${JSON.stringify(robots || '(none = index, follow)')}`);
  const canon = doc.querySelector('link[rel="canonical"]')?.getAttribute('href');
  check(!!canon && /^https:\/\//.test(canon), `canonical: ${canon ?? 'missing'} (absolute URL recommended)`);
  if (meta('keywords')) out.push('NOTE  meta keywords present — ignored by Google, harmless but useless');

  const h1s = doc.querySelectorAll('h1').length;
  check(h1s === 1, `one <h1> (found ${h1s}) — a clarity convention, not a Google rule`);
  const levels = [...doc.querySelectorAll('h1,h2,h3,h4,h5,h6')].map(h => +h.tagName[1]);
  const skip = levels.find((l, i) => i > 0 && l > levels[i - 1] + 1);
  check(!skip, `heading levels ${levels.join('→')} ${skip ? '(skips a level)' : ''}`);
  check(!!doc.querySelector('main'), '<main> landmark');
  const noAlt = [...doc.images].filter(i => !i.hasAttribute('alt')).length;
  check(noAlt === 0, `images without alt: ${noAlt}`);
  const vague = [...doc.links].filter(a => /^(click here|here|read more|more)$/i.test(a.textContent.trim()));
  check(vague.length === 0, `vague link text: ${vague.map(a => JSON.stringify(a.textContent.trim())).join(', ') || 'none'}`);

  for (const s of doc.querySelectorAll('script[type="application/ld+json"]')) {
    try {
      const d = JSON.parse(s.textContent);
      const want = { Article: ['headline', 'image', 'datePublished', 'author'], BreadcrumbList: ['itemListElement'] }[d['@type']] ?? [];
      const missing = want.filter(k => !(k in d));
      check(d['@context'] === 'https://schema.org' && missing.length === 0,
        `JSON-LD ${d['@type']}: ${missing.length ? 'missing ' + missing.join(', ') : 'recommended properties present'}`);
    } catch (e) { check(false, `JSON-LD does not parse: ${e.message.split('\n')[0]}`); }
  }
  console.log(`\n${file}`);
  console.log(out.map(l => '  ' + l).join('\n'));
  console.log(`  score: ${out.filter(l => l.startsWith('PASS')).length}/${out.filter(l => !l.startsWith('NOTE')).length}`);
}
audit('03_seo_bad.html');
audit('03_seo_good.html');
