// tools_run: skip — regenerates every PNG in ../img (~20 s):  node make_figures.mjs
// (run 08_tailwind_build.mjs and 09_css_modules.mjs first: they produce the CSS those pages load)
import { shot, sheet } from './measure.mjs';
import fs from 'node:fs';

const save = async (file, out, opts) => { fs.writeFileSync(out, await shot(file, opts)); console.log('wrote', out); };
await save('03_has.html', '../img/03_has.png', { width: 760, height: 100 });
await save('05_scope.html', '../img/05_scope.png', { width: 520, height: 100 });
await save('06_logical.html', '../img/06_logical.png', { width: 760, height: 100 });

const four = [['07_bem.html', 'BEM (plain CSS)'], ['08_tailwind.html', 'Tailwind CSS v4 utilities'],
              ['09_css_modules.html', 'CSS Modules (Lightning CSS)'], ['10_css_in_js.html', 'runtime CSS-in-JS (mini)']];
const pngs = [];
for (const [f, label] of four) pngs.push({ png: await shot(f, { width: 390, height: 60 }), label });
await sheet(pngs, '../img/07_architecture.png', { width: 840 });

const themes = [];
for (const os of ['light', 'dark'])
  themes.push({ png: await shot('13_theming.html', { width: 400, height: 60, media: [{ name: 'prefers-color-scheme', value: os }] }),
                label: `OS preference: ${os} (setting "System")` });
await sheet(themes, '../img/13_theming.png', { width: 860 });
