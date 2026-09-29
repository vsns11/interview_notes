// Sorting: the default string order trap, comparators, multi-key sorts, stability, locale.
console.log([10, 9, 1, 100, 25].sort());                    // default: compared as STRINGS
console.log([10, 9, 1, 100, 25].sort((a, b) => a - b));     // ascending numbers
console.log([10, 9, 1, 100, 25].sort((a, b) => b - a));     // descending numbers
console.log([3, undefined, 1, , 2].sort());                 // undefined and holes go last

// Comparator contract: negative -> a first, positive -> b first, 0 -> keep relative order
const people = [
  { name: 'Zoe', dept: 'eng', age: 31 },
  { name: 'adam', dept: 'ops', age: 25 },
  { name: 'Émile', dept: 'eng', age: 25 },
  { name: 'Bob', dept: 'ops', age: 31 },
];
console.log(people.toSorted((a, b) => a.age - b.age).map((p) => p.name));
console.log(people.map((p) => p.name).toSorted());                              // code-unit order
console.log(people.map((p) => p.name).toSorted((a, b) => a.localeCompare(b)));  // human order

// Multi-key: dept ascending, then age descending, then name
const byDeptAgeName = (a, b) =>
  a.dept.localeCompare(b.dept) || b.age - a.age || a.name.localeCompare(b.name);
console.log(people.toSorted(byDeptAgeName).map((p) => `${p.dept}/${p.age}/${p.name}`));

// Stability (guaranteed since ES2019): equal items keep their original order
const tasks = [['write', 2], ['test', 1], ['deploy', 2], ['plan', 1]];
console.log(tasks.toSorted((a, b) => a[1] - b[1]).map((t) => t[0]));

// Sorting by a computed key: precompute when the key is expensive ("decorate-sort-undecorate")
const words = ['banana', 'Kiwi', 'apple', 'fig'];
const sortedByLength = words
  .map((w) => ({ w, key: w.length }))
  .sort((a, b) => a.key - b.key || a.w.localeCompare(b.w))
  .map((x) => x.w);
console.log(sortedByLength);

// Booleans and dates
console.log([true, false, true].toSorted((a, b) => Number(a) - Number(b)));
const dates = [new Date('2024-03-01'), new Date('2023-12-25'), new Date('2024-01-15')];
console.log(dates.toSorted((a, b) => a - b).map((d) => d.toISOString().slice(0, 10)));

// A broken comparator (returns boolean) gives unreliable results - don't do this
console.log('boolean comparator ->', [3, 1, 2].sort((a, b) => a > b));

// Shuffle: Fisher-Yates, not sort(() => Math.random() - 0.5)
function shuffle(arr, rand = Math.random) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(rand() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
let seed = 42; const lcg = () => ((seed = (seed * 1103515245 + 12345) % 2 ** 31) / 2 ** 31); // deterministic demo RNG
console.log(shuffle([1, 2, 3, 4, 5, 6], lcg));
