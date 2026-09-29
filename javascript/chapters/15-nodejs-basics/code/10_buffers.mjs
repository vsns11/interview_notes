// Buffer: Node's type for raw bytes (a Uint8Array subclass)
const text = 'héllo 👋';
const buf = Buffer.from(text, 'utf8');
console.log('string length (UTF-16 units):', text.length);
console.log('byte length (UTF-8)          :', buf.length, '=', Buffer.byteLength(text));
console.log('bytes (hex)                  :', buf.toString('hex'));
console.log('base64                       :', buf.toString('base64'));
console.log('back to text                 :', Buffer.from(buf.toString('base64'), 'base64').toString('utf8'));
console.log('is a Uint8Array              :', buf instanceof Uint8Array);

// Slicing a Buffer SHARES memory (unlike Array.prototype.slice)
const whole = Buffer.from('abcdef');
const part = whole.subarray(0, 3);
part[0] = 0x7a;                                   // 'z'
console.log('subarray shares memory       :', whole.toString());

// Cutting UTF-8 in the middle of a character produces the replacement char
console.log('cut mid-character            :', JSON.stringify(buf.subarray(0, 2).toString('utf8')));

// Concatenate chunks (what you do with stream data) and compare
const joined = Buffer.concat([Buffer.from('Node '), Buffer.from('24')]);
console.log('concat                       :', joined.toString(), '| equals:', joined.equals(Buffer.from('Node 24')));
console.log('alloc(4) is zero-filled      :', Buffer.alloc(4));
