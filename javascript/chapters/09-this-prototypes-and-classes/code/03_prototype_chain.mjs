// Every object has a hidden link [[Prototype]] to another object (or null).

const animal = {
  eats: true,
  describe() { return `${this.name} eats: ${this.eats}`; },
};
const rabbit = Object.create(animal);   // rabbit.[[Prototype]] = animal
rabbit.name = 'Rabbit';
rabbit.jumps = true;

console.log(Object.getPrototypeOf(rabbit) === animal);         // true
console.log(rabbit.eats, rabbit.describe());                   // found on animal
console.log('own keys:', Object.keys(rabbit));
console.log('hasOwn eats?', Object.hasOwn(rabbit, 'eats'), '| "eats" in rabbit?', 'eats' in rabbit);

// for...in walks inherited enumerable keys too
const seen = [];
for (const k in rabbit) seen.push(k);
console.log('for...in:', seen);

// Writing creates an OWN property (shadowing); the prototype is untouched.
rabbit.eats = false;
console.log('rabbit.eats', rabbit.eats, '| animal.eats', animal.eats);
delete rabbit.eats;
console.log('after delete, rabbit.eats', rabbit.eats);

// The full chain of rabbit, and of built-ins
function chain(obj) {
  const names = [];
  for (let p = Object.getPrototypeOf(obj); p !== null; p = Object.getPrototypeOf(p)) {
    names.push(p === animal ? 'animal' : p === Object.prototype ? 'Object.prototype' : `${p.constructor.name}.prototype`);
  }
  return names.concat('null').join(' -> ');
}
console.log('rabbit :', chain(rabbit));
console.log('[]     :', chain([]));
console.log('fn     :', chain(function f() {}));
console.log('Map    :', chain(new Map()));

// Methods are shared, not copied
const a = [1], b = [2];
console.log('shared push?', a.push === b.push, a.push === Array.prototype.push);

// Object with no prototype — a clean dictionary
const dict = Object.create(null);
dict.toString = 'just data';
console.log('proto of dict:', Object.getPrototypeOf(dict), '| "hasOwnProperty" in dict:', 'hasOwnProperty' in dict);

// Setting a prototype later is possible but slow — prefer Object.create
const base = { hello() { return 'hi from base'; } };
const late = {};
Object.setPrototypeOf(late, base);
console.log(late.hello());

// Object literal shortcut: __proto__ key in a literal sets the prototype (standard)
const lit = { __proto__: base, own: 1 };
console.log(lit.hello(), Object.keys(lit));

// Performance note: lookups that fail walk the whole chain
console.log('missing ->', rabbit.nope);
