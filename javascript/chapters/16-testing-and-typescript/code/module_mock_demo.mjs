// tools_run: skip  (needs a flag)  Run:  node --test --experimental-test-module-mocks module_mock_demo.mjs
// Module mocking replaces what `import` returns - here the built-in node:os.
import { test } from 'node:test';
import assert from 'node:assert/strict';

test('greeting uses the mocked os module', async (t) => {
  t.mock.module('node:os', { namedExports: { userInfo: () => ({ username: 'ana' }) } });
  const { greeting } = await import('./src/greeting.mjs');   // import AFTER mocking
  assert.equal(greeting(), 'Hello, ana!');
});
