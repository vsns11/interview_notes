// How Tailwind v4 works: it scans your source for class names and generates ONLY the CSS for those.
import { execFileSync } from 'node:child_process';
import { mkdtempSync, writeFileSync, readFileSync, rmSync } from 'node:fs';
import { createRequire } from 'node:module';
import { tmpdir } from 'node:os';
import path from 'node:path';

const dir = mkdtempSync(path.join(tmpdir(), 'tw-'));
// A "component" using a few utilities, variants and one arbitrary value:
writeFileSync(path.join(dir, 'Badge.jsx'),
  `export const Badge = () => <span className="px-2 font-semibold hover:bg-blue-700 md:text-lg dark:text-white w-[37px]">new</span>;`);
// Only the utilities layer, so the output stays short (a real app imports "tailwindcss" = theme + preflight + utilities).
writeFileSync(path.join(dir, 'in.css'),
  `@import "tailwindcss/theme.css" layer(theme);\n@import "tailwindcss/utilities.css" layer(utilities);\n@source "./Badge.jsx";`);

const require = createRequire(import.meta.url);
const cli = path.join(path.dirname(require.resolve('@tailwindcss/cli/package.json')), 'dist/index.mjs');
execFileSync(process.execPath, [cli, '-i', path.join(dir, 'in.css'), '-o', path.join(dir, 'out.css')],
  { cwd: dir, stdio: 'ignore' });
const css = readFileSync(path.join(dir, 'out.css'), 'utf8');
const start = css.indexOf('@layer utilities');
const utilities = css.slice(start, css.indexOf('\n}\n', start) + 2);     // just the utilities block
console.log(utilities.trim());
rmSync(dir, { recursive: true });
