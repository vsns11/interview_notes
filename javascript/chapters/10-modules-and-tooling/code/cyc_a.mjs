// tools_run: skip — circular import demo (a <-> b), unsafe version
import { b } from './cyc_b.mjs';
export const a = 'A';
console.log('[cyc_a] sees', b);
