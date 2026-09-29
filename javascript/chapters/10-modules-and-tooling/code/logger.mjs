// tools_run: skip — helper module with a default export
export default function log(level, msg) {       // the default export (one per module at most)
  return `[${level.toUpperCase()}] ${msg}`;
}
export const LEVELS = ['debug', 'info', 'warn', 'error']; // a module can mix default + named
