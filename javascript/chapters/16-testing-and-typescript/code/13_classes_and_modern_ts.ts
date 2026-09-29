// Classes in TS, interfaces as contracts, as const, satisfies, and enum alternatives

// 1) Access modifiers are compile-time only; #private is enforced at runtime
interface Account { deposit(cents: number): void; readonly balance: number }

class BankAccount implements Account {
  #balance = 0;                               // JS private field (runtime)
  private readonly history: number[] = [];    // TS private (compile-time only)
  protected owner: string;
  static count = 0;

  constructor(owner: string) {                // (parameter properties like `constructor(private x)`
    this.owner = owner;                       //  are NOT erasable - see section 16.9)
    BankAccount.count++;
  }
  deposit(cents: number): void {
    if (cents <= 0) throw new RangeError('deposit must be positive');
    this.#balance += cents;
    this.history.push(cents);
  }
  get balance(): number { return this.#balance; }
}
class SavingsAccount extends BankAccount {
  describe(): string { return `${this.owner}'s savings: ${this.balance}`; }  // protected OK in subclass
}
const acct = new SavingsAccount('Ana');
acct.deposit(5000);
console.log('1)', acct.describe(), '| accounts:', BankAccount.count);

// 2) "as const" - literal, readonly values; derive a union type from data
const ROLES = ['viewer', 'editor', 'admin'] as const;
type Role = (typeof ROLES)[number];           // 'viewer' | 'editor' | 'admin'
function canEdit(r: Role): boolean { return r !== 'viewer'; }
console.log('2)', ROLES.map((r) => `${r}:${canEdit(r)}`).join(' '));

// 3) Enum alternative: a const object + derived type (works with Node type stripping)
const LogLevel = { Debug: 0, Info: 1, Warn: 2, Error: 3 } as const;
type LogLevel = (typeof LogLevel)[keyof typeof LogLevel];   // 0 | 1 | 2 | 3
function shouldLog(level: LogLevel, min: LogLevel = LogLevel.Info): boolean { return level >= min; }
console.log('3)', shouldLog(LogLevel.Debug), shouldLog(LogLevel.Error));

// 4) satisfies: check against a type but keep the precise inferred type
type Route = { path: string; auth: boolean };
const routes = {
  home: { path: '/', auth: false },
  admin: { path: '/admin', auth: true },
} satisfies Record<string, Route>;
console.log('4)', Object.keys(routes).join(','), routes.admin.path);   // routes.admin is known to exist

// 5) Index signatures and noUncheckedIndexedAccess
const stock: Record<string, number> = { pen: 3 };
const inkCount = stock['ink'];                // number | undefined (with noUncheckedIndexedAccess)
console.log('5)', inkCount ?? 0);
