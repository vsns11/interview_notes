# Chapter 13 — The DOM and Events

To JavaScript, a web page is a tree of objects called the **DOM**. Every interactive feature — a dropdown opening, a todo ticked off, a form rejecting an invalid email — is JavaScript finding nodes in that tree, changing them, and reacting to **events** such as clicks and key presses. This chapter covers the DOM API (the objects and functions for reading and changing the page), the full event system including the capture/bubble path, forms, and the performance rules that keep pages smooth.

**What you'll learn**

1. The DOM: nodes, elements, the tree, and how HTML becomes a DOM.
2. Selecting elements and walking the tree.
3. Creating, inserting, moving and removing nodes; `textContent` vs `innerHTML`; templates and fragments.
4. Attributes vs properties; `classList`, `dataset`, styles.
5. Events: listeners, the event object, listener options, custom events.
6. Propagation: capture, target and bubble phases; stopping it.
7. Event delegation — one listener for many elements.
8. Default actions, `preventDefault`, passive listeners.
9. Forms: `submit`, `FormData`, the Constraint Validation API, `input` vs `change`.
10. `MutationObserver`.
11. Rendering performance: reflow, repaint, layout thrashing (measured in Chrome).
12. When scripts run: `DOMContentLoaded`, `load`, `defer`.

