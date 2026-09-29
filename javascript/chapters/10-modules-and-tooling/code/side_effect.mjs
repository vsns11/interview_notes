// tools_run: skip — a module imported only for its side effect
globalThis.polyfilled = true;
console.log('[side_effect.mjs] patched globalThis');
