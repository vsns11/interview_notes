// TypeScript basics: annotations, inference, object types, unions, any vs unknown
// Run: node 10_ts_basics.ts   (Node strips the types)   Check: tsc --noEmit

// 1) Primitive annotations - and inference (no annotation needed when obvious)
let title: string = 'Testing & TypeScript';
let pages = 30;                          // inferred: number
const lang = 'ts';                       // inferred: the LITERAL type "ts" (const)
const big: bigint = 10n ** 20n;
let nothing: null = null;
console.log('1)', typeof title, typeof pages, lang, big, nothing);

// 2) Arrays and tuples
const scores: number[] = [90, 72, 85];
const names: Array<string> = ['Ana', 'Bo'];
const point: [x: number, y: number] = [3, 4];          // tuple: fixed length & types
console.log('2)', scores.length, names.join('+'), Math.hypot(...point));

// 3) Object types: type alias vs interface; optional and readonly properties
type User = {
  readonly id: number;
  name: string;
  email?: string;                        // optional: string | undefined
};
interface Product {
  sku: string;
  price: number;
}
interface Product { tags: string[] }     // interfaces MERGE (declaration merging)
const u: User = { id: 1, name: 'Ana' };
const p: Product = { sku: 'pen', price: 150, tags: ['office'] };
console.log('3)', u.email ?? '(no email)', p.tags[0]);

// 4) Functions: parameter & return types, optional/default params, void
function greet(user: User, greeting = 'Hello'): string {
  return `${greeting}, ${user.name}!`;
}
const log = (msg: string): void => { console.log('4)', msg); };
log(greet(u));
log(greet({ id: 2, name: 'Bo' }, 'Hi'));

// 5) Union and literal types
type Status = 'draft' | 'published' | 'archived';
function badge(s: Status): string {
  return s === 'published' ? '[LIVE]' : `[${s.toUpperCase()}]`;
}
let id: number | string = 42;
id = 'A-42';                             // still OK: number | string
console.log('5)', badge('published'), badge('draft'), id);

// 6) any vs unknown: unknown forces you to check before use
function parse(json: string): unknown {
  return JSON.parse(json);
}
const data = parse('{"count": 3}');
if (typeof data === 'object' && data !== null && 'count' in data && typeof data.count === 'number') {
  console.log('6) count + 1 =', data.count + 1);
}

// 7) Type assertions and the non-null assertion - you override the checker (use sparingly)
const maybe = scores.find((n) => n > 80);           // number | undefined
console.log('7)', (maybe as number).toFixed(1), maybe!.toFixed(0));

// 8) Types disappear at runtime: this is plain JS after stripping
console.log('8) typeof User at runtime:', typeof (globalThis as any).User);
