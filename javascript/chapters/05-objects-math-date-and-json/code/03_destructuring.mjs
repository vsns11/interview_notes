// Object destructuring: rename, defaults, nesting, parameters.
const order = {
  id: 'A-17',
  customer: { name: 'Mira', address: { city: 'Lyon' } },
  items: 3,
};

const { id, items } = order;                       // basic
const { id: orderId } = order;                     // rename: take `id`, call it orderId
const { status = 'pending' } = order;              // default when the value is undefined
const { customer: { name, address: { city } } } = order; // nested
console.log(id, items, orderId, status, name, city);

// Defaults apply only for undefined, not for null
const { a = 1, b = 2 } = { a: undefined, b: null };
console.log(a, b);

// Destructuring in function parameters ("named arguments"), with a default for the whole object
function createUser({ name, role = 'viewer', active = true } = {}) {
  return `${name ?? 'anonymous'} / ${role} / ${active}`;
}
console.log(createUser({ name: 'Jo', role: 'admin' }));
console.log(createUser());

// Destructuring into existing variables needs parentheses
let x, y;
({ x, y } = { x: 10, y: 20 });
console.log(x, y);

// Destructuring null/undefined throws
try { const { q } = null; } catch (e) { console.log(e.constructor.name); }
