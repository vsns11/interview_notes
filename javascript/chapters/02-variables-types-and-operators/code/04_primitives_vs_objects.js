// Primitives are copied by value; objects are shared by reference.
let x = 10;
let y = x;        // copies the value 10
y = 20;
console.log("x, y:", x, y);

const p1 = { name: "Ana" };
const p2 = p1;    // copies the REFERENCE — both names point at one object
p2.name = "Ben";
console.log("p1.name:", p1.name);

console.log("{} === {}:", {} === {});           // two different objects
console.log("p1 === p2:", p1 === p2);           // same object
console.log("'a' === 'a':", "a" === "a");       // primitives compare by value

// Primitives are immutable: string methods return NEW strings.
const s = "hello";
const upper = s.toUpperCase();
console.log(s, upper);
// Autoboxing: a primitive temporarily wrapped so you can call methods on it.
console.log("length of 'hello':", s.length, "| (255).toString(16):", (255).toString(16));
// Wrapper objects via new are objects, not primitives — avoid them.
console.log("typeof new Number(5):", typeof new Number(5), "| typeof Number('5'):", typeof Number("5"));
