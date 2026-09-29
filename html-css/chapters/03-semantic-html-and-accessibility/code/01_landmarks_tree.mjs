// Chrome's accessibility tree for landmarks.html: landmarks, headings, links — what a screen reader navigates by.
import { openPage, printTree, fullTree } from './a11y_lib.mjs';

const { browser, page } = await openPage('landmarks.html');
printTree(await fullTree(page), { skipRoles: ['StaticText', 'generic', 'none', 'InlineTextBox', 'LineBreak', 'ListMarker', 'LabelText', 'listitem', 'time'] });

// A screen reader's "headings list" and "landmarks list":
const outline = await page.$$eval('h1,h2,h3,h4,h5,h6', hs => hs.map(h => '  '.repeat(+h.tagName[1] - 1) + h.tagName.toLowerCase() + ' ' + h.firstChild.textContent.trim()));
console.log('\nHeading outline:\n' + outline.join('\n'));
await browser.close();
