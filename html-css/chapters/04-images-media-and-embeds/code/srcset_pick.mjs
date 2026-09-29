// A simplified model of how a browser picks a file from srcset + sizes.
// Real browsers may choose differently (e.g. prefer a cached larger file, or a smaller one on
// slow networks) -- the spec lets them. This follows the common "smallest file that is dense enough" rule.
// Run: node srcset_pick.mjs

const srcset = 'hills-400.jpg 400w, hills-800.jpg 800w, hills-1600.jpg 1600w';
const sizes = '(max-width: 600px) 100vw, (max-width: 1000px) 50vw, 400px';

// "hills-400.jpg 400w" -> { url, w }
const candidates = srcset.split(',').map(s => {
  const [url, d] = s.trim().split(/\s+/);
  return { url, w: parseInt(d) };
});

// Evaluate sizes: first media condition that matches wins; the last entry has no condition.
function slotWidth(sizesAttr, viewport) {
  for (const part of sizesAttr.split(',').map(s => s.trim())) {
    const m = part.match(/^\((max|min)-width:\s*(\d+)px\)\s+(.+)$/);
    const length = m ? m[3] : part;
    const ok = !m || (m[1] === 'max' ? viewport <= +m[2] : viewport >= +m[2]);
    if (ok) return length.endsWith('vw') ? (parseFloat(length) / 100) * viewport : parseFloat(length);
  }
}

function pick(viewport, dpr) {
  const slot = slotWidth(sizes, viewport);             // CSS px the image will occupy
  const needed = slot * dpr;                           // device pixels needed to look sharp
  const withDensity = candidates.map(c => ({ ...c, density: c.w / slot }))
    .sort((a, b) => a.w - b.w);
  const chosen = withDensity.find(c => c.density >= dpr) ?? withDensity.at(-1);
  return { slot, needed, chosen };
}

console.log('sizes =', JSON.stringify(sizes));
console.log('viewport  dpr  slot(CSS px)  needs(px)  chosen');
for (const vw of [360, 768, 1280])
  for (const dpr of [1, 2, 3]) {
    const { slot, needed, chosen } = pick(vw, dpr);
    console.log(`${String(vw).padStart(8)}  ${dpr}x   ${slot.toFixed(0).padStart(11)}  ${needed.toFixed(0).padStart(9)}  ${chosen.url}`);
  }
