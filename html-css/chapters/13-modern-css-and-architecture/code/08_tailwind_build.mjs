// A real Tailwind CSS v4 build: scan 08_tailwind.html, generate only the utilities it uses.
import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const cli = path.join(process.env.NODE_PATH.split(path.delimiter)[0], '@tailwindcss/cli/dist/index.mjs');
const run = args => execFileSync(process.execPath, [cli, ...args], { stdio: ['ignore', 'pipe', 'pipe'] });
run(['-i', '08_tailwind.css', '-o', '08_tailwind.out.css']);
run(['-i', '08_tailwind.css', '-o', '.min.css', '--minify']);

const css = fs.readFileSync('08_tailwind.out.css', 'utf8');
const html = fs.readFileSync('08_tailwind.html', 'utf8');
const used = new Set([...html.matchAll(/class="([^"]+)"/g)].flatMap(m => m[1].split(/\s+/)));
const rules = [...css.matchAll(/^\s+(\.[^\s{]+)\s*\{/gm)].map(m => m[1]);
console.log(`classes used in the HTML: ${used.size}`);
console.log(`utility rules generated:   ${rules.length}`);
console.log(`output: ${css.length} bytes (${fs.statSync('.min.css').size} minified)`);
console.log(`top-level at-rules: ${[...css.matchAll(/^@(layer [\w, .-]+|property [\w-]+)/gm)].map(m => m[1]).slice(0, 7).join(" | ")} ...`);
console.log(`theme variables emitted: ${[...css.matchAll(/^\s+(--color-[\w-]+):/gm)].map(m => m[1]).join(' ')}`);
const show = sel => { const i = css.indexOf(sel); return css.slice(css.lastIndexOf('\n', i) + 1, css.indexOf('}', i) + 1); };
console.log('\n' + ['.p-3\\.5 {', '.bg-brand {', '.w-\\[170px\\] {', '.hover\\:bg-brand-dark:hover {'].map(show).join('\n'));
fs.rmSync('.min.css');
