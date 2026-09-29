// Running other programs: child_process, and npm-style scripts with `node --run`
import { execFile, spawn } from 'node:child_process';
import { promisify } from 'node:util';
import { mkdtemp, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const execFileP = promisify(execFile);

// 1) execFile: run a program, buffer its output (no shell -> no shell injection)
const { stdout } = await execFileP(process.execPath, ['-e', 'console.log(6 * 7)']);
console.log('child said:', stdout.trim());

// 2) spawn: stream output, get the exit code
const child = spawn(process.execPath, ['-e', 'console.log("working"); process.exit(3)']);
child.stdout.on('data', (d) => console.log('spawn stdout:', d.toString().trim()));
const [code] = await new Promise((r) => child.on('close', (...a) => r(a)));
console.log('spawn exit code:', code);

// 3) A throw-away project with package.json scripts, run with `node --run`
const dir = await mkdtemp(join(tmpdir(), 'proj-'));
try {
  await writeFile(join(dir, 'package.json'), JSON.stringify({
    name: 'demo', private: true, type: 'module',
    scripts: { hello: 'node hello.js', greet: 'node hello.js' },
  }, null, 2));
  await writeFile(join(dir, 'hello.js'), 'console.log(`hello from ${process.argv[2] ?? "script"}`)');
  const r1 = await execFileP(process.execPath, ['--run', 'hello'], { cwd: dir });
  console.log('node --run hello ->', r1.stdout.trim());
  const r2 = await execFileP(process.execPath, ['--run', 'greet', '--', 'Ana'], { cwd: dir });
  console.log('node --run greet -- Ana ->', r2.stdout.trim());
} finally {
  await rm(dir, { recursive: true, force: true });
}
