#!/usr/bin/env node
// Render one or more module README.md files into a styled PDF (headless Chrome, Monaco font).
//
// Usage:
//   node tools/md2pdf.mjs <out.pdf> <README.md> [README.md ...]
//
// One input  -> a single module's notes.
// Many inputs -> a combined book with a cover page and table of contents.
// ```mermaid blocks are rendered to SVG; ![alt](images/x.png) course diagrams are embedded.
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { marked } from 'marked';
import puppeteer from 'puppeteer-core';
import hljs from 'highlight.js';

const CHROME = process.env.CHROME_PATH || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const here = path.dirname(fileURLToPath(import.meta.url));
const [outPdf, ...inputs] = process.argv.slice(2);
if (!outPdf || !inputs.length) {
  console.error('usage: node tools/md2pdf.mjs <out.pdf> <README.md> [README.md ...]');
  process.exit(1);
}
const combined = inputs.length > 1;
const mermaidJs = fs.readFileSync(path.join(here, 'node_modules/mermaid/dist/mermaid.min.js'), 'utf8');

const esc = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const slug = s => s.toLowerCase().replace(/<[^>]+>/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

let docIdx = 0, baseDir = '';
const toc = [];
const renderer = new marked.Renderer();
renderer.code = (code, lang) => lang === 'mermaid'
  ? `<figure class="diagram"><pre class="mermaid">${esc(code)}</pre></figure>`
  : (() => {
      const l = (lang || '').toLowerCase().split(/\s/)[0];
      let html;
      try {
        if (l && hljs.getLanguage(l)) html = hljs.highlight(code, { language: l, ignoreIllegals: true }).value;
        else if (!l || l === 'text' || l === 'plain' || l === 'output') html = esc(code);
        else html = hljs.highlightAuto(code, ['java','python','bash','sql','json','javascript','typescript','yaml','xml','go','cpp']).value;
      } catch (e) { html = esc(code); }
      return `<pre class="hl"><code class="hljs lang-${l || 'plain'}">${html}</code></pre>`;
    })();
renderer.image = (href, title, text) => {
  const abs = /^(https?:|file:|data:)/.test(href) ? href : 'file://' + path.resolve(baseDir, href);
  return `<img class="course" src="${abs}" alt="${esc(text || '')}">`;
};
renderer.heading = (text, level, raw) => {
  const id = `d${docIdx}-${slug(raw)}`;
  if (level === 1) toc.push({ id, text, subs: [] });
  else if (level === 2 && toc.length) toc[toc.length - 1].subs.push({ id, text });
  return `<h${level} id="${id}">${text}</h${level}>\n`;
};
marked.use({ renderer, gfm: true });

// single-tilde fix: only ~~double~~ tildes mean strikethrough (single ~ is used for "approximately")
marked.use({ tokenizer: { del(src) { const m = /^~~(?=\S)([\s\S]*?\S)~~(?!~)/.exec(src); if (m) return { type: 'del', raw: m[0], text: m[1], tokens: this.lexer.inlineTokens(m[1]) }; if (/^~(?!~)/.test(src)) return { type: 'text', raw: '~', text: '~' }; return false; } } });

let body = '';
for (const f of inputs) {
  docIdx++;
  baseDir = path.dirname(path.resolve(f));
  body += `<section class="doc">${marked.parse(fs.readFileSync(f, 'utf8'))}</section>\n`;
}

const cover = combined ? `
<div class="cover"><h1>${process.env.COVER_TITLE || 'System Design Patterns'}</h1>
<p>${process.env.COVER_SUB || 'From Fundamentals to Real Systems: beginner-friendly study notes'}</p>
<p>${inputs.length} modules · 60+ patterns · every lesson explained, with course diagrams and links to the originals</p>
<div class="legend"><span class="what">what it is</span><span class="problem">problem</span><span class="how">how it works</span><span class="trade">trade-offs</span><span class="real">real world</span><span class="interview">interview</span><span class="self">self-check</span><span class="tldr">TL;DR</span></div>
<p class="small">Personal study notes summarised from the DesignGurus course (Arslan Ahmad). Not for redistribution.</p></div>
<nav class="toc"><h2>Contents</h2><ol>${toc.map(m => `<li><a href="#${m.id}">${m.text}</a><ul>${m.subs.map(s => `<li><a href="#${s.id}">${s.text}</a></li>`).join('')}</ul></li>`).join('')}</ol></nav>` : '';

const CSS = `
:root{color-scheme:light}
*{box-sizing:border-box}
html{background:#FBF0D9;-webkit-print-color-adjust:exact;print-color-adjust:exact}
body{font-family:Monaco,'Courier New',monospace;font-size:11.5px;line-height:1.55;color:#2E261C;background:#FBF0D9;margin:0;padding:0 11mm}
a{color:#1F5FAF;text-decoration:none}
h1{font-size:20px;color:#1C1712;border-bottom:3px solid #3A5BC7;padding-bottom:6px;margin:0 0 14px}
section.doc + section.doc h1:first-child, .toc + section.doc h1:first-child{page-break-before:always}
h2{font-size:15.5px;margin:18px 0 6px;padding-top:8px;border-top:1px solid #D6C29A;color:#2F5DB0;page-break-after:avoid}
h3{font-size:12.5px;margin:14px 0 6px;color:#2A2218;page-break-after:avoid}
h4{font-size:11.5px;margin:12px 0 4px;color:#2A2218;page-break-after:avoid}
p{margin:5px 0}
hr{border:0;border-top:1px solid #D6C29A;margin:16px 0}
code{font-family:Monaco,'Courier New',monospace;font-size:10px;background:#EAD8B2;color:#8B2F5F;padding:1px 5px;border-radius:4px}
pre{background:#F3E5C6;border:1px solid #D6C29A;border-left:4px solid #3A5BC7;border-radius:6px;padding:8px 11px;margin:8px 0;page-break-inside:avoid;white-space:pre-wrap}
pre code{background:none;color:#2E261C;padding:0;font-size:10px;line-height:1.42;display:block}
table{border-collapse:collapse;width:100%;margin:10px 0;font-size:10.3px;page-break-inside:avoid}
th,td{border:1px solid #D6C29A;padding:5px 8px;text-align:left;vertical-align:top}
th{background:#EAD8B2;color:#1C1712}
tr:nth-child(even) td{background:#F6EAD0}
blockquote{margin:10px 0;padding:8px 13px;background:#F6EAD0;border-left:4px solid #C08A1E;border-radius:5px;color:#4A3B22}
ul,ol{margin:6px 0 6px 4px;padding-left:20px}
li{margin:2px 0}
strong{color:#1C1712}
em{color:#5A4A36}
img.course{display:block;max-width:92%;max-height:95mm;margin:12px auto 2px;border:1px solid #D6C29A;border-radius:8px;padding:6px;background:#ffffff;page-break-inside:avoid}
p:has(> img.course){page-break-inside:avoid;text-align:center}
figure.diagram{margin:10px 0 12px;text-align:center;page-break-inside:avoid;background:#F3E5C6;border:1px solid #D6C29A;border-radius:8px;padding:8px}
figure.diagram svg{max-width:100% !important;max-height:125mm;height:auto}
pre.mermaid{background:none;border:0;padding:0}
.merr{color:#C62828;font-size:9px;white-space:pre-wrap;text-align:left;border:1px dashed #C62828;padding:6px}
.cover{height:250mm;display:flex;flex-direction:column;justify-content:center;page-break-after:always}
.cover h1{font-size:30px;border:0;color:#2F5DB0}
.cover p{font-size:13px;color:#3B3024;margin:4px 0}.cover .small{margin-top:30px;font-size:10px;color:#7A6A52}
.toc{font-size:10px;page-break-after:always}.toc h2{border:0}
.toc>ol>li{margin-top:6px;font-weight:bold}.toc ul{font-weight:normal;columns:2;margin:2px 0 0}
.toc a{color:#2F5DB0}

/* ---------- colour system ---------- */
h1{color:#B8326F;border-bottom:0;padding-bottom:8px;background:linear-gradient(90deg,#B8326F,#0B6E8A,#2E7D3A) bottom/100% 3px no-repeat}
h2{color:#0B6E8A;border-top:0;border-left:5px solid #0B6E8A;padding:6px 10px;background:linear-gradient(90deg,rgba(11,110,138,.10),rgba(11,110,138,0));border-radius:4px}
strong{color:#6B4400}
code{background:#EAD8B2;color:#9C2A6E}
.k{--c:#3B3024}
h3.k,h4.k{color:var(--c);border-left:3px solid var(--c);padding-left:8px}
p.lab>strong:first-child{color:var(--c);display:inline-block;background:color-mix(in srgb,var(--c) 16%,transparent);border:1px solid color-mix(in srgb,var(--c) 45%,transparent);border-radius:4px;padding:0 6px;margin-right:3px}
.box{background:color-mix(in srgb,var(--c) 10%,#FBF0D9);border:1px solid color-mix(in srgb,var(--c) 40%,transparent);border-left:4px solid var(--c);border-radius:6px;padding:8px 12px;margin:10px 0}
.box>strong:first-child{color:var(--c)}
blockquote.box{color:#3B3024}
.what{--c:#2E7D3A}.analogy{--c:#2E7D3A}.problem{--c:#C0392B}.how{--c:#1F6FB2}.walk{--c:#1F6FB2}
.trade{--c:#8A5600}.gain{--c:#2E7D3A}.pay{--c:#C0392B}.whennot{--c:#8A5600}.real{--c:#6B3FB0}
.interview{--c:#B02E86}.mistake{--c:#C62828}.self{--c:#0B6B5E}.tldr{--c:#6E5700}.related{--c:#4F5F80}
.stage{--c:#9A4F00}.intro{--c:#2F5DB0}
ul.box,ol.box{padding:8px 12px 8px 30px}
p.lab{page-break-after:avoid;break-after:avoid}
[data-sec] li>strong:first-child{color:var(--c)}
[data-sec] li>em:first-child>strong,[data-sec] li>strong:first-child>em{color:var(--c)}
.legend{display:flex;flex-wrap:wrap;gap:6px;margin:10px 0 0}.legend span{border:1px solid var(--c);color:var(--c);border-radius:10px;padding:1px 8px;font-size:9px}

/* ---------- colourful code (syntax highlighting) ---------- */
pre.hl{background:#F3E5C6;border:1px solid #D6C29A;border-left:4px solid #8E5BC7;border-radius:8px}
pre.hl code.hljs{color:#2E261C;background:none}
.hljs-comment,.hljs-quote{color:#7A7060;font-style:italic}
.hljs-keyword,.hljs-selector-tag,.hljs-doctag{color:#A0237A;font-weight:bold}
.hljs-string,.hljs-regexp,.hljs-addition{color:#2F7D32}
.hljs-number,.hljs-literal{color:#B5541B}
.hljs-title,.hljs-title.function_,.hljs-section{color:#1F5FAF}
.hljs-title.class_,.hljs-type,.hljs-class .hljs-title{color:#8A5A00}
.hljs-built_in,.hljs-symbol{color:#0B7285}
.hljs-meta,.hljs-meta .hljs-keyword{color:#B8326F}
.hljs-attr,.hljs-attribute,.hljs-variable,.hljs-template-variable,.hljs-property{color:#B23A48}
.hljs-params{color:#5B3D8F}
.hljs-operator,.hljs-punctuation{color:#5A6FA8}
.hljs-deletion{color:#C62828}
.hljs-emphasis{font-style:italic}.hljs-strong{font-weight:bold}
code.lang-plain,code.lang-output,code.lang-text{color:#2F5E3A}
`;

const html = `<!doctype html><html><head><meta charset="utf-8"><title>${path.basename(outPdf, '.pdf')}</title><style>${CSS}</style></head><body>
${cover}${body}
<script>
(() => {
  const R = [
    [/^what it is|^what this (module|capstone|lesson) is|^the idea/i,'what'],[/^analogy/i,'analogy'],
    [/^the problem|^incident|^the brief|^why the obvious fixes fail|^why it needs|^what makes it hard/i,'problem'],
    [/^how it works|^the pattern|^mechanism/i,'how'],[/^walkthrough/i,'walk'],
    [/^you gain/i,'gain'],[/^you pay/i,'pay'],[/^trade-?offs/i,'trade'],[/^when not to use/i,'whennot'],
    [/^real-world/i,'real'],[/^mistakes? that fail/i,'mistake'],
    [/^in the interview|^30-second answer|^likely follow-ups|^interview (one-liner|summary)/i,'interview'],
    [/^self-check/i,'self'],[/^tl;?dr/i,'tldr'],[/^related patterns/i,'related'],
    [/^stage \\d|^decision \\d|^pause and decide|^the final architecture|^debrief|^extend it/i,'stage'],
    [/^requirements|^the failure walk/i,'problem'],[/^the five[- ]questions|^the architecture|^deep dive/i,'how'],[/^the follow-up/i,'interview'],[/^the pattern manifest|^pattern manifest/i,'tldr'],
    [/^must-know/i,'what'],[/^key facts/i,'how'],[/^code to remember/i,'real'],[/^gotchas/i,'mistake'],[/^last-minute checklist|^top \d+ questions/i,'tldr'],[/^how to use this edition/i,'intro'],[/^overview|^the big idea|^in plain english/i,'what'],[/^key terms|^vocabulary/i,'intro'],[/^chapter summary|^summary/i,'tldr'],[/^interview angle|^in an interview/i,'interview'],[/^common (confusion|mistake|pitfall)s?|^watch out/i,'mistake'],[/^compared|^comparison|^versus/i,'trade'],[/^in the real world|^real systems/i,'real'],
    [/^before you start|^four terms|^two facts|^how to read|^module map/i,'intro'],
  ];
  const kind = t => { t = t.trim().replace(/^[^A-Za-z0-9]+/,'').replace(/^\\d+[.)]\\s*/,''); for (const [re,k] of R) if (re.test(t)) return k; };
  document.querySelectorAll('h3,h4').forEach(h => { const k = kind(h.textContent); if (k) h.classList.add('k',k); });
  const boxed = new Set(['analogy','tldr','mistake']);
  document.querySelectorAll('p, blockquote').forEach(el => {
    const first = el.tagName === 'BLOCKQUOTE' ? el.querySelector('p > strong:first-child, strong:first-child') : el.querySelector(':scope > strong:first-child');
    if (!first) return;
    if (el.tagName === 'P' && el.firstChild !== first && el.firstChild.textContent.trim() !== '') return;
    const k = kind(first.textContent); if (!k) return;
    if (el.tagName === 'BLOCKQUOTE' || boxed.has(k) || /^30-second|^interview one-liner/i.test(first.textContent)) el.classList.add('box',k);
    else el.classList.add('lab',k);
  });
  // tag every element after a coloured h3/h4 with its section, until the next heading
  document.querySelectorAll('h3.k,h4.k').forEach(h => {
    const k = [...h.classList].find(c => c !== 'k');
    for (let n = h.nextElementSibling; n && !/^H[1-4]$/.test(n.tagName); n = n.nextElementSibling) {
      n.dataset.sec = k; n.classList.add(k);
      if (k === 'tldr' && /^(UL|OL|TABLE)$/.test(n.tagName)) n.classList.add('box');
    }
  });
  // lists right after a "Likely follow-ups" / "Self-check" label get a matching left rule
  document.querySelectorAll('p.lab').forEach(p => { const n = p.nextElementSibling; if (n && /^(UL|OL)$/.test(n.tagName)) { n.classList.add('box', [...p.classList].find(c => !['lab','k'].includes(c))); } });
})();
</script>
<script>${mermaidJs}</script>
<script>
mermaid.initialize({ startOnLoad:false, theme:'base', themeVariables:{ background:'#F3E5C6', primaryColor:'#EAD8B2', primaryTextColor:'#2E261C', primaryBorderColor:'#3A5BC7', lineColor:'#5A6FA8', secondaryColor:'#EAD8B2', tertiaryColor:'#F6EAD0', noteBkgColor:'#D6C29A', noteTextColor:'#2E261C', actorBkg:'#EAD8B2', actorTextColor:'#2E261C', signalColor:'#3B3024', signalTextColor:'#2E261C' }, securityLevel:'loose', fontFamily:'Monaco, monospace', flowchart:{useMaxWidth:true}, sequence:{useMaxWidth:true} });
window.__errors = [];
(async () => {
  const els = [...document.querySelectorAll('pre.mermaid')];
  for (let i = 0; i < els.length; i++) {
    const el = els[i], src = el.textContent;
    try { const { svg } = await mermaid.render('mm' + i, src); el.outerHTML = svg; }
    catch (e) {
      window.__errors.push({ i, err: String(e.message || e).slice(0, 200), src: src.slice(0, 200) });
      el.outerHTML = '<div class="merr">' + src.replace(/[<&]/g, c => c === '<' ? '&lt;' : '&amp;') + '</div>';
    }
    document.querySelectorAll('[id^="dmm"]').forEach(n => n.remove());
  }

  // colourful diagrams: give each node/actor its own colour
  const PAL=[['#DCE8F7','#3E7CC4'],['#EBDDF7','#8E5BC7'],['#DDF0E2','#2E7D3A'],['#F7DDE3','#C24A63'],['#F6E7B8','#8A5A00'],['#D5EEF2','#0B6B5E'],['#F7E0CC','#9A4F00']];
  document.querySelectorAll('figure.diagram svg').forEach(svg=>{
    let i=0;
    svg.querySelectorAll('g.node, g.classGroup').forEach(g=>{
      const [f,st]=PAL[i++%PAL.length];
      g.querySelectorAll(':scope > rect, :scope > polygon, :scope > circle, :scope > ellipse, :scope > path.basic, :scope > path.label-container, :scope > g > rect, :scope > g > path').forEach(sh=>{
        if(sh.closest('.label')) return; sh.style.fill=f; sh.style.stroke=st; sh.style.strokeWidth='1.5px';
      });
      g.querySelectorAll('.nodeLabel, text, span').forEach(t=>{t.style.color='#1C1712'; if(t.tagName==='text') t.style.fill='#1C1712';});
    });
    const actors={};
    svg.querySelectorAll('rect.actor').forEach(r=>{
      const name=(r.parentNode.textContent||'').trim(); if(!(name in actors)) actors[name]=Object.keys(actors).length;
      const [f,st]=PAL[actors[name]%PAL.length]; r.style.fill=f; r.style.stroke=st; r.style.strokeWidth='1.5px';
    });
    svg.querySelectorAll('rect.note').forEach(r=>{r.style.fill='#F6E7B8'; r.style.stroke='#8A5A00';});
    svg.querySelectorAll('.flowchart-link, path.relation, .messageLine0, .messageLine1, .transition').forEach(e=>{e.style.stroke='#5A6FA8';});
  });
  await Promise.all([...document.images].map(im => im.complete ? 0 : new Promise(r => { im.onload = im.onerror = r; })));
  window.__done = els.length;
})();
</script></body></html>`;

const htmlPath = outPdf.replace(/\.pdf$/, '.html');
fs.writeFileSync(htmlPath, html);
const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new', args: ['--allow-file-access-from-files', ...(process.env.CHROME_PATH ? ['--no-sandbox'] : [])] });
try {
  const page = await browser.newPage();
  await page.goto('file://' + path.resolve(htmlPath), { waitUntil: 'load', timeout: 0 });
  await page.waitForFunction('window.__done !== undefined', { timeout: 600000 });
  const errors = await page.evaluate(() => window.__errors);
  const imgs = await page.evaluate(() => [...document.images].map(i => [i.naturalWidth > 0, i.alt]));
  const band = (inner, h) => `<style>html,body{margin:0;padding:0}#header,#footer{padding:0!important}</style><div style="-webkit-print-color-adjust:exact;print-color-adjust:exact;background:#FBF0D9;width:100%;height:${h};margin:0;display:flex;align-items:center;justify-content:center;font-family:Monaco,monospace;font-size:7px;color:#7A6A52">${inner}</div>`;
  await page.pdf({
    path: outPdf, timeout: 0, format: 'A4', printBackground: true, displayHeaderFooter: true,
    headerTemplate: band('', '14mm'),
    footerTemplate: band(`${path.basename(outPdf, '.pdf')} · <span class="pageNumber"></span>/<span class="totalPages"></span>`, '15mm'),
    margin: { top: '14mm', bottom: '15mm', left: '0', right: '0' },
  });
  const broken = imgs.filter(([ok]) => !ok).map(([, a]) => a);
  console.log(`wrote ${outPdf} (${(fs.statSync(outPdf).size / 1e6).toFixed(1)} MB) — mermaid: ${errors.length} errors, images: ${imgs.length - broken.length}/${imgs.length} ok`);
  if (errors.length) console.log(JSON.stringify(errors, null, 1));
  if (broken.length) console.log('broken images:', broken);
} finally {
  await browser.close();
  fs.unlinkSync(htmlPath);
}
