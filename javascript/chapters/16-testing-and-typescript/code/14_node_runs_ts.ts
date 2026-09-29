// Run directly with Node 24:  node 14_node_runs_ts.ts   (no build step)
// Rules for Node's type stripping: full file extensions in imports, `import type` for types,
// and only "erasable" syntax (no enum, namespace with code, parameter properties).
import type { Shape } from './shapes.ts';
import { area } from './shapes.ts';

const shapes: Shape[] = [
  { kind: 'rect', w: 2, h: 5 },
  { kind: 'circle', r: 1 },
];
const total: number = shapes.reduce((sum, s) => sum + area(s), 0);
console.log('total area:', total.toFixed(3));
console.log('shapes:', shapes.map((s): string => s.kind).join(', '));
