// The worked grow/shrink calculations from the chapter, done step by step by flex_algorithm.mjs.
// (03_flex_measure.mjs then checks every answer against real Chrome.)
import { resolveFlex, round2 } from './flex_algorithm.mjs';

const show = (title, container, items, opts = {}) => {
  console.log(`\n== ${title}`);
  const r = resolveFlex(container, items, { ...opts, log: s => console.log('  ' + s) });
  console.log('  result: ' + Object.entries(r).map(([k, v]) => `${k}=${round2(v)}`).join('  '));
};

show('G1 grow', 600, [
  { name: 'A', basis: 100, grow: 1, shrink: 1 },
  { name: 'B', basis: 150, grow: 2, shrink: 1 },
  { name: 'C', basis: 50,  grow: 1, shrink: 1 }]);
show('G2 shrink', 400, [
  { name: 'A', basis: 300, grow: 0, shrink: 1 },
  { name: 'B', basis: 200, grow: 0, shrink: 1 },
  { name: 'C', basis: 100, grow: 0, shrink: 2 }]);
show('G3 grow with gap 20', 600, [
  { name: 'A', basis: 100, grow: 1, shrink: 1 },
  { name: 'B', basis: 150, grow: 2, shrink: 1 },
  { name: 'C', basis: 50,  grow: 1, shrink: 1 }], { gap: 20 });
show('G4 flex 1 1 0, A max 100', 600, [
  { name: 'A', basis: 0, grow: 1, shrink: 1, max: 100 },
  { name: 'B', basis: 0, grow: 1, shrink: 1 },
  { name: 'C', basis: 0, grow: 1, shrink: 1 }]);
show('G7 grow factors summing to 0.5', 600, [
  { name: 'A', basis: 100, grow: 0.25, shrink: 1 },
  { name: 'B', basis: 100, grow: 0.25, shrink: 1 }]);
