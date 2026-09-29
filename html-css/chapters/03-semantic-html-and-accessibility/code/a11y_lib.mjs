// tools_run: skip  (helper imported by the other scripts)
// Open an HTML file in real headless Chrome and return Chrome's accessibility tree — what screen readers get.
import puppeteer from 'puppeteer-core';
import path from 'node:path';

export async function openPage(file) {
  const browser = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: 'new' });
  const page = await browser.newPage();
  await page.goto('file://' + path.resolve(file));
  return { browser, page };
}

// Print one line per node: role "accessible name"  states. Wrapper/noise roles are skipped (their children are kept);
// StaticText is shown only when it is not just repeating its parent's name.
const SHOW = ['level', 'checked', 'expanded', 'required', 'invalid', 'disabled', 'value', 'description'];
const NOISE = ['generic', 'none', 'InlineTextBox', 'LineBreak', 'ListMarker', 'LabelText'];
export function printTree(node, { depth = 0, skipRoles = NOISE, parentName = '' } = {}) {
  if (!node) return;
  const name = (node.name ?? '').trim();
  const skip = skipRoles.includes(node.role) || (node.role === 'paragraph' && !name) ||
               (node.role === 'StaticText' && (parentName.includes(name) || !name));
  if (!skip) {
    const extras = SHOW.filter(k => node[k] !== undefined && node[k] !== '' && !(k === 'required' && node[k] === false)).map(k => `${k}=${JSON.stringify(node[k])}`);
    console.log('  '.repeat(depth) + node.role + (name ? ` "${name}"` : '') + (extras.length ? '  ' + extras.join(' ') : ''));
  }
  for (const c of node.children ?? [])
    printTree(c, { depth: skip ? depth : depth + 1, skipRoles, parentName: skip ? parentName : name });
}

export const fullTree = page => page.accessibility.snapshot({ interestingOnly: false });
