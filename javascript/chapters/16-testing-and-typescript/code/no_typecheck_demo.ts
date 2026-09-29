// Node strips types WITHOUT checking them: this file has a type error but still runs.
// (Excluded from tsconfig.json so `tsc --noEmit` stays clean; check it on its own to see the error.)
const price: number = 5;
const qty: number = '2';                  // type error: string is not assignable to number
const total: number = price + qty;        // JS concatenates: 5 + '2' -> '52'
console.log('total =', JSON.stringify(total), '| typeof', typeof total);
