// How assistive technology sees form-a11y.html: every field's name, description and states.
import { openPage, printTree, fullTree } from './a11y_lib.mjs';

const { browser, page } = await openPage('form-a11y.html');
printTree(await fullTree(page), { skipRoles: ['generic', 'none', 'InlineTextBox', 'LineBreak', 'ListMarker', 'LabelText', 'StaticText'] });

await page.type('input[placeholder]', '555');
console.log('\nAfter typing into the placeholder-only field, the visible hint is gone:',
  await page.$eval('input[placeholder]', el => ({ value: el.value, placeholderShown: el.matches(':placeholder-shown') })));
await browser.close();
