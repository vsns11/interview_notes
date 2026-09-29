import '../../../../tools_dom.mjs';
document.body.innerHTML = `
<input id="qty" type="number" value="1">
<a id="link" href="/docs?page=2">Docs</a>
<input id="agree" type="checkbox" checked>
<button id="buy" class="btn primary" data-product-id="42" data-in-stock="true" style="color: red">Buy</button>`;

// 1) Attribute = what the HTML says; property = the live state
const qty = document.getElementById('qty');
qty.value = '5';                                 // what the user "typed"
console.log('value property       :', qty.value, '| value attribute:', qty.getAttribute('value'), '| defaultValue:', qty.defaultValue);
console.log('valueAsNumber        :', qty.valueAsNumber, typeof qty.valueAsNumber);

const link = document.getElementById('link');
console.log('href property        :', link.href, '(resolved, absolute)');
console.log('href attribute       :', link.getAttribute('href'));

// 2) Boolean attributes: presence = true, the value doesn't matter
const agree = document.getElementById('agree');
agree.checked = false;                           // user unticks it
console.log('checked prop / attr :', agree.checked, '/', agree.hasAttribute('checked'));
const buy = document.getElementById('buy');
buy.setAttribute('disabled', 'false');           // still disabled!
console.log('disabled="false"    :', buy.disabled);
buy.removeAttribute('disabled');
console.log('after remove        :', buy.disabled);

// 3) Names differ: class -> className/classList, for -> htmlFor
console.log('className           :', buy.className);

// 4) classList
buy.classList.add('loading');
buy.classList.remove('primary');
buy.classList.toggle('active');                  // add if missing
buy.classList.toggle('active', false);           // force off
buy.classList.replace('btn', 'button');
console.log('classList           :', [...buy.classList], '| contains loading:', buy.classList.contains('loading'));

// 5) dataset: data-* attributes, kebab-case <-> camelCase, always strings
console.log('dataset             :', { ...buy.dataset });
console.log('inStock is a string :', typeof buy.dataset.inStock, buy.dataset.inStock === 'true');
buy.dataset.variantColor = 'blue';
console.log('new attribute       :', buy.getAttribute('data-variant-color'));

// 6) Inline style vs computed style
buy.style.backgroundColor = 'yellow';            // camelCase property
buy.style.setProperty('--accent', 'teal');       // custom properties need setProperty
console.log('style attribute     :', buy.getAttribute('style'));
console.log('computed color      :', getComputedStyle(buy).color);

// 7) All attributes
console.log('attributes          :', [...buy.attributes].map((a) => a.name));
