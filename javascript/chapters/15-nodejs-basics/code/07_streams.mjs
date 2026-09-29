// Streams: process data piece by piece instead of loading it all into memory
import { Readable, Writable, Transform } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import { createReadStream, createWriteStream } from 'node:fs';
import { mkdtemp, rm, stat, readFile } from 'node:fs/promises';
import { createGzip, createGunzip } from 'node:zlib';
import { createInterface } from 'node:readline';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

// 1) Readable from any iterable; consume with for await
const letters = Readable.from(['a', 'b', 'c']);
const seen = [];
for await (const chunk of letters) seen.push(chunk);
console.log('1) read chunks:', seen);

// 2) A Transform: uppercase every chunk. A Writable: collect output.
const upper = () => new Transform({
  transform(chunk, _enc, done) { done(null, chunk.toString().toUpperCase()); },
});
function collector(out) {
  return new Writable({
    write(chunk, _enc, done) { out.push(chunk.toString()); done(); },
  });
}
const out = [];
await pipeline(Readable.from(['hello ', 'streams']), upper(), collector(out));
console.log('2) pipeline result:', out.join(''));

// 3) A line splitter Transform (chunks do NOT align with lines!) in object mode
function splitLines() {
  let rest = '';
  return new Transform({
    readableObjectMode: true,
    transform(chunk, _enc, done) {
      const parts = (rest + chunk).split('\n');
      rest = parts.pop();                 // keep the incomplete last piece
      for (const line of parts) this.push(line);
      done();
    },
    flush(done) { if (rest) this.push(rest); done(); },
  });
}
const lines = [];
await pipeline(Readable.from(['id,name\n1,Ana\n2,Bo', 'b\n3,Cy']), splitLines(),
  new Writable({ objectMode: true, write(line, _e, d) { lines.push(line); d(); } }));
console.log('3) lines:', lines);

// 4) Files: write a big file, gzip it, gunzip it back - constant memory
const dir = await mkdtemp(join(tmpdir(), 'streams-'));
try {
  const src = join(dir, 'log.txt');
  function* logLines() { for (let i = 1; i <= 50_000; i++) yield `line ${i} status=ok\n`; }
  await pipeline(Readable.from(logLines()), createWriteStream(src));
  await pipeline(createReadStream(src), createGzip(), createWriteStream(src + '.gz'));
  await pipeline(createReadStream(src + '.gz'), createGunzip(), createWriteStream(src + '.copy'));
  const [a, b] = [await stat(src), await stat(src + '.gz')];
  console.log('4) original bytes:', a.size, '| gzip smaller:', b.size < a.size / 5);
  const same = (await readFile(src, 'utf8')) === (await readFile(src + '.copy', 'utf8'));
  console.log('   round trip identical:', same);

  // 5) Read a file line by line with readline
  const rl = createInterface({ input: createReadStream(src), crlfDelay: Infinity });
  let n = 0, last = '';
  for await (const line of rl) { n++; last = line; }
  console.log('5) readline counted', n, 'lines; last =', JSON.stringify(last));

  // 6) pipeline propagates errors and destroys every stream
  try {
    await pipeline(createReadStream(join(dir, 'missing.txt')), createGzip(), createWriteStream(join(dir, 'x.gz')));
  } catch (err) {
    console.log('6) pipeline rejected:', err.code);
  }
} finally {
  await rm(dir, { recursive: true, force: true });
}

// 7) Backpressure: write() returns false when the buffer is full -> wait for 'drain'
const slow = new Writable({
  highWaterMark: 16,                                   // tiny buffer (bytes)
  write(_chunk, _enc, done) { setTimeout(done, 1); },  // a slow destination
});
let falses = 0, drains = 0;
slow.on('drain', () => drains++);
for (let i = 0; i < 5; i++) {
  const ok = slow.write('0123456789');                 // 10 bytes each
  if (!ok) { falses++; await new Promise((r) => slow.once('drain', r)); }
}
slow.end();
await new Promise((r) => slow.on('finish', r));
console.log('7) write() returned false', falses, 'times; waited for drain each time:', drains === falses);
