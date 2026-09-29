// A tiny model of npm's semver ranges (^, ~, exact, x) — for learning, not a replacement for the `semver` package.
// Pre-release tags (1.0.0-beta.1) and ||, - ranges are deliberately left out.
const parse = (v) => v.split('.').map(Number);
const cmp = (a, b) => { for (let i = 0; i < 3; i++) if (a[i] !== b[i]) return a[i] - b[i]; return 0; };

function toBounds(range) {
  const op = range.match(/^[\^~]/)?.[0] ?? '';
  const parts = range.slice(op.length).split('.');
  const given = parts.filter((p) => p !== 'x' && p !== '*').length;       // how many numbers were given
  const [M = 0, m = 0, p = 0] = parts.map((x) => (x === 'x' || x === '*' ? 0 : Number(x)));
  const low = [M, m, p];
  let high;
  if (op === '^') {                    // don't change the left-most non-zero part
    if (M > 0 || given === 1) high = [M + 1, 0, 0];
    else if (m > 0 || given === 2) high = [0, m + 1, 0];
    else high = [0, 0, p + 1];
  } else if (op === '~') {             // allow patch changes (or minor, if only major given)
    high = given === 1 ? [M + 1, 0, 0] : [M, m + 1, 0];
  } else {                             // exact or x-range
    high = given === 3 ? [M, m, p + 1] : given === 2 ? [M, m + 1, 0] : given === 1 ? [M + 1, 0, 0] : [Infinity, 0, 0];
  }
  return { low, high };
}
const satisfies = (version, range) => {
  const { low, high } = toBounds(range);
  const v = parse(version);
  return cmp(v, low) >= 0 && cmp(v, high) < 0;
};

const ranges = ['^1.2.3', '~1.2.3', '1.2.3', '1.x', '^0.2.3', '^0.0.3', '~1'];
const versions = ['1.2.3', '1.2.9', '1.3.0', '1.9.9', '2.0.0', '0.2.5', '0.3.0', '0.0.3', '0.0.4'];
for (const r of ranges) {
  const { low, high } = toBounds(r);
  const ok = versions.filter((v) => satisfies(v, r));
  console.log(`${r.padEnd(7)} >=${low.join('.')} <${high.join('.')}`.padEnd(30), 'matches:', ok.join(', ') || '(none)');
}
