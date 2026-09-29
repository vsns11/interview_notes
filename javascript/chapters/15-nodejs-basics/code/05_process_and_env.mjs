// process: arguments, environment, exit codes, cwd, lifecycle events
import { parseArgs } from 'node:util';
import { writeFileSync, rmSync, mkdtempSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

// 1) argv: [path to node, path to script, ...user args]
console.log('argv[0] is node:', process.argv[0] === process.execPath);
console.log('user args      :', process.argv.slice(2));

// Parse flags with util.parseArgs (built in). We pass a sample array instead of real argv.
const { values, positionals } = parseArgs({
  args: ['--port', '8080', '-v', 'build', 'src/'],
  options: {
    port: { type: 'string', default: '3000' },
    verbose: { type: 'boolean', short: 'v' },
    env: { type: 'string', default: 'development' },
  },
  allowPositionals: true,
});
console.log('parsed values  :', values);
console.log('positionals    :', positionals);

// 2) Environment variables are ALWAYS strings (or undefined)
process.env.MAX_ITEMS = 25;                         // assigned a number...
console.log('typeof MAX_ITEMS:', typeof process.env.MAX_ITEMS);  // ...stored as a string
const maxItems = Number.parseInt(process.env.MAX_ITEMS ?? '10', 10);
console.log('parsed number  :', maxItems + 1);
const debug = process.env.DEBUG === 'true';         // "false" is a truthy string!
console.log('DEBUG flag     :', debug, '| truthy "false"?', Boolean('false'));

// Load a .env file (Node 20.12+/21.7+). CLI alternative: node --env-file=.env app.mjs
const dir = mkdtempSync(join(tmpdir(), 'env-'));
const envFile = join(dir, '.env');
writeFileSync(envFile, 'DB_HOST=localhost\nDB_PORT=5432\n# comment\nGREETING="hello world"\n');
process.loadEnvFile(envFile);
console.log('from .env      :', process.env.DB_HOST, process.env.DB_PORT, process.env.GREETING);
rmSync(dir, { recursive: true });

// Config pattern: read + validate env once at startup, fail fast
function loadConfig(env) {
  const port = Number(env.PORT ?? 3000);
  if (!Number.isInteger(port) || port <= 0) throw new Error(`Invalid PORT: ${env.PORT}`);
  return Object.freeze({ port, dbHost: env.DB_HOST ?? 'localhost', isProd: env.NODE_ENV === 'production' });
}
console.log('config         :', loadConfig({ PORT: '8080', NODE_ENV: 'production' }));
try { loadConfig({ PORT: 'abc' }); } catch (e) { console.log('config error   :', e.message); }

// 3) Other useful bits
console.log('cwd is a string:', typeof process.cwd());
console.log('platform/arch  :', typeof process.platform, typeof process.arch);
console.log('pid is number  :', Number.isInteger(process.pid));
const t0 = process.hrtime.bigint();                  // high-resolution timer (ns, BigInt)
const ns = process.hrtime.bigint() - t0;
console.log('hrtime is bigint:', typeof ns);
process.stdout.write('stdout.write adds no newline -> ');
process.stdout.write('see?\n');

// 4) Exit codes: set process.exitCode and let Node finish naturally.
//    process.exit() stops immediately and may cut off pending writes.
process.on('exit', (code) => console.log(`exit event, code ${code}`));
process.exitCode = 0;
