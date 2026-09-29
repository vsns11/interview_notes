// tools_run: skip — reads `a` while cyc_a has not finished evaluating
import { a } from './cyc_a.mjs';
export const b = `B knows ${a}`;   // a is still in its TDZ here -> ReferenceError
