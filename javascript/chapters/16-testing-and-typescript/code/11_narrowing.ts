// Narrowing: how TypeScript refines a union type inside if/switch branches

// 1) typeof, truthiness, equality
function describe(x: string | number | null): string {
  if (x === null) return 'nothing';
  if (typeof x === 'string') return `text of length ${x.length}`;   // x: string here
  return `number ${x.toFixed(2)}`;                                    // x: number here
}
console.log('1)', describe('hey'), '|', describe(3.14159), '|', describe(null));

// 2) instanceof and "in"
class HttpError extends Error {
  status: number;
  constructor(status: number) { super(`HTTP ${status}`); this.status = status; }
}
function statusOf(e: Error | { code: string }): string {
  if (e instanceof HttpError) return `http ${e.status}`;
  if ('code' in e) return `code ${e.code}`;
  return `error ${e.message}`;
}
console.log('2)', statusOf(new HttpError(404)), '|', statusOf({ code: 'ENOENT' }), '|', statusOf(new Error('x')));

// 3) Discriminated union + exhaustive switch (never)
type Shape =
  | { kind: 'circle'; radius: number }
  | { kind: 'rect'; width: number; height: number }
  | { kind: 'triangle'; base: number; height: number };

function area(s: Shape): number {
  switch (s.kind) {
    case 'circle': return Math.PI * s.radius ** 2;
    case 'rect': return s.width * s.height;
    case 'triangle': return (s.base * s.height) / 2;
    default: {
      const unreachable: never = s;        // compile error here if a new kind is not handled
      throw new Error(`unknown shape ${JSON.stringify(unreachable)}`);
    }
  }
}
const shapes: Shape[] = [{ kind: 'circle', radius: 1 }, { kind: 'rect', width: 2, height: 3 }, { kind: 'triangle', base: 4, height: 5 }];
console.log('3)', shapes.map((s) => `${s.kind}=${area(s).toFixed(2)}`).join(', '));

// 4) Result type: success or failure without exceptions
type Result<T> = { ok: true; value: T } | { ok: false; error: string };
function parsePort(text: string): Result<number> {
  const n = Number(text);
  return Number.isInteger(n) && n > 0 && n < 65536 ? { ok: true, value: n } : { ok: false, error: `bad port "${text}"` };
}
for (const t of ['8080', 'http']) {
  const r = parsePort(t);
  console.log('4)', r.ok ? `port ${r.value}` : r.error);   // r.value only exists when r.ok
}

// 5) User-defined type guard (type predicate) and assertion function
interface Cat { meow(): string }
interface Dog { bark(): string }
function isCat(pet: Cat | Dog): pet is Cat { return 'meow' in pet; }
const pets: (Cat | Dog)[] = [{ meow: () => 'meow' }, { bark: () => 'woof' }];
console.log('5)', pets.map((p) => (isCat(p) ? p.meow() : p.bark())).join(' '));

function assertIsString(v: unknown, name: string): asserts v is string {
  if (typeof v !== 'string') throw new TypeError(`${name} must be a string`);
}
const input: unknown = 'hello';
assertIsString(input, 'input');
console.log('5) after assert:', input.toUpperCase());    // input: string from here on

// 6) Optional chaining & nullish with strictNullChecks
type Order = { customer?: { address?: { city: string } } };
const orders: Order[] = [{ customer: { address: { city: 'Pune' } } }, {}];
console.log('6)', orders.map((o) => o.customer?.address?.city ?? 'unknown'));
