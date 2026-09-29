import dom from '../../../../tools_dom.mjs';
const { DOMParser } = dom.window;

document.body.innerHTML = `
<main id="app">
  <h1 class="title">Groceries</h1>
  <!-- a comment node -->
  <ul id="list">
    <li data-id="1">Milk</li>
    <li data-id="2" class="done">Bread</li>
  </ul>
</main>`;

// Walk the tree and print it, including text and comment nodes
const TYPE = { 1: 'ELEMENT', 3: 'TEXT', 8: 'COMMENT', 9: 'DOCUMENT' };
function show(node, depth = 0) {
  const pad = '  '.repeat(depth);
  if (node.nodeType === Node.TEXT_NODE) {
    const t = node.textContent.trim();
    console.log(`${pad}#text ${t ? JSON.stringify(t) : '(whitespace)'}`);
  } else if (node.nodeType === Node.COMMENT_NODE) {
    console.log(`${pad}#comment "${node.textContent.trim()}"`);
  } else {
    console.log(`${pad}<${node.nodeName.toLowerCase()}> ${TYPE[node.nodeType]}`);
  }
  for (const child of node.childNodes) show(child, depth + 1);
}
show(document.querySelector('#app'));

const main = document.getElementById('app');
console.log('\nchildNodes (all nodes):', main.childNodes.length, '| children (elements only):', main.children.length);
console.log('document.documentElement:', document.documentElement.nodeName, '| head:', document.head.nodeName, '| body:', document.body.nodeName);
console.log('nodeType of document:', document.nodeType, '=== Node.DOCUMENT_NODE:', document.nodeType === Node.DOCUMENT_NODE);

// The DOM is live: change it and every view of it changes
const items = document.getElementById('list').children;   // live HTMLCollection
console.log('items before:', items.length);
document.getElementById('list').insertAdjacentHTML('beforeend', '<li data-id="3">Eggs</li>');
console.log('items after :', items.length);

// Parsing fixes broken HTML the way browsers do
const parsed = new DOMParser().parseFromString('<table><tr><td>cell</table><p>unclosed', 'text/html');
console.log('\nparser auto-inserted <tbody>:', parsed.querySelector('table > tbody > tr') !== null);
console.log('parsed body:', parsed.body.innerHTML);
