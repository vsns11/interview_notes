import { readFile } from 'node:fs';

// 1) Error-first callbacks (the Node convention): cb(err, result)
readFile(new URL('./missing.txt', import.meta.url), 'utf8', (err, text) => {
  if (err) { console.log('error-first:', err.code); return; }
  console.log(text);
});

// 2) Callback hell: each step needs the previous result
const db = {
  getUser: (id, cb) => setTimeout(() => cb(null, { id, name: 'Ada' }), 5),
  getOrders: (user, cb) => setTimeout(() => cb(null, [{ id: 7, user: user.id }]), 5),
  getItems: (order, cb) => setTimeout(() => cb(order.id === 7 ? null : new Error('no order'), ['book', 'pen']), 5),
};
db.getUser(1, (err, user) => {
  if (err) return console.log(err.message);
  db.getOrders(user, (err, orders) => {
    if (err) return console.log(err.message);
    db.getItems(orders[0], (err, items) => {
      if (err) return console.log(err.message);
      console.log('pyramid result:', user.name, orders[0].id, items);
    });
  });
});

// 3) Inversion of control: a buggy library calls your callback twice
function buggyCharge(amount, cb) { cb(null, 'ok'); cb(null, 'ok'); }
let charges = 0;
buggyCharge(10, () => { charges += 1; });
console.log('callback-style charges:', charges);

// A promise can settle only once, so the second call is ignored
const charged = new Promise((resolve) => buggyCharge(10, (_e, r) => resolve(r)));
let promiseCharges = 0;
await charged.then(() => { promiseCharges += 1; });
console.log('promise-style charges :', promiseCharges);

// 4) Zalgo: a callback that is sometimes sync, sometimes async -> unpredictable order
const cache = new Map([['a', 1]]);
function getValue(key, cb) {
  if (cache.has(key)) cb(cache.get(key));          // sync!
  else setTimeout(() => cb(42), 0);                // async
}
const order = [];
getValue('a', () => order.push('cached cb'));
order.push('after call (a)');
getValue('b', () => order.push('fetched cb'));
order.push('after call (b)');
setTimeout(() => console.log('zalgo order:', order.join(' | ')), 30);