**Before you start.** Closures ([Chapter 4](../04-functions-scope-and-closures/README.md)), `this` in handlers ([Chapter 9 §9.1](../09-this-prototypes-and-classes/README.md#91-this--the-call-time-context)); listeners run as tasks on the event loop ([Chapter 12](../12-asynchronous-javascript/README.md#122-the-event-loop)). HTML and CSS — semantic elements, forms markup, selectors (patterns like `.title` or `#app`) — are in the HTML & CSS Handbook ([`../../../html-css/`](../../../html-css/)).

**How the code runs.** Node has no DOM, so the `.mjs` programs in [`code/`](code/) start with `import '../../../../tools_dom.mjs';`, which creates a **jsdom** document (a JavaScript implementation of the DOM standard used by test tools) and exposes `document`, `window` etc. as globals. jsdom implements DOM and events faithfully but has **no layout engine** (no sizes, no painting); for those parts, small `.html` pages were run in headless Chrome and screenshotted after a scripted interaction. All outputs are real.

---

## 13.1 The DOM tree

### What it is
The **Document Object Model (DOM)** is the browser's live, in-memory representation of a page: a **tree** of **nodes** with an API (WHATWG — the browser makers' standards group — DOM and HTML standards) to read and change it. Change the tree and the screen updates. "View Source" shows the original HTML; DevTools' Elements panel shows the current DOM.

- **Node** — any item in the tree. Types: **element** (`<li>`, nodeType 1), **text** (3), **comment** (8), **document** (9), **document fragment** (11).
- **Element** — a node created from a tag; has attributes and can have children.
- **Parent / child / sibling / ancestor / descendant** — tree relations.
- `document` — the root; `document.documentElement` is `<html>`, plus `document.head` and `document.body`.

### How it works
1. The HTML **parser** builds the tree, fixing errors in standard ways (unclosed tags, missing `<tbody>`).
2. Whitespace between tags becomes **text nodes** — present though invisible.
3. Scripts change the tree through the DOM API; changes are live everywhere.

[`code/01_dom_tree.mjs`](code/01_dom_tree.mjs) — key parts:

```js
document.body.innerHTML = `
<main id="app">
  <h1 class="title">Groceries</h1>
  <!-- a comment node -->
  <ul id="list">
    <li data-id="1">Milk</li>
    <li data-id="2" class="done">Bread</li>
  </ul>
</main>`;

function show(node, depth = 0) {           // recursive walk over childNodes
  // prints #text / #comment / <tag> ELEMENT with indentation
  for (const child of node.childNodes) show(child, depth + 1);
}
show(document.querySelector('#app'));

const main = document.getElementById('app');
console.log('\nchildNodes (all nodes):', main.childNodes.length, '| children (elements only):', main.children.length);
// ...
const items = document.getElementById('list').children;   // live HTMLCollection
console.log('items before:', items.length);
document.getElementById('list').insertAdjacentHTML('beforeend', '<li data-id="3">Eggs</li>');
console.log('items after :', items.length);

const parsed = new DOMParser().parseFromString('<table><tr><td>cell</table><p>unclosed', 'text/html');
console.log('\nparser auto-inserted <tbody>:', parsed.querySelector('table > tbody > tr') !== null);
console.log('parsed body:', parsed.body.innerHTML);
```

Output:
```
<main> ELEMENT
  #text (whitespace)
  <h1> ELEMENT
    #text "Groceries"
  #text (whitespace)
  #comment "a comment node"
  #text (whitespace)
  <ul> ELEMENT
    #text (whitespace)
    <li> ELEMENT
      #text "Milk"
    #text (whitespace)
    <li> ELEMENT
      #text "Bread"
    #text (whitespace)
  #text (whitespace)

childNodes (all nodes): 7 | children (elements only): 2
document.documentElement: HTML | head: HEAD | body: BODY
nodeType of document: 9 === Node.DOCUMENT_NODE: true
items before: 2
items after : 3

parser auto-inserted <tbody>: true
parsed body: <table><tbody><tr><td>cell</td></tr></tbody></table><p>unclosed</p>
```

### Common mistakes
- `childNodes`/`firstChild`/`nextSibling` return whitespace text nodes. Prefer **element-only** versions: `children`, `firstElementChild`, `nextElementSibling`.
- Assuming the DOM equals the source — the parser inserts, moves and closes elements.
- A `<head>` script querying body elements before they're parsed (§13.12).

### TL;DR
The DOM is a live tree of nodes (elements, text, comments) built by the parser. Use element-only navigation to skip whitespace text nodes.

---

## 13.2 Selecting and traversing

### What it is
| Method | Returns | Notes |
|---|---|---|
| `document.getElementById(id)` | Element or `null` | Fastest; id without `#` |
| `el.querySelector(css)` | First match or `null` | Any CSS selector; searches descendants of `el` |
| `el.querySelectorAll(css)` | **Static** `NodeList` (snapshot) | Has `forEach`; spread to an array for `map`/`filter` |
| `el.getElementsByClassName(c)` / `getElementsByTagName(t)` | **Live** `HTMLCollection` | Updates automatically; no `forEach` |
| `document.forms.name.elements.field` | Form controls by `name` | §13.9 |
| `el.closest(css)` | Nearest ancestor **or itself** matching, or `null` | Key to event delegation |
| `el.matches(css)` | `true`/`false` | "Does this element match?" |
| `parent.contains(node)` | `true`/`false` | Includes `parent` itself |

Traversal: `parentElement`, `children`, `firstElementChild`, `lastElementChild`, `previousElementSibling`, `nextElementSibling` (node-level: `parentNode`, `childNodes`, `firstChild`, `nextSibling`…).

[`code/02_select_traverse.mjs`](code/02_select_traverse.mjs) — key parts:

```js
console.log('querySelector       :', document.querySelector('.links a').textContent);        // first match
console.log('querySelectorAll    :', [...document.querySelectorAll('.links a')].map((a) => a.textContent));
console.log('attribute selector  :', document.querySelector('[data-track]').textContent);
console.log(':not + :nth-child   :', [...document.querySelectorAll('li:not(:first-child) a')].map((a) => a.textContent));
console.log('no match            :', document.querySelector('.nope'), document.querySelectorAll('.nope').length);

const staticList = document.querySelectorAll('li');          // snapshot
const liveList = document.getElementsByTagName('li');         // live
document.querySelector('ul').append(document.createElement('li'));
console.log('\nafter adding an <li>: static', staticList.length, '| live', liveList.length);

const blog = document.querySelector('a[href="/blog"]');
console.log('closest("nav")      :', blog.closest('nav').id);
console.log('matches(":not(.x)") :', blog.matches('a:not(.active)'));
const li = blog.parentElement;
console.log('nextSibling (a node):', JSON.stringify(li.nextSibling.textContent), '<- whitespace text node');

try { document.querySelector('a[href=/]'); } catch (e) { console.log('\nbad selector        :', e.name); }
try { document.querySelector('#123'); } catch (e) { console.log('id starting w/ digit:', e.name, '-> use getElementById("123") or CSS.escape'); }
```

Output (lines for the code shown):
```
querySelector       : Home
querySelectorAll    : [ 'Home', 'Blog', 'About' ]
attribute selector  : About
:not + :nth-child   : [ 'Blog', 'About' ]
no match            : null 0

after adding an <li>: static 3 | live 4
closest("nav")      : menu
matches(":not(.x)") : true
nextSibling (a node): "\n    " <- whitespace text node

bad selector        : SyntaxError
id starting w/ digit: SyntaxError -> use getElementById("123") or CSS.escape
```

### Common mistakes
- `document.querySelector('.item').classList.add(…)` with no match → `TypeError: Cannot read properties of null`. Check for `null`, or use `?.` when absence is fine.
- `.map` on a `NodeList` — it has `forEach` only; use `[...list]` or `Array.from(list)`.
- Removing items while looping a **live** collection — indexes shift, items are skipped. Loop a static copy.
- Unquoted attribute values containing `/` or ids starting with digits → `SyntaxError`. Quote: `a[href="/"]`.

### Self-check
1. Which returns a live collection? → `getElementsBy…`; `querySelectorAll` is static.
2. `el.closest('li')` when `el` is an `<li>`? → `el`.

### TL;DR
`querySelector(All)` for almost everything; `getElementById` for ids; `closest`/`matches`/`contains` for relationships. `querySelectorAll` = static snapshot; `getElementsBy*` = live.

---

## 13.3 Creating, inserting and removing nodes

### What it is
| Task | API |
|---|---|
| Create | `document.createElement('li')`, `document.createTextNode('hi')`, `document.createDocumentFragment()` |
| Insert inside | `parent.append(...nodesOrStrings)`, `parent.prepend(...)`, `parent.replaceChildren(...)` (also empties) |
| Insert beside | `el.before(...)`, `el.after(...)` |
| Remove / replace | `el.remove()`, `el.replaceWith(...)` |
| Insert HTML string | `el.insertAdjacentHTML('beforebegin' / 'afterbegin' / 'beforeend' / 'afterend', html)` |
| Copy | `el.cloneNode(true)` (deep = with descendants) |
| Text | `el.textContent = '...'` (safe, never parsed) |
| Markup | `el.innerHTML = '...'` / `el.outerHTML` (parsed as HTML) |

Legacy equivalents: `appendChild`, `insertBefore`, `removeChild`, `replaceChild` (nodes only, no strings).

[`code/03_create_modify.mjs`](code/03_create_modify.mjs) — key parts:

```js
const li = document.createElement('li');
li.textContent = 'appended';
list.append(li, 'a text node too');          // append accepts nodes AND strings
// ... prepend, after
list.lastChild.remove();                       // the text node
list.querySelector('li:nth-child(3)').replaceWith(/* new li "replaced" */);
document.getElementById('out').append(list.firstElementChild);   // appending an existing node MOVES it

const userInput = '<img src=x onerror="alert(1)">';
box.textContent = userInput;                    // shown as text, safe
console.log('3) textContent ->', box.innerHTML);
box.innerHTML = '<b>bold</b> and <i>italic</i>'; // parsed as HTML (only for trusted strings!)

p.insertAdjacentHTML('beforebegin', '<span>bb</span>');  // + afterbegin, beforeend, afterend

const frag = document.createDocumentFragment();
for (const name of ['Ada', 'Grace', 'Linus']) {
  const item = document.createElement('li');
  item.textContent = name;
  frag.append(item);
}
list.replaceChildren(frag);                     // one insertion

const tpl = document.getElementById('card');    // <template id="card"><article>...</article></template>
for (const pr of products) {
  const node = tpl.content.cloneNode(true);     // deep copy of the template's content
  node.querySelector('h2').textContent = pr.name;
  node.querySelector('.price').textContent = `$${pr.price}`;
  grid.append(node);
}
```

Output:
```
1) <li>prepended</li><li>after first</li><li>first</li><li>appended</li>a text node too
2) <li>after first</li><li>replaced</li><li>appended</li> | out: <li>prepended</li>
3) textContent -> &lt;img src=x onerror="alert(1)"&gt;
   innerHTML   -> 2 elements; textContent = "bold and italic"
   outerHTML   -> <div id="out"><b>bold</b> and <i>italic</i></div>
4) <span>bb</span><p><span>ab</span>P<span>be</span></p><span>ae</span>
5) <li>Ada</li><li>Grace</li><li>Linus</li> | fragment is now empty: 0
6) <article class="card"><h2>Lamp</h2><p class="price">$25</p></article><article class="card"><h2>Desk</h2><p class="price">$180</p></article>
7) innerText in jsdom: not implemented (use textContent)
```

### `textContent` vs `innerText` vs `innerHTML`
| Property | Reads | Writes | Cost / safety |
|---|---|---|---|
| `textContent` | All descendant text, incl. hidden elements and `<script>` text | Replaces children with one text node | Fast; **safe** for user data |
| `innerText` | Only *rendered* text (respects `display:none`, line breaks) | Like textContent but `\n` → `<br>` | Needs layout → slower; not in jsdom |
| `innerHTML` | Serialized HTML of the children | **Parses** the string, replaces children | **XSS** risk (cross-site scripting — attacker code injected into your page) with untrusted strings; destroys existing children and their listeners |

Line 3: with `textContent` the "image" became harmless text (`&lt;img …`). Via `innerHTML` it would be a real `<img>` whose `onerror` runs attacker code — see [Chapter 14 §14.10](../14-browser-apis-and-security/README.md#1410-cross-site-scripting-xss-and-sanitizing).

### Why fragments and templates
- A **`DocumentFragment`** is a lightweight, parentless container. Appending it moves all its children in one operation (it ends up empty — line 5): one page update instead of N.
- A **`<template>`** holds inert markup (not rendered, scripts don't run, images don't load). `template.content.cloneNode(true)` stamps copies — how framework-free apps and Web Components (the browser's built-in custom HTML tags) render lists. Fill clones with `textContent`.

### Common mistakes
- `el.innerHTML += '<li>…</li>'` in a loop re-parses everything and wipes listeners and input state. Use `append` or `insertAdjacentHTML('beforeend', …)`.
- Appending an existing node **moves** it (line 2); clone for a copy.
- `cloneNode` doesn't copy `addEventListener` listeners.

### TL;DR
`createElement` + `textContent` + `append`/`before`/`after`/`remove`/`replaceWith`. `textContent` for data, `innerHTML` only for trusted markup. Batch inserts with a fragment; stamp repeated markup from a `<template>`.

---

## 13.4 Attributes, properties, classes, data and styles

### What it is
- An **attribute** is what's written in the HTML (`<input value="1">`) — always a string; `getAttribute`, `setAttribute`, `hasAttribute`, `removeAttribute`, `toggleAttribute`.
- A **property** is a field on the element object (`input.value`). Most standard attributes are **reflected** as same-named properties, but properties can be typed (booleans, numbers), normalized (absolute URLs) and hold **live state** the attribute doesn't.

[`code/04_attributes_properties.mjs`](code/04_attributes_properties.mjs) — key parts:

```js
const qty = document.getElementById('qty');      // <input id="qty" type="number" value="1">
qty.value = '5';                                 // what the user "typed"
console.log('value property       :', qty.value, '| value attribute:', qty.getAttribute('value'), '| defaultValue:', qty.defaultValue);
console.log('valueAsNumber        :', qty.valueAsNumber, typeof qty.valueAsNumber);
console.log('href property        :', link.href, '(resolved, absolute)');
console.log('href attribute       :', link.getAttribute('href'));

agree.checked = false;                           // <input type="checkbox" checked>
console.log('checked prop / attr :', agree.checked, '/', agree.hasAttribute('checked'));
buy.setAttribute('disabled', 'false');           // still disabled!
console.log('disabled="false"    :', buy.disabled);

buy.classList.add('loading');
buy.classList.remove('primary');
buy.classList.toggle('active');                  // add if missing
buy.classList.toggle('active', false);           // force off
buy.classList.replace('btn', 'button');

console.log('dataset             :', { ...buy.dataset });     // data-product-id="42" data-in-stock="true"
buy.dataset.variantColor = 'blue';               // -> data-variant-color="blue"

buy.style.backgroundColor = 'yellow';            // camelCase property
buy.style.setProperty('--accent', 'teal');       // custom properties need setProperty
console.log('computed color      :', getComputedStyle(buy).color);
```

Output (lines for the code shown):
```
value property       : 5 | value attribute: 1 | defaultValue: 1
valueAsNumber        : 5 number
href property        : http://localhost/docs?page=2 (resolved, absolute)
href attribute       : /docs?page=2
checked prop / attr : false / true
disabled="false"    : true
dataset             : { productId: '42', inStock: 'true' }
computed color      : rgb(255, 0, 0)
```

### Key rules
| Rule | Example |
|---|---|
| Form controls: **attribute = initial/default value**, **property = current value** | `value`/`defaultValue`, `checked`/`defaultChecked` |
| **Boolean attributes**: present = true, whatever the value | `disabled="false"` is still disabled; use `el.disabled = false` or `removeAttribute` |
| Some names differ | `class` → `className` / `classList`; `for` → `htmlFor` |
| URL properties are absolute | `a.href`, `img.src` |
| `data-*` → `el.dataset.camelCase`, always strings | `data-in-stock` → `dataset.inStock === 'true'`; read numbers with `Number(el.dataset.userId)` |
| `el.style` = **inline** style only; `getComputedStyle(el)` = final values from all CSS | Computed values are resolved (`rgb(255, 0, 0)`) |

Prefer toggling **classes** over many inline styles: the look stays in CSS and one class change is one style recalculation. Inline `style` suits truly dynamic values, ideally via CSS custom properties (`--name` variables): `el.style.setProperty('--progress', '40%')`.

### Self-check
1. User types "7" into `<input value="1">`. `getAttribute('value')`? → `"1"`; `input.value` is `"7"`.
2. Un-disable a button? → `btn.disabled = false` (or `removeAttribute('disabled')`), not `setAttribute('disabled', 'false')`.

### TL;DR
Attributes = HTML strings (initial values); properties = live typed state. `classList` for classes, `dataset` for `data-*`, `style` for inline, `getComputedStyle` to read final styles.

---

## 13.5 Events and listeners

### What it is
An **event** is a notification that something happened — click, key press, form submission, resource loaded. An **event listener** (handler) is a function registered to run when an event type reaches a **target** (element, `document`, `window`, or any `EventTarget` — the base type of everything that can receive events). Multiple listeners run in registration order.

### How it works
1. `target.addEventListener(type, listener, options?)` registers.
2. The event occurs (user action, network, or `target.dispatchEvent(event)`).
3. The browser builds an **event object** and sends it along a path through the tree (§13.6), calling matching listeners.
4. User-generated events arrive as **tasks**; `dispatchEvent` calls listeners **synchronously**, before it returns.

| Member | Meaning |
|---|---|
| `type` | `'click'`, `'keydown'`, … |
| `target` | Where the event originated (deepest element clicked) |
| `currentTarget` | Element whose listener is running now (= `this` in a non-arrow listener) |
| `eventPhase` | 1 capture, 2 at target, 3 bubble |
| `preventDefault()` / `defaultPrevented` | Cancel the default action (§13.8) |
| `stopPropagation()` / `stopImmediatePropagation()` | Stop the journey (§13.6) |
| `isTrusted` | `true` only for real user/browser events |
| `key`, `code`, `ctrlKey`, `shiftKey`, `altKey`, `metaKey` | Keyboard (`key` = character/meaning, `code` = physical key) |
| `clientX/Y`, `button` | Pointer position and button |
| `detail` | Data on a `CustomEvent` |

[`code/05_events_basics.mjs`](code/05_events_basics.mjs) — key parts:

```js
function onClick(event) {
  console.log('1) type:', event.type, '| target:', event.target.id, '| currentTarget:', event.currentTarget.id,
    '| this === currentTarget:', this === event.currentTarget, '| isTrusted:', event.isTrusted);
}
btn.addEventListener('click', onClick);
btn.click();                                     // synthetic click (isTrusted false)

btn.addEventListener('click', () => console.log('2) second listener'));
btn.addEventListener('click', onClick);          // duplicate, ignored
btn.click();

btn.removeEventListener('click', () => {});     // different function: does nothing
btn.removeEventListener('click', onClick);
console.log('3) after remove:'); btn.click();

b2.addEventListener('click', () => console.log('4) once fired'), { once: true });
b2.click(); b2.click();
const ac = new AbortController();
b2.addEventListener('click', () => console.log('   A'), { signal: ac.signal });
b2.addEventListener('click', () => console.log('   B'), { signal: ac.signal });
b2.click(); ac.abort(); b2.click();

b2.onclick = () => console.log('5) handler 1');
b2.onclick = () => console.log('5) handler 2 replaced 1');
b2.click();

input.addEventListener('keydown', (e) => console.log('6) key:', e.key, '| code:', e.code, '| ctrl:', e.ctrlKey));
input.dispatchEvent(new KeyboardEvent('keydown', { key: 's', code: 'KeyS', ctrlKey: true, bubbles: true }));

document.addEventListener('cart:add', (e) => console.log('7) custom event detail:', e.detail));
btn.dispatchEvent(new CustomEvent('cart:add', { detail: { sku: 'A-1', qty: 2 }, bubbles: true }));

console.log('8) before dispatch');
btn.addEventListener('ping', () => console.log('   listener'));
btn.dispatchEvent(new Event('ping'));
console.log('   after dispatch');
```

Output:
```
1) type: click | target: save | currentTarget: save | this === currentTarget: true | isTrusted: false
1) type: click | target: save | currentTarget: save | this === currentTarget: true | isTrusted: false
2) second listener
3) after remove:
2) second listener
4) once fired
   A
   B
   (both removed by one abort)
5) handler 2 replaced 1
6) key: s | code: KeyS | ctrl: true
   input event, value = Ada
7) custom event detail: { sku: 'A-1', qty: 2 }
8) before dispatch
   listener
   after dispatch
```

### Listener options
| Option | Effect |
|---|---|
| `capture: true` | Run during the capture phase (§13.6). Must match when removing. |
| `once: true` | Auto-remove after the first call |
| `passive: true` | Promise not to call `preventDefault()` — the browser can scroll without waiting (§13.8) |
| `signal: abortSignal` | Remove when the signal aborts — removes many at once ([Ch. 12 §12.11](../12-asynchronous-javascript/README.md#1211-cancellation-abortcontroller-and-abortsignal)) |

### `addEventListener` vs `on…` properties vs inline attributes
| Style | Multiple handlers? | Notes |
|---|---|---|
| `el.addEventListener('click', fn)` | Yes | Options, removable — the standard choice |
| `el.onclick = fn` | No — assigning replaces (line 5) | Fine for quick scripts |
| `<button onclick="doIt()">` | — | JS in HTML, code from a string, blocked by a strict CSP (Content Security Policy — [Ch. 14](../14-browser-apis-and-security/README.md#1412-content-security-policy-csp)) — avoid |

### Common mistakes
- `addEventListener('click', handle())` calls `handle` now and registers its return value. Pass the function.
- Removing with a *new* arrow function (line 3's no-op): keep a reference, or use `{ signal }`.
- Arrow listeners don't get `this = element`; `event.currentTarget` works for both styles.
- The event type has no `on` prefix (`'click'`, not `'onclick'`).

### TL;DR
`addEventListener(type, fn, { once, passive, signal, capture })`. `target` = origin, `currentTarget` = listener owner; cancel defaults or stop propagation from the event object. `dispatchEvent` + `CustomEvent` for your own events.

---

## 13.6 Propagation: capture, target, bubble

### What it is
Clicking a button inside nested boxes also clicks each box, the body, the document and the window. The event travels this **propagation path** in three phases:

1. **Capture phase** — from `window` down through each ancestor; only `{ capture: true }` listeners run.
2. **Target phase** — at the target; listeners in registration order (capture ones first in modern browsers).
3. **Bubble phase** — back up from the target's parent to `window`; normal listeners run. Most events bubble; some don't.

[`code/propagation.html`](code/propagation.html) registers a capture and a bubble listener on `window`, `document`, `#outer`, `#inner` and the button, then clicks once:

![Nested boxes and the ordered list of ten listener calls: four capture calls from window down to #inner, two at the target, four bubble calls back up to window](img/propagation.png)

*Figure — one click, ten listener calls: capture (blue) down, target (green), bubble (orange) up. Headless Chrome.*

[`code/06_propagation.mjs`](code/06_propagation.mjs) shows the same path in jsdom plus the ways to stop it:

```js
for (const el of [window, document, ...els]) {
  const name = el === window ? 'window' : el === document ? 'document' : el.id;
  el.addEventListener('click', (e) => log.push(`${name}:${PHASE[e.eventPhase]}`), { capture: true });
  el.addEventListener('click', (e) => log.push(`${name}:${PHASE[e.eventPhase]}`));
}
document.getElementById('btn').click();

const stop = (e) => e.stopPropagation();
els[1].addEventListener('click', stop);           // on #inner, bubble phase
els[2].click();

btn.addEventListener('click', (e) => { log.push('btn:first-extra'); e.stopImmediatePropagation(); });
btn.addEventListener('click', () => log.push('btn:NEVER'));
btn.click();

document.body.addEventListener('focus', () => seen.push('focus (bubble listener)'));
document.body.addEventListener('focusin', () => seen.push('focusin'));
input.focus();
```

Output:
```
1) full path:
   window:CAPTURE > document:CAPTURE > outer:CAPTURE > inner:CAPTURE > btn:TARGET > btn:TARGET > inner:BUBBLE > outer:BUBBLE > document:BUBBLE > window:BUBBLE
2) stopPropagation at inner:
   window:CAPTURE > document:CAPTURE > outer:CAPTURE > inner:CAPTURE > btn:TARGET > btn:TARGET > inner:BUBBLE
3) stopImmediatePropagation:
   window:CAPTURE > document:CAPTURE > outer:CAPTURE > inner:CAPTURE > btn:TARGET > btn:TARGET > btn:first-extra
4) new Event() defaults to x: bubbles=false
   body heard: [ 'focusin' ] <- focus does not bubble, focusin does
```

### Stopping
| Call | Effect |
|---|---|
| `stopPropagation()` | Finish listeners on the current element, then stop travelling |
| `stopImmediatePropagation()` | Also skip the remaining listeners on the current element |
| `preventDefault()` | Unrelated to propagation: cancels the default action only |

### Events that don't bubble
`focus`/`blur` (element gaining/losing keyboard **focus**; delegate with `focusin`/`focusout`), `mouseenter`/`mouseleave` (use `mouseover`/`mouseout`), `load`/`error` on elements like `<img>`, `scroll` on elements. `new Event(type)` **doesn't bubble unless you pass `{ bubbles: true }`** (line 4) — a common reason a parent "doesn't hear" a custom event.

### When not to stop propagation
`stopPropagation` also blocks analytics listeners, "click outside to close" handlers on `document`, and framework root listeners (React attaches its listeners at the root container). Prefer checking `event.target` in the outer handler, or `defaultPrevented` as a signal.

### In the interview
**30-second answer:** "Events travel in three phases: capture from window down to the target, the target phase, then bubbling back up. `addEventListener` listens in the bubble phase by default; `{capture: true}` switches to capture. `stopPropagation` halts the journey, `stopImmediatePropagation` also skips other listeners on the same element, and `preventDefault` is separate — it only cancels the browser's default action."

- *Q: `target` vs `currentTarget`?* A: `target` is where the event started; `currentTarget` is the element whose listener is running.
- *Q: When use capture?* A: To see an event before children can stop it — global "close menus on any click", focus tracking.

### TL;DR
Capture (down, `{capture:true}`) → target → bubble (up, default). `focus`, `mouseenter`, element `load` don't bubble. Stop sparingly.

---

## 13.7 Event delegation

### What it is
**Event delegation**: **one** listener on a common ancestor instead of one per child, using `event.target` (usually with `closest`) to find which child was acted on. It works because events bubble.

### The problem it solves
1,000 rows with 1,000 listeners cost memory and setup time, and items added later (from `fetch` or the user) need listeners attached — and removed ones cleaned up. With delegation, new items are handled automatically.

### How it works
1. Attach a listener to a stable container (`ul`, `table`, a component root, or `document`).
2. In the handler, `const item = event.target.closest('selector')`.
3. Ignore `null` (click elsewhere) or items outside the container (`container.contains(item)`).
4. Read what you need (`item.dataset.id`) and act.

[`code/07_delegation.mjs`](code/07_delegation.mjs):

```js
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

list.querySelector('li[data-id="1"] span').click();      // click on the text (a child of li)
document.getElementById('add').click();                   // adds item 3 AFTER the listener was attached
list.querySelector('li[data-id="3"] span').click();      // still handled - no new listener needed
list.querySelector('li[data-id="2"] .del').click();
```

Output:
```
toggle item 1 -> done = true
toggle item 3 -> done = true
delete item 2
remaining: [ '1(done)', '3(done)' ]
listeners attached to items: 0, to the list: 1
```

As a real page, [`code/todo_delegation.html`](code/todo_delegation.html): one `click` listener on the `<ul>`, one `submit` listener on the form (with `preventDefault`), new items built with `textContent` — so a task typed as `Call <b>mom</b>` shows literally. After adding two tasks, ticking two and deleting one:

![A todo app: two struck-through done items, one item showing the literal text Call <b>mom</b>, and an event log listing add, toggle and delete actions with target SPAN or BUTTON and currentTarget todos](img/todo_delegation.png)

*Figure — `target` is the inner `SPAN`/`BUTTON`; `currentTarget` is always the list.*

### Why `closest` and not `event.target` directly
`event.target` is the **deepest** element clicked — the `<span>` in the `<li>`, an `<svg>` icon in a button. `closest('li')` climbs to the element you care about.

### Trade-offs
| | Delegation | Listener per element |
|---|---|---|
| Listeners | One | N |
| Dynamic items | Handled automatically | Must attach/detach |
| Non-bubbling events | Needs the bubbling twin (`focusin`) or capture | Works directly |
| Handler complexity | Must route by selector | Simple |

### TL;DR
One listener on the container + `event.target.closest(selector)` handles current and future children. Use for lists, tables, menus.

---

## 13.8 Default actions and `preventDefault`

### What it is
Many events have a **default action** the browser performs after listeners run: a link navigates, a form submits (reloading the page), a checkbox toggles, a key types, a wheel scrolls, a right-click opens the context menu. `event.preventDefault()` cancels it if the event is **cancelable** (most user-input events are). `dispatchEvent` returns `false` when a listener cancelled it.

| Event | Default action | Typical reason to prevent |
|---|---|---|
| `submit` on a form | Send the form and navigate | Validate and send with `fetch` |
| `click` on `<a href>` | Navigate | Client-side routing (change view and URL without reload; [History API, Ch. 14](../14-browser-apis-and-security/README.md#147-the-history-api)) |
| `keydown` | Insert the character / browser shortcut | Custom shortcut like Ctrl+S |
| `contextmenu` | Show the browser menu | Custom context menu |
| `dragover` | Refuse drops | Allow dropping files |

(`preventDefault` output lines are in the forms program, §13.9.)

### Passive listeners
For `touchstart`, `touchmove` and `wheel`, the browser must wait for your listener before scrolling in case you call `preventDefault()` — janky scrolling. `{ passive: true }` promises you won't, so scrolling starts immediately. Browsers other than Safari already default `passive` to `true` for these on `window`, `document` or `body`; `preventDefault()` in a passive listener is ignored (with a console warning).

### Common mistakes
- `return false` in an `addEventListener` handler does nothing (works only in `on…` properties).
- Preventing link/button defaults and forgetting keyboard and screen-reader users — use real `<button>`s for actions and `<a>` for navigation.
- `preventDefault()` does **not** stop propagation; `stopPropagation()` does **not** prevent the default.

### TL;DR
`preventDefault()` cancels the browser's built-in response (navigation, submit, typing, scrolling). Passive listeners for scroll/touch.

---

## 13.9 Forms

### What it is
- `form.elements` — controls by `name` or index (`form.elements.email`).
- **`submit` event** on the form (fires for Enter in a field and for submit buttons) — listen here, not on the button's `click`.
- **`FormData`** — reads every successful control (named, enabled, checked where relevant) into key/value entries; usable directly as a `fetch` body.
- The **Constraint Validation API** — `required`, `type="email"`, `min`, `max`, `minlength`, `pattern` produce a `validity` object; `checkValidity()`, `reportValidity()`, `setCustomValidity(msg)`.
- **`input`** fires on every value change; **`change`** when the change is committed (on blur for text, immediately for checkboxes/selects).

[`code/08_forms.mjs`](code/08_forms.mjs) — key parts:

```js
age.value = '7';
console.log('1) form valid?', form.checkValidity());
for (const el of [email, age]) {
  const v = el.validity;
  console.log(`   ${el.name}: valid=${v.valid} valueMissing=${v.valueMissing} rangeUnderflow=${v.rangeUnderflow} msg="${el.validationMessage}"`);
}
email.value = 'not-an-email';
console.log('   typeMismatch for "not-an-email":', email.validity.typeMismatch);

email.value = 'ada@example.com';
email.setCustomValidity(email.value.endsWith('@example.com') ? 'Please use your work email' : '');
console.log('2) customError:', email.validity.customError, '|', email.validationMessage);
email.setCustomValidity('');                  // '' clears the custom error

form.addEventListener('submit', (event) => {
  event.preventDefault();                      // stay on the page, send with fetch instead
  const data = new FormData(form);
  console.log('3) submit prevented:', event.defaultPrevented);
  console.log('   entries   :', JSON.stringify([...data.entries()]));
  console.log('   getAll    :', data.getAll('topics'));
  console.log('   as object :', JSON.stringify(Object.fromEntries(data)), '<- keeps only the LAST topic');
  console.log('   submitter :', event.submitter?.textContent);
});
form.elements.topics[1].checked = true;
form.requestSubmit(form.querySelector('button'));  // like clicking the button: validates, fires submit

age.addEventListener('input', () => log.push(`input(${age.value})`));
age.addEventListener('change', () => log.push(`change(${age.value})`));
// ... two input events, then one change event

const a = document.createElement('a'); a.href = '/leave';
a.addEventListener('click', (e) => e.preventDefault());
console.log('5) link click not cancelled?', a.dispatchEvent(new MouseEvent('click', { cancelable: true })));
cb.addEventListener('click', (e) => e.preventDefault());
cb.click();
console.log('   checkbox still checked after prevented click:', cb.checked);
```

Output:
```
1) form valid? false
   email: valid=false valueMissing=true rangeUnderflow=false msg="Constraints not satisfied"
   age: valid=false valueMissing=false rangeUnderflow=true msg="Constraints not satisfied"
   typeMismatch for "not-an-email": true
2) customError: true | Please use your work email
   now valid? true
3) submit prevented: true
   entries   : [["email","ada@example.com"],["age","36"],["topics","js"],["topics","css"],["plan","pro"],["bio","Hello"]]
   getAll    : [ 'js', 'css' ]
   as object : {"email":"ada@example.com","age":"36","topics":"css","plan":"pro","bio":"Hello"} <- keeps only the LAST topic
   submitter : Sign up
4) input(4) input(42) change(42)
5) link click not cancelled? false
   checkbox still checked after prevented click: true
```

(jsdom's `validationMessage` is a generic "Constraints not satisfied"; browsers give localized messages like "Please fill out this field.")

### The `validity` flags
| Flag | Set when | Caused by |
|---|---|---|
| `valueMissing` | Empty required field | `required` |
| `typeMismatch` | Not a valid email/URL | `type="email"`, `type="url"` |
| `patternMismatch` | Regex doesn't match the whole value | `pattern` |
| `tooShort` / `tooLong` | Length out of range (after user edits) | `minlength` / `maxlength` |
| `rangeUnderflow` / `rangeOverflow` / `stepMismatch` | Number/date out of range | `min` / `max` / `step` |
| `badInput` | Browser can't convert input (letters in a number field) | input type |
| `customError` | `setCustomValidity('msg')` with a non-empty message | your code |
| `valid` | None of the above | — |

[`code/form_validation.html`](code/form_validation.html) uses `novalidate` (turns off the browser's bubbles) and renders its own accessible messages from these flags — `aria-invalid` on the field, `aria-describedby` pointing to the message (ARIA attributes describe state and relationships to assistive technology such as screen readers). One delegated `input` listener gives live feedback; the `submit` handler checks everything. After a scripted submit with a personal email, age 9 and no password:

![A sign-up form with three red-bordered fields and messages: use your work email, you must be at least 13, this field is required; status says fix the highlighted fields](img/form_validation.png)

*Figure — custom validation messages driven by the Constraint Validation API (headless Chrome).*

### Common mistakes
- Listening to the submit button's `click` → misses Enter-key submits. Listen to `submit`.
- `Object.fromEntries(new FormData(form))` keeps only the last value of multi-value fields (checkbox groups, `<select multiple>`); use `getAll`.
- Treating client-side validation as security — it's UX (user experience) only; **the server must validate everything again**.
- Missing `name` attributes — unnamed controls are left out of `FormData`.
- `form.submit()` skips validation *and* the `submit` event; `requestSubmit()` behaves like a real submit.

### TL;DR
Listen to `submit`, `preventDefault`, read with `FormData`, validate with `validity`/`setCustomValidity` for UX, re-validate on the server. `input` = every keystroke, `change` = committed value.

---

## 13.10 Watching the DOM: `MutationObserver`

### What it is
`MutationObserver` calls you back when the DOM changes — children added/removed, attributes changed, text edited. Records are **batched** and delivered as a **microtask** after the current script ([Ch. 12 §12.2](../12-asynchronous-javascript/README.md#122-the-event-loop)). It replaced the old, slow "mutation events".

[`code/09_mutation_observer.mjs`](code/09_mutation_observer.mjs):

```js
const observer = new MutationObserver((records) => {
  console.log(`observer got ${records.length} records:`,
    records.map((r) => r.type === 'childList' ? `childList +${r.addedNodes.length} -${r.removedNodes.length}` : `${r.type}:${r.attributeName}`));
});
observer.observe(feed, { childList: true, attributes: true, subtree: true });

for (let i = 1; i <= 3; i++) feed.insertAdjacentHTML('beforeend', `<li>post ${i}</li>`);
feed.firstElementChild.remove();
feed.setAttribute('data-count', '2');
console.log('sync code done - no callback yet (it is a microtask)');
await null;
observer.disconnect();
```

Output:
```
sync code done - no callback yet (it is a microtask)
observer got 5 records: [
  'childList +1 -0',
  'childList +1 -0',
  'childList +1 -0',
  'childList +0 -1',
  'attributes:data-count'
]
done
```

Uses: reacting to DOM changes by third-party code (ads, chat widgets), auto-initialising widgets when they appear, test utilities that wait for elements (Testing Library's `waitFor`/`findBy*`). Don't use it to watch your own changes. Size/visibility observers (`ResizeObserver`, `IntersectionObserver`): [Chapter 14 §14.9](../14-browser-apis-and-security/README.md#149-intersectionobserver-and-resizeobserver).

### TL;DR
`new MutationObserver(cb).observe(node, { childList, attributes, subtree, characterData })`; batched microtask callbacks; `disconnect()` when done.

---

## 13.11 Rendering performance: reflow, repaint and layout thrashing

### What it is
After DOM or style changes, the browser's **rendering pipeline** works out what to draw:

```mermaid
flowchart LR
    JS["JavaScript<br/>DOM and class changes"] --> ST["Style<br/>which CSS rules apply"]
    ST --> L["Layout (reflow)<br/>sizes and positions"]
    L --> P["Paint (repaint)<br/>pixels per layer"]
    P --> C["Composite<br/>layers onto screen"]
```

- **Reflow / layout** — computing geometry. Changing width, height, font size, adding elements dirties layout — often for the whole subtree or document.
- **Repaint** — redrawing pixels without geometry changes (color, background, shadow).
- **Composite** — assembling layers (separately drawn sheets the GPU stacks); changing only `transform` and `opacity` can often skip layout and paint — hence the go-to animation properties.

The browser normally **batches** your changes and runs the pipeline once before the next frame (§12.2 step 3). But **reading** geometry (`offsetWidth`, `offsetTop`, `clientHeight`, `scrollTop`, `getBoundingClientRect()`, `getComputedStyle(...)` values) after a write forces layout **synchronously** — a **forced synchronous layout**. Write → read → write → read in a loop forces one per iteration: **layout thrashing**.

### Measured
[`code/reflow.html`](code/reflow.html) widens 2,000 boxes by 10 px two ways (median of 7 runs, headless Chrome):

```js
// Interleaved (thrashing): each read forces a layout because of the previous write
for (const b of boxes) { const w = b.offsetWidth; b.style.width = (w + 10) + 'px'; }

// Batched: all reads (one layout), then all writes (one layout before the next frame)
const ws = boxes.map((b) => b.offsetWidth);
boxes.forEach((b, i) => { b.style.width = (ws[i] + 10) + 'px'; });
```

![Benchmark table: interleaved read/write 346.3 ms versus batched reads then writes 1.5 ms, about 231 times slower](img/reflow.png)

*Figure — same result, two orders of magnitude apart (one run; exact numbers vary, the gap doesn't).*

### How to keep rendering fast
1. **Batch reads, then writes** — never interleave in loops (fastdom formalizes this).
2. **Change classes, not many individual styles.**
3. **Build off-DOM** (fragment/template), insert once (§13.3).
4. **Animate `transform` and `opacity`**, not `top`/`left`/`width`; `requestAnimationFrame` for JS animation ([Ch. 14 §14.5](../14-browser-apis-and-security/README.md#145-timers-and-requestanimationframe)).
5. **Debounce/throttle** high-frequency handlers (`scroll`, `resize`, `input`); passive listeners.
6. **Virtualize** long lists (render only visible rows); CSS `content-visibility: auto` for long off-screen sections.
7. **Measure** in the DevTools Performance panel — purple "Layout" blocks with a "Forced reflow" warning point at the culprit.

### In the interview
**30-second answer:** "The browser recalculates style, layout, paint and composite. It batches DOM writes until the next frame, but reading geometry like `offsetWidth` right after a write forces a synchronous layout. Doing that in a loop is layout thrashing. The fix is to batch all reads, then all writes, animate with `transform`/`opacity`, and use `requestAnimationFrame`."

- *Q: Reflow vs repaint?* A: Reflow recomputes geometry (expensive, cascades); repaint redraws pixels without geometry changes.
- *Q: Which properties avoid layout?* A: `transform` and `opacity` typically only need compositing.

### TL;DR
Writes are cheap until you read layout. Batch reads then writes; animate `transform`/`opacity`; measure in DevTools.

---

## 13.12 When scripts run: `DOMContentLoaded`, `load` and `defer`

A script can only find elements already parsed.

| Moment | Event | Meaning |
|---|---|---|
| HTML parsed, DOM ready, deferred scripts run | `DOMContentLoaded` on `document` | Safe to query any element |
| Everything loaded (images, styles, iframes) | `load` on `window` | Needed only for image sizes etc. |
| User leaves / tab hidden | `pagehide`, `visibilitychange` | Save state; `beforeunload` only to warn about unsaved changes |

| Tag | Blocks parsing? | Runs when | Order |
|---|---|---|---|
| `<script src>` in `<head>` | Yes (download + run) | Immediately | Document order |
| `<script defer src>` | No | After parsing, before `DOMContentLoaded` | Document order |
| `<script async src>` | No (pauses only to run) | As soon as downloaded | Unpredictable |
| `<script type="module">` | No | Deferred by default | Document order |

**Recommended:** scripts in `<head>` with `defer` (or `type="module"`, deferred automatically — [Chapter 10 §10.7](../10-modules-and-tooling/README.md#107-modules-in-the-browser)): the DOM is ready when your code runs, no `DOMContentLoaded` wrapper needed. `async` for independent scripts like analytics.

```html
<!-- (HTML — not run) -->
<head>
  <script src="app.js" defer></script>
</head>
```

### TL;DR
`defer` or modules so scripts run after parsing. `DOMContentLoaded` = DOM ready; `load` = all resources loaded.

---

## Chapter 13 TL;DR

- **DOM** = live tree of nodes (element, text, comment) built by the parser; element-only navigation (`children`, `nextElementSibling`).
- **Select** with `querySelector(All)` / `getElementById`; relate with `closest`, `matches`, `contains`. `querySelectorAll` static; `getElementsBy*` live.
- **Modify** with `createElement`, `append`/`prepend`/`before`/`after`/`replaceWith`/`remove`, `replaceChildren`. `textContent` for data; `innerHTML` only for trusted markup. Fragments and `<template>` for bulk rendering.
- **Attributes vs properties**: initial HTML strings vs live typed state. `classList`, `dataset`, `style`, `getComputedStyle`.
- **Events**: `addEventListener(type, fn, { once, passive, signal, capture })`; `target` vs `currentTarget`; `CustomEvent` + `dispatchEvent` (synchronous).
- **Propagation**: capture ↓, target, bubble ↑; stop sparingly; `focus`/`mouseenter` don't bubble; `new Event` needs `{ bubbles: true }`.
- **Delegation**: one container listener + `event.target.closest()`.
- **`preventDefault`** cancels default actions; passive listeners for scroll/touch.
- **Forms**: `submit`, `FormData` (`getAll` for multi-values), `validity`, `setCustomValidity`; `requestSubmit()` not `submit()`; re-validate on the server.
- **MutationObserver**: batched microtask callbacks.
- **Performance**: batch reads then writes (thrashing measured ~230× slower), animate `transform`/`opacity`, `requestAnimationFrame`.
- **Loading**: `defer`/modules; `DOMContentLoaded` vs `load`.

Next: talking to servers, storing data, keeping pages safe — [Chapter 14: Browser APIs and Security](../14-browser-apis-and-security/README.md).
