// tools_run: skip  (module under test for module_mock.test.mjs)
import { userInfo } from 'node:os';
export function greeting() {
  return `Hello, ${userInfo().username}!`;
}
