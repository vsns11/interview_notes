// Creating arrays, reading/writing by index, length, holes, and when to pick an array vs an object.
const fruit1 = 'apple', fruit2 = 'banana', fruit3 = 'cherry';   // three separate variables...
const fruits = ['apple', 'banana', 'cherry'];                  // ...or one array
console.log(fruits, fruits.length, typeof fruits, Array.isArray(fruits), Array.isArray({ length: 0 }));

// Creating
const mixed = [1, 'two', { three: 3 }, [4], null, () => 5];   // any types, can nest
console.log(mixed.length, mixed[2].three, mixed[3][0], mixed[5]());
console.log(new Array(3), new Array(3).length, new Array('a', 'b', 'c'), new Array(3, 4));
console.log(Array.of(3), Array.of(1, 2, 3));
console.log(Array.from('hey'), Array.from({ length: 4 }, (_, i) => i * i), Array.from(new Set([1, 1, 2])));
console.log(new Array(3).fill(0), Array(2).fill().map(() => []));

// Reading and changing
const colors = ['red', 'green', 'blue'];
console.log(colors[0], colors[colors.length - 1], colors.at(-1), colors[5]);
colors[2] = 'purple';
console.log(colors);

// Adding at the ends
const nums = [1, 2, 3];
console.log(nums.push(4), nums.unshift(0), nums);          // both return the NEW length
console.log(nums.pop(), nums.shift(), nums);               // both return the REMOVED element

// length is writable: truncating and extending
const letters = ['a', 'b', 'c', 'd'];
letters.length = 2;
console.log(letters);
letters[5] = 'f';                                          // writing past the end creates holes
console.log(letters, letters.length, 3 in letters, letters[3]);

// Holes vs undefined: some methods skip holes
const holey = [1, , 3];
const withUndef = [1, undefined, 3];
let visited = 0; holey.forEach(() => visited++);
console.log(holey, visited, withUndef.map((x) => x ?? 'U'), holey.map((x) => x ?? 'U'));

// Looping
for (let i = 0; i < colors.length; i++) process.stdout.write(`${i}:${colors[i]} `);
console.log();
for (const c of colors) process.stdout.write(c + ' ');
console.log();
colors.forEach((c, i) => process.stdout.write(`[${i}]${c} `));
console.log();
for (const [i, c] of colors.entries()) process.stdout.write(`${i}=${c} `);
console.log();

// Arrays are objects: typeof, references, const
const a = [1, 2]; const b = a; b.push(3);
console.log(typeof a, a, a === b, [1, 2] === [1, 2]);

// Array vs object for the same data
const fruitList = ['Apple', 'Banana', 'Cherry'];                      // ordered, by position
const fruitDetails = {                                                // lookup by name
  apple: { color: 'red', taste: 'sweet' },
  cherry: { color: 'red', taste: 'tart' },
};
console.log(fruitList[1], fruitDetails.cherry.taste);
const fruitRecords = [                                                // most common in real apps
  { name: 'Apple', color: 'red' }, { name: 'Banana', color: 'yellow' },
];
console.log(fruitRecords.find((f) => f.name === 'Banana').color);
