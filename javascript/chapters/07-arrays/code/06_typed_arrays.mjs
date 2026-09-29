// Typed arrays: fixed-length arrays of raw numbers over an ArrayBuffer (binary data).
const buf = new ArrayBuffer(8);                 // 8 raw bytes, all zero
const bytes = new Uint8Array(buf);              // view them as 8 unsigned bytes
const words = new Uint16Array(buf);             // ...or as 4 unsigned 16-bit ints (same memory)
bytes[0] = 0x34; bytes[1] = 0x12;
console.log(bytes, words[0].toString(16), '(little-endian on this machine)');

// Values are coerced to the element type
const u8 = new Uint8Array([255, 256, -1, 3.7]);
const clamped = new Uint8ClampedArray([255, 256, -1, 3.7]);
const i8 = new Int8Array([127, 128]);
console.log(u8, clamped, i8);

// Fixed length: no push/pop/splice, but most read methods exist
const f = new Float64Array([3, 1, 2]);
console.log(f.length, typeof f.push, f.map((x) => x * 2), f.toSorted(), Array.isArray(f));
console.log(Float32Array.of(0.1)[0], new Float16Array([0.1])[0]);   // precision per element type

// Converting and real-world use
console.log(Array.from(f), [...new TextEncoder().encode('Hi')], new TextDecoder().decode(new Uint8Array([72, 105])));
const view = new DataView(buf);
view.setUint16(2, 0xabcd, false);                                    // big-endian write, e.g. a network protocol
console.log(bytes.slice(2, 4), view.getUint16(2, false).toString(16));
