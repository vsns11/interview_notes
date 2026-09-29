import '../../../../tools_dom.mjs';

document.body.innerHTML = `
<nav id="menu">
  <ul class="links">
    <li><a href="/" class="active">Home</a></li>
    <li><a href="/blog">Blog</a></li>
    <li><a href="/about" data-track="about-click">About</a></li>
  </ul>
</nav>
<form name="login"><input name="email" type="email"><input name="pw" type="password"></form>`;

// 1) Selecting
console.log('getElementById      :', document.getElementById('menu').id);
console.log('querySelector       :', document.querySelector('.links a').textContent);        // first match
console.log('querySelectorAll    :', [...document.querySelectorAll('.links a')].map((a) => a.textContent));
console.log('attribute selector  :', document.querySelector('[data-track]').textContent);
console.log(':not + :nth-child   :', [...document.querySelectorAll('li:not(:first-child) a')].map((a) => a.textContent));
console.log('scoped query        :', document.getElementById('menu').querySelectorAll('a').length);
console.log('forms collection    :', document.forms.login.elements.email.type);
console.log('no match            :', document.querySelector('.nope'), document.querySelectorAll('.nope').length);

// 2) Static NodeList vs live HTMLCollection
const staticList = document.querySelectorAll('li');          // snapshot
const liveList = document.getElementsByTagName('li');         // live
document.querySelector('ul').append(document.createElement('li'));
console.log('\nafter adding an <li>: static', staticList.length, '| live', liveList.length);
console.log('NodeList has forEach:', typeof staticList.forEach, '| HTMLCollection has forEach:', typeof liveList.forEach);

// 3) Traversing
const blog = document.querySelector('a[href="/blog"]');
console.log('\nparentElement       :', blog.parentElement.tagName);
console.log('closest("nav")      :', blog.closest('nav').id);                      // nearest ancestor (or itself)
console.log('closest(".missing") :', blog.closest('.missing'));
console.log('matches(":not(.x)") :', blog.matches('a:not(.active)'));
const li = blog.parentElement;
console.log('previousElementSibling:', li.previousElementSibling.textContent.trim());
console.log('nextElementSibling  :', li.nextElementSibling.textContent.trim());
console.log('nextSibling (a node):', JSON.stringify(li.nextSibling.textContent), '<- whitespace text node');
console.log('firstElementChild   :', document.querySelector('ul').firstElementChild.textContent.trim());
console.log('contains            :', document.getElementById('menu').contains(blog));

// 4) Invalid selectors throw a SyntaxError DOMException
try { document.querySelector('a[href=/]'); } catch (e) { console.log('\nbad selector        :', e.name); }
try { document.querySelector('#123'); } catch (e) { console.log('id starting w/ digit:', e.name, '-> use getElementById("123") or CSS.escape'); }
