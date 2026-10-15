// Render a workshop script (JSON) into one 1920x1080 PNG per narration step.
// Usage: node render_slides.mjs <script.json> <outDir>
// Writes <outDir>/frames/<scene>_<step>.png and <outDir>/frames.json (ordered list).
// Uses puppeteer-core + mermaid from ../system-design-patterns/tools/node_modules.
import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';

const here = path.dirname(new URL(import.meta.url).pathname);
const mods = path.join(here, '..', 'system-design-patterns', 'tools', 'node_modules');
const require = createRequire(path.join(mods, 'x.js'));
const puppeteer = require('puppeteer-core');
const mermaidJs = fs.readFileSync(path.join(mods, 'mermaid/dist/mermaid.min.js'), 'utf8');

const [scriptPath, outDir] = process.argv.slice(2);
const script = JSON.parse(fs.readFileSync(scriptPath, 'utf8'));
const framesDir = path.join(outDir, 'frames');
fs.mkdirSync(framesDir, { recursive: true });

const esc = s => String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
// **bold** and `code` inside slide text
const fmt = s => esc(s).replace(/\*\*(.+?)\*\*/g, '<b>$1</b>').replace(/`(.+?)`/g, '<code>$1</code>');

const css = `
*{box-sizing:border-box;margin:0;padding:0}
html,body{width:1920px;height:1080px;background:#FBF0D9;color:#2E261C;font-family:'DejaVu Sans',Arial,sans-serif;overflow:hidden}
.frame{position:absolute;inset:0;padding:80px 120px 110px;display:flex;flex-direction:column}
.kicker{font-size:30px;letter-spacing:2px;text-transform:uppercase;color:#8A6D3B;margin-bottom:14px;font-weight:600}
h1{font-size:64px;line-height:1.12;color:#2B3F8C;margin-bottom:34px}
ul{list-style:none}
li{font-size:44px;line-height:1.3;margin:0 0 26px 0;padding-left:56px;position:relative}
li::before{content:'';position:absolute;left:8px;top:20px;width:20px;height:20px;border-radius:50%;background:#3A5BC7}
li.hidden{visibility:hidden}
li.new{color:#1D2E70}
b{color:#7A2E12}
code{font-family:'DejaVu Sans Mono',monospace;background:#EAD8B2;padding:2px 10px;border-radius:8px;font-size:.9em}
.cols{display:flex;gap:70px}
.col{flex:1;background:#F3E5C6;border-radius:24px;padding:36px 40px}
.col h2{font-size:44px;color:#7A2E12;margin-bottom:26px}
.col li{font-size:38px}
table{border-collapse:collapse;font-size:38px;width:100%}
th{background:#3A5BC7;color:#fff;text-align:left;padding:16px 22px}
td{padding:14px 22px;border-bottom:2px solid #E2CFA6}
tr.hidden td{visibility:hidden}
.diagram{display:flex;justify-content:center;align-items:center;flex:1;min-height:0}
.diagram svg{max-width:1680px;width:auto;height:auto}
.caption{font-size:34px;color:#5A4A33;text-align:center;margin-top:14px}
.big{font-size:120px;font-weight:700;color:#2B3F8C;text-align:center;margin-top:120px;line-height:1.15}
.title-wrap{display:flex;flex-direction:column;justify-content:center;height:100%}
.title-wrap .series{font-size:40px;color:#8A6D3B;letter-spacing:3px;text-transform:uppercase}
.title-wrap h1{font-size:104px;margin:24px 0}
.title-wrap .sub{font-size:48px;color:#5A4A33}
.section{display:flex;flex-direction:column;justify-content:center;height:100%}
.section h1{font-size:96px}
.quiz li::before{background:#C9852B;border-radius:6px}
.footer{position:absolute;left:120px;right:120px;bottom:40px;display:flex;justify-content:space-between;font-size:26px;color:#8A6D3B}
.bar{position:absolute;left:0;bottom:0;height:10px;background:#3A5BC7}
`;

function itemsFor(sc) {
  if (sc.layout === 'twocol') return [...(sc.left?.bullets || []), ...(sc.right?.bullets || [])];
  if (sc.layout === 'table') return sc.table?.rows || [];
  return sc.bullets || [];
}

function body(sc, reveal) {
  const show = i => (reveal == null || i < reveal);
  const li = (b, i, n) => `<li class="${show(i) ? (i === reveal - 1 ? 'new' : '') : 'hidden'}">${fmt(b)}</li>`;
  const kick = sc.kicker ? `<div class="kicker">${fmt(sc.kicker)}</div>` : '';
  switch (sc.layout) {
    case 'title':
      return `<div class="title-wrap"><div class="series">${fmt(script.series)} · Episode ${script.episode}</div><h1>${fmt(sc.title || script.title)}</h1><div class="sub">${fmt(sc.caption || script.subtitle || '')}</div></div>`;
    case 'section':
      return `<div class="section">${kick}<h1>${fmt(sc.title)}</h1>${sc.caption ? `<div class="sub" style="font-size:44px;color:#5A4A33">${fmt(sc.caption)}</div>` : ''}</div>`;
    case 'twocol': {
      const L = sc.left?.bullets || [];
      const R = sc.right?.bullets || [];
      return `${kick}<h1>${fmt(sc.title)}</h1><div class="cols"><div class="col"><h2>${fmt(sc.left?.heading)}</h2><ul>${L.map((b, i) => li(b, i)).join('')}</ul></div><div class="col"><h2>${fmt(sc.right?.heading)}</h2><ul>${R.map((b, i) => li(b, i + L.length)).join('')}</ul></div></div>`;
    }
    case 'table': {
      const t = sc.table || { head: [], rows: [] };
      return `${kick}<h1>${fmt(sc.title)}</h1><table><tr>${t.head.map(h => `<th>${fmt(h)}</th>`).join('')}</tr>${t.rows.map((r, i) => `<tr class="${show(i) ? '' : 'hidden'}">${r.map(c => `<td>${fmt(c)}</td>`).join('')}</tr>`).join('')}</table>${sc.caption ? `<div class="caption">${fmt(sc.caption)}</div>` : ''}`;
    }
    case 'diagram':
      return `${kick}<h1>${fmt(sc.title)}</h1><div class="diagram"><pre class="mermaid">${esc(sc.mermaid)}</pre></div>${sc.caption ? `<div class="caption">${fmt(sc.caption)}</div>` : ''}`;
    case 'number':
      return `${kick}<h1>${fmt(sc.title)}</h1><div class="big">${fmt(sc.big)}</div>${sc.caption ? `<div class="caption" style="margin-top:60px">${fmt(sc.caption)}</div>` : ''}`;
    case 'quiz':
      return `<div class="kicker">${fmt(sc.kicker || 'Pause and think')}</div><h1>${fmt(sc.title)}</h1><ul class="quiz">${(sc.bullets || []).map((b, i) => `<li>${fmt(b)}</li>`).join('')}</ul>`;
    default: // bullets, recap
      return `${kick}<h1>${fmt(sc.title)}</h1><ul>${(sc.bullets || []).map((b, i) => li(b, i)).join('')}</ul>`;
  }
}

