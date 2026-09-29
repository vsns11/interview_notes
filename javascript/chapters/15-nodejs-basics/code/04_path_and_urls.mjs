// node:path - build and take apart file paths without string hacks
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const p = '/home/ana/projects/shop/src/utils/price.test.mjs';
console.log('basename :', path.basename(p));
console.log('  no ext :', path.basename(p, '.mjs'));
console.log('extname  :', path.extname(p));           // only the LAST extension
console.log('dirname  :', path.dirname(p));
console.log('parse    :', path.parse(p));
console.log('format   :', path.format({ dir: '/tmp', name: 'report', ext: '.csv' }));

console.log('join     :', path.join('/srv', 'app', '..', 'data', './users.json'));
console.log('resolve  :', path.resolve('/srv/app', 'logs', 'out.log'));
console.log('resolve abs wins:', path.resolve('/srv/app', '/etc', 'hosts'));
console.log('normalize:', path.normalize('/a//b/../c/./d'));
console.log('relative :', path.relative('/srv/app/src', '/srv/app/public/logo.png'));
console.log('isAbsolute:', path.isAbsolute('src/x.js'), path.isAbsolute('/src/x.js'));

// Platform differences: path uses the host OS rules; path.posix / path.win32 are fixed.
console.log('sep here :', JSON.stringify(path.sep));
console.log('win32 join:', path.win32.join('C:\\Users', 'ana', 'file.txt'));
console.log('posix join:', path.posix.join('usr', 'local', 'bin'));

// ES modules identify files by URL; convert when you need a path.
const url = new URL('file:///home/ana/my%20notes/a.txt');
console.log('fileURLToPath:', fileURLToPath(url));
console.log('pathToFileURL:', pathToFileURL('/home/ana/my notes/a.txt').href);
console.log('sibling file :', path.join(import.meta.dirname, 'data.json').endsWith('code/data.json'));

// Security: stop "../" escaping a base folder (path traversal)
function safeJoin(base, userPath) {
  const full = path.resolve(base, userPath);
  if (full !== base && !full.startsWith(base + path.sep)) throw new Error(`blocked: ${userPath}`);
  return full;
}
console.log('safe  :', safeJoin('/srv/uploads', 'cat.png'));
try { safeJoin('/srv/uploads', '../../etc/passwd'); } catch (e) { console.log('unsafe:', e.message); }
