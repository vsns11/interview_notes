import '../../../../tools_dom.mjs';
document.body.innerHTML = `
<ul id="todos">
  <li data-id="1"><span>Buy milk</span> <button class="del">x</button></li>
  <li data-id="2"><span>Write chapter</span> <button class="del">x</button></li>
</ul>
<button id="add">Add</button>`;
const list = document.getElementById('todos');

// ONE listener on the parent handles clicks for every current AND future item
list.addEventListener('click', (event) => {
  const del = event.target.closest('button.del');
  if (del && list.contains(del)) {
    const li = del.closest('li');
    console.log('delete item', li.dataset.id);
    li.remove();
    return;
  }
  const li = event.target.closest('li');
  if (li) {
    li.classList.toggle('done');
    console.log('toggle item', li.dataset.id, '-> done =', li.classList.contains('done'));
  }
});

let nextId = 3;
document.getElementById('add').addEventListener('click', () => {
  list.insertAdjacentHTML('beforeend', `<li data-id="${nextId}"><span>New task ${nextId}</span> <button class="del">x</button></li>`);
  nextId += 1;
});

// Simulate the user
list.querySelector('li[data-id="1"] span').click();      // click on the text (a child of li)
document.getElementById('add').click();                   // adds item 3 AFTER the listener was attached
list.querySelector('li[data-id="3"] span').click();      // still handled - no new listener needed
list.querySelector('li[data-id="2"] .del').click();
console.log('remaining:', [...list.children].map((li) => `${li.dataset.id}${li.classList.contains('done') ? '(done)' : ''}`));
console.log('listeners attached to items: 0, to the list: 1');
