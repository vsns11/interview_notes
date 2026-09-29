// Object.keys / values / entries / fromEntries / assign / create / is, spread and copies.
const stock = { apples: 5, pears: 0, plums: 12 };

console.log(Object.keys(stock));
console.log(Object.values(stock));
console.log(Object.entries(stock));

// entries -> transform with array methods -> fromEntries back into an object
const inStock = Object.fromEntries(
  Object.entries(stock).filter(([, qty]) => qty > 0).map(([k, qty]) => [k.toUpperCase(), qty]),
);
console.log(inStock);

// Iterating an object
for (const [fruit, qty] of Object.entries(stock)) console.log(`${fruit.padEnd(7)}${qty}`);

// Object.assign copies own enumerable props into the target (mutates it) and returns it
const defaults = { theme: 'light', lang: 'en', fontSize: 14 };
const prefs = { theme: 'dark' };
const merged = Object.assign({}, defaults, prefs); // later sources win
console.log(merged);

// Spread does the same, without mutating anything
console.log({ ...defaults, ...prefs, fontSize: 16 });

// Rest in object destructuring: pull out some keys, keep "the rest"
const { password, ...safeUser } = { id: 7, name: 'Lin', password: 'x' };
console.log(safeUser);

// Shallow copy trap: nested objects are shared
const original = { name: 'Kit', address: { city: 'Pune' } };
const shallow = { ...original };
const deep = structuredClone(original);
shallow.address.city = 'Delhi';
console.log(original.address.city, deep.address.city);

// Object.create: new object whose prototype is the given object
const animal = { speak() { return `${this.name} makes a sound`; } };
const dog = Object.create(animal);
dog.name = 'Rex';
console.log(dog.speak(), Object.getPrototypeOf(dog) === animal, Object.keys(dog));
const dict = Object.create(null);    // no prototype at all: a "pure" dictionary
console.log('toString' in dict, 'toString' in {});

// Object.is: like === except for NaN and +0/-0
console.log(NaN === NaN, Object.is(NaN, NaN), 0 === -0, Object.is(0, -0));