const browser = await puppeteer.launch({ executablePath: process.env.CHROME_PATH, args: ['--no-sandbox'] });
const page = await browser.newPage();
await page.setViewport({ width: 1920, height: 1080 });
const frames = [];
const total = script.scenes.length;
let errors = 0;
for (const [si, sc] of script.scenes.entries()) {
  const nItems = itemsFor(sc).length;
  for (const [ki, st] of (sc.steps || []).entries()) {
    const reveal = ['bullets', 'recap', 'table', 'twocol'].includes(sc.layout || 'bullets')
      ? Math.min(st.reveal ?? nItems, nItems) : null;
    if (sc.mermaid && /^\s*(flowchart|graph)\s+LR/.test(sc.mermaid) && !/subgraph/.test(sc.mermaid)) {
      const ids = new Set([...sc.mermaid.matchAll(/([A-Za-z0-9_]+)\s*[\[\(\{]/g)].map(m => m[1]));
      if (ids.size >= 4) sc.mermaid = sc.mermaid.replace(/^(\s*(?:flowchart|graph)\s+)LR/, '$1TB');
    }
    const html = `<!doctype html><html><head><meta charset="utf-8"><style>${css}</style></head><body>
<div class="frame">${body(sc, reveal)}</div>
<div class="footer"><span>${fmt(script.series)} · Ep ${script.episode}: ${fmt(script.title)}</span><span>${si + 1} / ${total}</span></div>
<div class="bar" style="width:${((si + 1) / total * 100).toFixed(2)}%"></div>
<script>${mermaidJs}</script>
<script>
mermaid.initialize({startOnLoad:false,theme:'base',themeVariables:{fontSize:'28px',background:'#FBF0D9',primaryColor:'#EAD8B2',primaryTextColor:'#2E261C',primaryBorderColor:'#3A5BC7',lineColor:'#5A6FA8',secondaryColor:'#EAD8B2',tertiaryColor:'#F6EAD0',actorBkg:'#EAD8B2',signalColor:'#3B3024'},securityLevel:'loose',fontFamily:'DejaVu Sans',flowchart:{useMaxWidth:false},sequence:{useMaxWidth:false}});
window.done=(async()=>{for(const [i,el] of [...document.querySelectorAll('pre.mermaid')].entries()){let src=el.textContent;try{let {svg}=await mermaid.render('m'+i,src);
const m=svg.match(/viewBox="[-\d.]+ [-\d.]+ ([\d.]+) ([\d.]+)"/);const vb=m?{width:+m[1],height:+m[2]}:null;
if(vb&&vb.width/vb.height>3.2&&/^\s*(flowchart|graph)\s+LR/.test(src)){src=src.replace(/^(\s*(?:flowchart|graph)\s+)LR/,'$1TB');svg=(await mermaid.render('mt'+i,src)).svg}
el.outerHTML=svg}catch(e){el.outerHTML='<div class="caption">diagram error</div>';window.merr=String(e)}}})();
</script></body></html>`;
    await page.setContent(html, { waitUntil: 'load' });
    await page.evaluate(() => window.done);
    const merr = await page.evaluate(() => window.merr || null);
    if (merr) { errors++; console.error(`mermaid error in ${sc.id}: ${merr.slice(0, 200)}`); }
    // scale SVG diagrams up to fill the box
    await page.evaluate(() => {
      for (const s of document.querySelectorAll('.diagram svg')) {
        const vb = s.viewBox.baseVal; if (!vb || !vb.width) continue;
        const box = s.parentElement.getBoundingClientRect(); const k = Math.min((box.width - 20) / vb.width, (box.height - 10) / vb.height, 2.2);
        s.setAttribute('width', vb.width * k); s.setAttribute('height', vb.height * k);
        s.style.maxWidth = 'none';
      }
    });
    const file = path.join(framesDir, `${sc.id}_${String(ki).padStart(2, '0')}.png`);
    await page.screenshot({ path: file });
    frames.push({ scene: sc.id, step: ki, file, say: st.say });
  }
}
await browser.close();
fs.writeFileSync(path.join(outDir, 'frames.json'), JSON.stringify(frames, null, 1));
console.log(`rendered ${frames.length} frames for ${total} scenes — mermaid errors: ${errors}`);
