// Class syntax: constructor, methods, fields, private members, static, accessors, static blocks.

class Temperature {
  // public instance field (ES2022) — created on each instance before the constructor body runs
  unit = 'C';
  // private instance field — only code inside this class body can touch it
  #celsius;
  // static field + private static field
  static instances = 0;
  static #absoluteZero = -273.15;

  // static initialization block (ES2022) — runs once when the class is defined
  static {
    Temperature.freezing = new Temperature(0);
    Temperature.instances = 0; // don't count the helper instance
  }

  constructor(celsius) {
    Temperature.#check(celsius);
    this.#celsius = celsius;
    Temperature.instances++;
  }

  // getter / setter pair — used like a property
  get fahrenheit() { return this.#celsius * 9 / 5 + 32; }
  set fahrenheit(f) {
    const c = (f - 32) * 5 / 9;
    Temperature.#check(c);
    this.#celsius = c;
  }
  get celsius() { return this.#celsius; }

  // private method
  #format(v) { return v.toFixed(1); }
  toString() { return `${this.#format(this.#celsius)}°C`; }

  static #check(c) {
    if (c < Temperature.#absoluteZero) throw new RangeError(`below absolute zero: ${c}`);
  }
  static fromFahrenheit(f) { const t = new Temperature(0); t.fahrenheit = f; return t; }
  // ergonomic brand check (ES2022): does obj have this class's private field?
  static isTemperature(obj) { return #celsius in obj; }
}

const t = new Temperature(25);
console.log(String(t), '=', t.fahrenheit, '°F');
t.fahrenheit = 212;
console.log('after set  :', String(t), '| unit field:', t.unit);
console.log('static     :', Temperature.instances, String(Temperature.freezing), String(Temperature.fromFahrenheit(-40)));
console.log('brand check:', Temperature.isTemperature(t), Temperature.isTemperature({ celsius: 1 }));
console.log('own keys   :', Object.keys(t), '(private fields are invisible)');
try { new Temperature(-300); } catch (e) { console.log('validation :', e.name, e.message); }
// t.#celsius  -> SyntaxError at parse time if written outside the class

// Facts about classes
console.log('typeof class        :', typeof Temperature);
console.log('methods on prototype:', Object.getOwnPropertyNames(Temperature.prototype));
console.log('methods enumerable? :', Object.keys(Temperature.prototype).length > 0);
try { Temperature(1); } catch (e) { console.log('call without new    :', e.message); }

// Class declarations are hoisted but stay in the TDZ until defined
try {
  new Later();
} catch (e) { console.log('TDZ                 :', e.name, '-', e.message); }
class Later {}

// Method vs arrow-function field: where does `this` come from?
class Clicker {
  count = 0;
  method() { return ++this.count; }
  arrow = () => ++this.count;   // one function per instance, this captured forever
}
const c = new Clicker();
const { method, arrow } = c;
try { method(); } catch (e) { console.log('detached method     :', e.message); }
console.log('detached arrow      :', arrow(), arrow());
console.log('arrow on prototype? :', 'arrow' in Clicker.prototype, '| own?', Object.hasOwn(c, 'arrow'));

// Computed method names and a class expression
const key = 'shout';
const Loud = class { [key]() { return 'HEY'; } static [Symbol.hasInstance](x) { return x === 42; } };
console.log('computed name       :', new Loud().shout(), '| 42 instanceof Loud:', 42 instanceof Loud);

// Accessor defined with Object.defineProperty equivalent
const desc = Object.getOwnPropertyDescriptor(Temperature.prototype, 'fahrenheit');
console.log('accessor descriptor :', typeof desc.get, typeof desc.set, 'enumerable:', desc.enumerable);
