// tools_run: skip — circular import, safe version: only use the import inside functions
import { describeB } from './cyc_safe_b.mjs';
export function nameA() { return 'A'; }       // function declarations are initialized early
export const result = describeB();
