// A model of the CSS cascade "sort" (CSS Cascade Level 5), our own simplified implementation.
// Given every declaration of ONE property that applies to ONE element, the winner is found by
// comparing, in this order:
//   1. origin + importance      2. element-attached (style="")      3. cascade layer
//   4. specificity              5. order of appearance (later wins)
// (Shadow-DOM "context" is left out.)
// Run: node cascade.mjs
import { specificity } from './specificity_lib.mjs';

// Higher rank = stronger. Normal declarations: user-agent < user < author.
// !important reverses the origins: important author < important user < important user-agent.
const ORIGIN_RANK = {
  'normal ua': 1, 'normal user': 2, 'normal author': 3,
  'important author': 5, 'important user': 6, 'important ua': 7,
};  // (CSS animations sit at 4 and transitions at 8 -- not modelled here)

function layerRank(decl, layerOrder) {
  // Normal: unlayered beats every layer, later layers beat earlier ones.
  // Important: reversed -- earlier layers beat later ones, and any layer beats unlayered.
  const n = layerOrder.length;
  const pos = decl.layer ? layerOrder.indexOf(decl.layer) : n;     // unlayered = "last layer"
  return decl.important ? n - pos : pos;
}

function key(decl, layerOrder) {
  const origin = ORIGIN_RANK[`${decl.important ? 'important' : 'normal'} ${decl.origin}`];
  const attached = decl.inline ? 1 : 0;
  const spec = decl.inline ? [0, 0, 0] : specificity(decl.selector);
  return [origin, attached, layerRank(decl, layerOrder), ...spec, decl.order];
}

const cmp = (a, b) => { for (let i = 0; i < a.length; i++) if (a[i] !== b[i]) return a[i] - b[i]; return 0; };

function cascade(title, decls, layerOrder = []) {
  console.log(`\n${title}`);
  const ranked = decls.map((d, order) => ({ ...d, order }))
    .map(d => ({ d, k: key(d, layerOrder) }))
    .sort((x, y) => cmp(y.k, x.k));
  ranked.forEach(({ d, k }, i) => {
    const where = d.inline ? 'style=""' : d.selector;
    const flags = [d.origin, d.important && '!important', d.layer && `@layer ${d.layer}`].filter(Boolean).join(', ');
    console.log(`  ${i === 0 ? 'WIN ' : '    '}${`color: ${d.value}`.padEnd(15)} ${where.padEnd(22)} [${flags}]  key=${k.join('.')}`);
  });
}

// Element: <p id="intro" class="lead" style="color: purple">
cascade('A. Specificity decides (same origin, no layers):', [
  { origin: 'author', selector: 'p', value: 'black' },
  { origin: 'author', selector: '#intro', value: 'blue' },
  { origin: 'author', selector: '.lead', value: 'green' },
]);

cascade('B. Equal specificity -> the later rule wins:', [
  { origin: 'author', selector: '.lead', value: 'green' },
  { origin: 'author', selector: '.lead', value: 'orange' },
]);

cascade('C. Inline style beats any selector; !important beats inline:', [
  { origin: 'author', selector: '#intro', value: 'blue' },
  { origin: 'author', inline: true, value: 'purple' },
  { origin: 'author', selector: 'p', value: 'red', important: true },
]);

cascade('D. Origins: the browser default loses to author CSS, but user !important beats everything else:', [
  { origin: 'ua', selector: 'p', value: 'canvastext' },
  { origin: 'author', selector: '#intro', value: 'blue', important: true },
  { origin: 'user', selector: 'p', value: 'yellow', important: true },
]);

cascade('E. Layers (@layer reset, base, components): unlayered wins even with lower specificity:', [
  { origin: 'author', selector: '#intro', value: 'gray', layer: 'reset' },
  { origin: 'author', selector: '.lead', value: 'teal', layer: 'components' },
  { origin: 'author', selector: 'p', value: 'navy' },
], ['reset', 'base', 'components']);

cascade('F. ...but with !important the layer order flips: the earliest layer wins:', [
  { origin: 'author', selector: 'p', value: 'gray', layer: 'reset', important: true },
  { origin: 'author', selector: '#intro', value: 'teal', layer: 'components', important: true },
  { origin: 'author', selector: '#intro', value: 'navy', important: true },
], ['reset', 'base', 'components']);
