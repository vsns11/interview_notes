// tools_run: skip  (library used by 02_flex_math.mjs and 03_flex_measure.mjs)
// A small, readable version of the CSS Flexbox spec's "Resolve the flexible lengths" algorithm
// (CSS Flexible Box Layout Level 1, section 9.7) for ONE line of items with no padding/border.
//   items: [{ name, basis, grow, shrink, min = 0, max = Infinity }]
//   Returns final main sizes; if log is given, it is called with a line of text for every step.
export function resolveFlex(container, items, { gap = 0, log = () => {} } = {}) {
  const gaps = gap * (items.length - 1);
  const it = items.map(i => ({ min: 0, max: Infinity, ...i }));
  for (const i of it) i.hypo = Math.min(Math.max(i.basis, i.min), i.max);   // clamp basis by min/max
  const used = it.reduce((s, i) => s + i.hypo, 0) + gaps;
  const growing = used < container;
  log(`space used by hypothetical sizes = ${it.map(i => i.hypo).join(' + ')}${gap ? ` + gaps ${gaps}` : ''} = ${used}`);
  log(`container = ${container}  ->  ${growing ? 'free space, use flex-grow' : 'overflow, use flex-shrink'}`);

  // items that can't flex in this direction are frozen at their hypothetical size
  for (const i of it) {
    const factor = growing ? i.grow : i.shrink;
    if (factor === 0 || (growing && i.basis > i.hypo) || (!growing && i.basis < i.hypo)) { i.frozen = true; i.size = i.hypo; }
  }
  const initialFree = container - gaps - it.reduce((s, i) => s + (i.frozen ? i.size : i.basis), 0);
  for (let round = 1; it.some(i => !i.frozen); round++) {
    const open = it.filter(i => !i.frozen);
    let free = container - gaps - it.filter(i => i.frozen).reduce((s, i) => s + i.size, 0)
                                  - open.reduce((s, i) => s + i.basis, 0);
    const factorSum = open.reduce((s, i) => s + (growing ? i.grow : i.shrink), 0);
    if (factorSum < 1 && Math.abs(initialFree * factorSum) < Math.abs(free)) {   // factors summing below 1
      free = initialFree * factorSum;                                            // only use that fraction
      log(`flex factors sum to ${factorSum} < 1, so only ${factorSum} of the free space is used`);
    }
    log(`round ${round}: free space = ${round2(free)} shared by ${open.map(i => i.name).join(', ')}`);
    if (growing) {
      const sum = open.reduce((s, i) => s + i.grow, 0);
      for (const i of open) { i.target = i.basis + free * i.grow / sum;
        log(`  ${i.name}: ${i.basis} + ${round2(free)} × ${i.grow}/${sum} = ${round2(i.target)}`); }
    } else {
      const sum = open.reduce((s, i) => s + i.shrink * i.basis, 0);           // scaled shrink factors
      for (const i of open) { i.target = i.basis + free * (i.shrink * i.basis) / sum;
        log(`  ${i.name}: ${i.basis} − ${round2(-free)} × (${i.shrink}×${i.basis})/${sum} = ${round2(i.target)}`); }
    }
    let violation = 0;                                                        // clamp to min/max
    for (const i of open) {
      i.clamped = Math.min(Math.max(i.target, i.min), i.max);
      if (i.clamped !== i.target) log(`  ${i.name} violates its ${i.clamped > i.target ? 'min' : 'max'} -> clamped to ${i.clamped}`);
      violation += i.clamped - i.target;
    }
    for (const i of open) {
      const freeze = violation === 0 || (violation > 0 ? i.clamped > i.target : i.clamped < i.target);
      if (freeze) { i.frozen = true; i.size = i.clamped; }
    }
  }
  return Object.fromEntries(it.map(i => [i.name, i.size]));
}
export const round2 = n => Math.round(n * 1000) / 1000;
