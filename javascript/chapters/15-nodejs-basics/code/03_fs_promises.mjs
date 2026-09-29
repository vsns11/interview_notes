// Files with the promise API: node:fs/promises
import { mkdtemp, writeFile, appendFile, readFile, readdir, stat, rename,
  copyFile, rm, open, mkdir, access } from 'node:fs/promises';
import { constants, glob } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const dir = await mkdtemp(join(tmpdir(), 'notes-'));   // unique temp folder
try {
  const file = join(dir, 'todo.txt');

  await writeFile(file, 'buy milk\n');                 // create / overwrite (utf8 by default)
  await appendFile(file, 'write chapter 15\n');        // add to the end
  const text = await readFile(file, 'utf8');           // without 'utf8' you get a Buffer
  console.log('read:', JSON.stringify(text));

  const buf = await readFile(file);
  console.log('as Buffer:', buf.constructor.name, buf.length, 'bytes, first byte', buf[0]);

  await writeFile(join(dir, 'config.json'), JSON.stringify({ port: 8080, debug: true }, null, 2));
  const config = JSON.parse(await readFile(join(dir, 'config.json'), 'utf8'));
  console.log('config.port:', config.port);

  await mkdir(join(dir, 'archive/2026'), { recursive: true });  // like mkdir -p
  await copyFile(file, join(dir, 'archive/2026/todo-copy.txt'));
  await rename(join(dir, 'config.json'), join(dir, 'settings.json'));

  const entries = await readdir(dir, { withFileTypes: true });
  console.log('entries:', entries.map((e) => `${e.name}${e.isDirectory() ? '/' : ''}`).sort().join(', '));

  const all = await readdir(dir, { recursive: true });          // Node 20+
  console.log('recursive:', all.sort().join(', '));

  const txt = [];
  for await (const p of glob('**/*.txt', { cwd: dir })) txt.push(p);   // fs glob (Node 22+)
  console.log('glob *.txt:', txt.sort().join(', '));

  const s = await stat(file);
  console.log('stat: isFile', s.isFile(), '| size', s.size, '| mtime is Date', s.mtime instanceof Date);

  // Low-level: a FileHandle - always close it (finally)
  const fh = await open(file, 'r');
  try {
    const { bytesRead, buffer } = await fh.read(Buffer.alloc(8), 0, 8, 0);
    console.log('first 8 bytes:', JSON.stringify(buffer.toString('utf8', 0, bytesRead)));
  } finally {
    await fh.close();
  }

  // Errors carry a code: ENOENT (missing), EEXIST, EACCES, EISDIR, ENOTDIR ...
  try {
    await readFile(join(dir, 'nope.txt'), 'utf8');
  } catch (err) {
    console.log('error code:', err.code, '| syscall:', err.syscall);
  }

  // "Does it exist?" - prefer just trying the operation; access() shown for completeness
  const exists = await access(file, constants.R_OK).then(() => true, () => false);
  console.log('exists & readable:', exists);

  // Flags: 'wx' = create, fail if it already exists (atomic "create new")
  try {
    await writeFile(file, 'x', { flag: 'wx' });
  } catch (err) {
    console.log('wx on existing file ->', err.code);
  }
} finally {
  await rm(dir, { recursive: true, force: true });     // like rm -rf
  console.log('cleaned up');
}
