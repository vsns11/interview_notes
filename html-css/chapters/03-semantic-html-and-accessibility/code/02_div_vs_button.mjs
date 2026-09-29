// Three identical-looking "Save" controls. What does the accessibility tree say, and what can a keyboard reach?
import { openPage, printTree, fullTree } from './a11y_lib.mjs';

const { browser, page } = await openPage('div-vs-button.html');
console.log('Accessibility tree:');
printTree(await fullTree(page));

console.log('\nPressing Tab 4 times:');
for (let i = 1; i <= 4; i++) {
  await page.keyboard.press('Tab');
  console.log(`  Tab ${i} ->`, await page.evaluate(() => document.activeElement === document.body ? '(body — focus left the page)' : document.activeElement.textContent));
}

for (const [label, key] of [['button', 'Enter'], ['button', ' '], ['div + role', 'Enter']]) {
  await page.$eval('#out', el => (el.textContent = ''));
  const handle = label === 'button' ? await page.$('button') : await page.$('[role=button]');
  await handle.focus();
  await page.keyboard.press(key === ' ' ? 'Space' : key);
  console.log(`\n${label.padEnd(10)} + ${key === ' ' ? 'Space' : key}: status = "${await page.$eval('#out', el => el.textContent)}"`);
}
await browser.close();
