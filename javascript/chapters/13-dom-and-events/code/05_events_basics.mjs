import dom from '../../../../tools_dom.mjs';
const { CustomEvent, AbortController } = dom.window;   // jsdom's own classes (Node has different ones)
document.body.innerHTML = '<button id="save">Save</button><input id="name">';
const btn = document.getElementById('save');

// 1) addEventListener + the event object
function onClick(event) {
  console.log('1) type:', event.type, '| target:', event.target.id, '| currentTarget:', event.currentTarget.id,
    '| this === currentTarget:', this === event.currentTarget, '| isTrusted:', event.isTrusted);
}
btn.addEventListener('click', onClick);
btn.click();                                     // synthetic click (isTrusted false)

// 2) Several listeners run in registration order; the same function is only added once
btn.addEventListener('click', () => console.log('2) second listener'));
btn.addEventListener('click', onClick);          // duplicate, ignored
btn.click();

// 3) removeEventListener needs the SAME function reference
btn.removeEventListener('click', () => {});     // different function: does nothing
btn.removeEventListener('click', onClick);
console.log('3) after remove:'); btn.click();

// 4) Options: once, signal (passive and capture are shown in later files)
const b2 = document.createElement('button');
b2.addEventListener('click', () => console.log('4) once fired'), { once: true });
b2.click(); b2.click();
const ac = new AbortController();
b2.addEventListener('click', () => console.log('   A'), { signal: ac.signal });
b2.addEventListener('click', () => console.log('   B'), { signal: ac.signal });
b2.click(); ac.abort(); b2.click();
console.log('   (both removed by one abort)');

// 5) The on-property style allows only ONE handler
b2.onclick = () => console.log('5) handler 1');
b2.onclick = () => console.log('5) handler 2 replaced 1');
b2.click();

// 6) Keyboard and input events carry extra data
const input = document.getElementById('name');
input.addEventListener('keydown', (e) => console.log('6) key:', e.key, '| code:', e.code, '| ctrl:', e.ctrlKey));
input.dispatchEvent(new KeyboardEvent('keydown', { key: 's', code: 'KeyS', ctrlKey: true, bubbles: true }));
input.addEventListener('input', () => console.log('   input event, value =', input.value));
input.value = 'Ada'; input.dispatchEvent(new Event('input', { bubbles: true }));

// 7) Custom events for your own components
document.addEventListener('cart:add', (e) => console.log('7) custom event detail:', e.detail));
btn.dispatchEvent(new CustomEvent('cart:add', { detail: { sku: 'A-1', qty: 2 }, bubbles: true }));

// 8) Listeners run synchronously inside dispatchEvent
console.log('8) before dispatch');
btn.addEventListener('ping', () => console.log('   listener'));
btn.dispatchEvent(new Event('ping'));
console.log('   after dispatch');
