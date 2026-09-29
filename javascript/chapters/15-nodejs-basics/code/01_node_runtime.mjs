// What is different about the Node runtime compared with a browser?
import { builtinModules } from 'node:module';

console.log('typeof window   :', typeof window);        // no DOM in Node
console.log('typeof document :', typeof document);
console.log('typeof process  :', typeof process);       // Node-only global
console.log('globalThis === global:', globalThis === global);
console.log('node major      :', process.versions.node.split('.')[0]);
console.log('has V8 & libuv  :', 'v8' in process.versions, 'uv' in process.versions);

// Web-standard globals that Node also provides
const webGlobals = ['fetch', 'URL', 'AbortController', 'TextEncoder', 'structuredClone',
  'setTimeout', 'queueMicrotask', 'crypto', 'Blob', 'WebSocket'];
console.log('web APIs present:', webGlobals.filter((n) => n in globalThis).length, 'of', webGlobals.length);

// Where am I? (ES modules have no __dirname; use import.meta)
console.log('this file       :', import.meta.filename.split('/').pop());
console.log('dirname ends    :', import.meta.dirname.split('/').slice(-2).join('/'));
console.log('typeof __dirname:', typeof __dirname);

// Built-in modules - a few of the most used
const wanted = ['fs', 'path', 'events', 'stream', 'http', 'os', 'crypto', 'child_process', 'worker_threads', 'zlib'];
console.log('built-ins exist :', wanted.every((m) => builtinModules.includes(m)));
console.log('built-in count >50:', builtinModules.length > 50);
