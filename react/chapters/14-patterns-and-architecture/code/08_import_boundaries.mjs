// A tiny architecture check for a feature-based folder structure (what eslint-plugin-boundaries or
// no-restricted-imports enforce in real projects). Rules:
//   1. app/      may import anything.
//   2. features/ may import shared/ and OTHER features only through their index (public API).
//   3. shared/   may not import features/ or app/.
import fs from 'node:fs';
import path from 'node:path';

const SRC = path.resolve('feature-app/src');
const files = [];
console.log('src/');
(function walk(dir, depth) {                                           // print the tree while collecting files
  for (const e of fs.readdirSync(dir, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name))) {
    const p = path.join(dir, e.name);
    console.log('  ' + '  '.repeat(depth) + e.name + (e.isDirectory() ? '/' : ''));
    e.isDirectory() ? walk(p, depth + 1) : files.push(p);
  }
})(SRC, 0);

const layerOf = (rel) => rel.split(path.sep)[0];                      // app | features | shared
const featureOf = (rel) => rel.startsWith('features') ? rel.split(path.sep)[1] : null;

const problems = [];
for (const f of files) {
  const from = path.relative(SRC, f);
  const src = fs.readFileSync(f, 'utf8');
  for (const [, spec] of src.matchAll(/from\s+'(\.[^']+)'/g)) {       // relative imports only
    const target = path.relative(SRC, path.resolve(path.dirname(f), spec));
    const [fl, tl] = [layerOf(from), layerOf(target)];
    if (fl === 'shared' && tl !== 'shared') problems.push([from, spec, 'shared/ must not import from ' + tl + '/']);
    if (fl === 'features' && tl === 'app') problems.push([from, spec, 'features must not import app/']);
    if (fl === 'features' && tl === 'features' && featureOf(from) !== featureOf(target)
        && target.split(path.sep).length > 2) {                          // deeper than features/<name>
      problems.push([from, spec, `deep import into feature "${featureOf(target)}" (use its index)`]);
    }
  }
}
console.log(`\n${problems.length} boundary violation(s):`);
for (const [from, spec, why] of problems) console.log(`  ${from}\n    imports '${spec}' -> ${why}`);
