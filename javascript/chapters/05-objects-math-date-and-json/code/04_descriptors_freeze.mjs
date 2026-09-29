// Property descriptors, defineProperty, freeze / seal / preventExtensions.
// This file is an ES module, so it runs in strict mode: forbidden writes THROW.
const tryIt = (label, fn) => {
  try { fn(); console.log(label, '-> ok'); } catch (e) { console.log(label, '->', e.constructor.name); }
};

const product = { name: 'Pen' };
console.log(Object.getOwnPropertyDescriptor(product, 'name'));

// defineProperty: every flag you omit defaults to false
Object.defineProperty(product, 'sku', { value: 'P-001' });
console.log(Object.getOwnPropertyDescriptor(product, 'sku'));
console.log(Object.keys(product), Object.getOwnPropertyNames(product), product.sku);
tryIt('write read-only sku', () => { product.sku = 'X'; });
tryIt('delete non-configurable sku', () => { delete product.sku; });

Object.defineProperties(product, {
  price: { value: 2.5, writable: true, enumerable: true, configurable: true },
});
console.log(JSON.stringify(product)); // non-enumerable sku is skipped

// Symbol keys are separate from string keys
const secret = Symbol('secret');
product[secret] = 42;
console.log(Object.keys(product), Object.getOwnPropertySymbols(product));

// The three lock levels
const ext = { a: 1 };  Object.preventExtensions(ext);
const sealed = { a: 1 }; Object.seal(sealed);
const frozen = { a: 1, inner: { b: 1 } }; Object.freeze(frozen);

tryIt('preventExtensions: add   ', () => { ext.z = 1; });
tryIt('preventExtensions: change', () => { ext.a = 2; });
tryIt('preventExtensions: delete', () => { delete ext.a; });
tryIt('seal: add                ', () => { sealed.z = 1; });
tryIt('seal: change             ', () => { sealed.a = 2; });
tryIt('seal: delete             ', () => { delete sealed.a; });
tryIt('freeze: change           ', () => { frozen.a = 2; });
tryIt('freeze: nested change    ', () => { frozen.inner.b = 2; }); // freeze is shallow!
console.log(Object.isExtensible(ext), Object.isSealed(sealed), Object.isFrozen(frozen), Object.isFrozen(frozen.inner));

// A recursive deepFreeze
function deepFreeze(obj) {
  for (const value of Object.values(obj)) {
    if (value && typeof value === 'object') deepFreeze(value);
  }
  return Object.freeze(obj);
}
const config = deepFreeze({ db: { host: 'localhost', port: 5432 } });
tryIt('deepFreeze: nested change', () => { config.db.port = 1; });

// setPrototypeOf exists, but changing prototypes after creation is slow; prefer Object.create
const base = { hello() { return 'hi'; } };
const obj = Object.setPrototypeOf({}, base);
console.log(obj.hello(), Object.getPrototypeOf(obj) === base);
