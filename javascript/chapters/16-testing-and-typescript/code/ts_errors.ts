// tools_run: skip  (contains type errors ON PURPOSE)  Check:  tsc --ignoreConfig --noEmit --strict --target es2024 --erasableSyntaxOnly ts_errors.ts
type User = { id: number; name: string; email?: string };

const u: User = { id: 1, name: 'Ana', age: 30 };              // 1) excess property
u.id = '2';                                                   // 2) wrong type
console.log(u.email.toLowerCase());                           // 3) possibly undefined

function total(prices: number[]): number {
  return prices.reduce((a, b) => a + b, '');                  // 4) string is not number
}
total(['1.50', '2.00']);                                      // 5) string[] is not number[]

type Status = 'draft' | 'published';
const s: Status = 'publised';                                 // 6) typo caught

function len(x: string | number) {
  return x.length;                                            // 7) number has no length
}

class Point { constructor(private x: number) {} }             // 8) not erasable (Node can't strip)
enum Color { Red, Green }                                     // 9) not erasable either
