// Methods that CHANGE the array vs methods that return a NEW array (ES2023 "change array by copy").
const show = (label, value, original) => console.log(label.padEnd(26), JSON.stringify(value).padEnd(22), 'original:', JSON.stringify(original));

// splice(start, deleteCount, ...itemsToInsert) - the Swiss-army mutator; returns removed items
let arr = ['a', 'b', 'c', 'd', 'e'];
show('splice(1, 2)', arr.splice(1, 2), arr);                   // remove 2 at index 1
show("splice(1, 0, 'X', 'Y')", arr.splice(1, 0, 'X', 'Y'), arr);  // insert, remove nothing
show("splice(-1, 1, 'Z')", arr.splice(-1, 1, 'Z'), arr);         // replace the last one

// sort / reverse / fill / copyWithin mutate AND return the same array
arr = [3, 1, 2];
const sorted = arr.sort();
console.log(sorted === arr, arr);
arr = [1, 2, 3, 4, 5];
show('reverse()', arr.reverse(), arr);
show('fill(0, 1, 3)', [1, 2, 3, 4].fill(0, 1, 3), '(new literal)');
show('copyWithin(0, 3)', [1, 2, 3, 4, 5].copyWithin(0, 3), '(new literal)');

// The copying twins (ES2023): same result, original untouched
const base = [3, 1, 2];
show('toSorted()', base.toSorted(), base);
show('toReversed()', base.toReversed(), base);
show("toSpliced(1, 1, 'x')", base.toSpliced(1, 1, 'x'), base);
show('with(0, 99)', base.with(0, 99), base);
show('with(-1, 7)', base.with(-1, 7), base);
try { base.with(10, 1); } catch (e) { console.log('with(10, 1) ->', e.constructor.name); }

// Other non-mutating builders
show('slice(1)', base.slice(1), base);
show('concat([4], 5)', base.concat([4], 5), base);
show('[...base, 4]', [...base, 4], base);

// Why it matters: shared references (e.g. React state, function arguments)
function topTwoBad(scores) { return scores.sort((a, b) => b - a).slice(0, 2); }
function topTwoGood(scores) { return scores.toSorted((a, b) => b - a).slice(0, 2); }
const s1 = [50, 90, 70];
console.log(topTwoBad(s1), 'caller now sees', s1);
const s2 = [50, 90, 70];
console.log(topTwoGood(s2), 'caller still sees', s2);

// const does not stop mutation
const frozenish = [1, 2];
frozenish.push(3);
console.log(frozenish, Object.isFrozen(Object.freeze([1])));
