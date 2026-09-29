import dom from '../../../../tools_dom.mjs';
document.body.innerHTML = '<ul id="list"><li>first</li></ul><div id="out"></div>';
const list = document.getElementById('list');

// 1) Create, fill, insert
const li = document.createElement('li');
li.textContent = 'appended';
list.append(li, 'a text node too');          // append accepts nodes AND strings
const first = document.createElement('li');
first.textContent = 'prepended';
list.prepend(first);
list.firstElementChild.after(Object.assign(document.createElement('li'), { textContent: 'after first' }));
console.log('1)', list.innerHTML);

// 2) Remove, replace, move
list.lastChild.remove();                       // the text node
list.querySelector('li:nth-child(3)').replaceWith(Object.assign(document.createElement('li'), { textContent: 'replaced' }));
document.getElementById('out').append(list.firstElementChild);   // appending an existing node MOVES it
console.log('2)', list.innerHTML, '| out:', document.getElementById('out').innerHTML);

// 3) textContent vs innerHTML
const box = document.getElementById('out');
const userInput = '<img src=x onerror="alert(1)">';
box.textContent = userInput;                    // shown as text, safe
console.log('3) textContent ->', box.innerHTML);
box.innerHTML = '<b>bold</b> and <i>italic</i>'; // parsed as HTML (only for trusted strings!)
console.log('   innerHTML   ->', box.children.length, 'elements; textContent =', JSON.stringify(box.textContent));
console.log('   outerHTML   ->', box.outerHTML);

// 4) insertAdjacentHTML positions
const p = document.createElement('p'); p.textContent = 'P';
box.replaceChildren(p);                         // empty the box, then insert p
p.insertAdjacentHTML('beforebegin', '<span>bb</span>');
p.insertAdjacentHTML('afterbegin', '<span>ab</span>');
p.insertAdjacentHTML('beforeend', '<span>be</span>');
p.insertAdjacentHTML('afterend', '<span>ae</span>');
console.log('4)', box.innerHTML);

// 5) Build many nodes off-DOM with a DocumentFragment, insert once
const frag = document.createDocumentFragment();
for (const name of ['Ada', 'Grace', 'Linus']) {
  const item = document.createElement('li');
  item.textContent = name;
  frag.append(item);
}
list.replaceChildren(frag);
console.log('5)', list.innerHTML, '| fragment is now empty:', frag.childNodes.length);

// 6) <template> + cloneNode
document.body.insertAdjacentHTML('beforeend',
  '<template id="card"><article class="card"><h2></h2><p class="price"></p></article></template>');
const tpl = document.getElementById('card');
const products = [{ name: 'Lamp', price: 25 }, { name: 'Desk', price: 180 }];
const grid = document.createElement('section');
for (const pr of products) {
  const node = tpl.content.cloneNode(true);     // deep copy of the template's content
  node.querySelector('h2').textContent = pr.name;
  node.querySelector('.price').textContent = `$${pr.price}`;
  grid.append(node);
}
console.log('6)', grid.innerHTML);

// 7) innerText is layout-aware and jsdom has no layout engine
console.log('7) innerText in jsdom:', 'innerText' in box ? 'implemented' : 'not implemented (use textContent)');
