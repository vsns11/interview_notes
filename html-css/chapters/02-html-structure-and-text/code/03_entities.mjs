// Character references are decoded by the parser; serializing escapes the dangerous characters again.
import '../../../../tools_dom.mjs';

const p = document.createElement('p');
p.innerHTML = 'Fish &amp; chips &lt;b&gt; &copy; &#169; &#xA9; 10&nbsp;km &mdash; café';
console.log('textContent :', JSON.stringify(p.textContent));
console.log('nbsp code   :', p.textContent.codePointAt(p.textContent.indexOf('km') - 1).toString(16));
console.log('innerHTML   :', p.innerHTML);

// Setting text (not HTML) is always safe: the browser escapes it for you.
const q = document.createElement('p');
q.textContent = '<img src=x onerror=alert(1)> & "quotes"';
console.log('\nescaped     :', q.innerHTML);
console.log('elements    :', q.children.length, '(the tag became text, not an element)');

// An ampersand that does not start a known reference is left alone — but don't rely on it.
const r = document.createElement('p');
r.innerHTML = 'AT&T, R&D, a&b=1';
console.log('\nbare &      :', r.textContent);

// ...but a few legacy names (like &copy) are decoded even without the semicolon — in text, not in attributes.
const s = document.createElement('div');
s.innerHTML = '<p>price&copy=1</p><a href="?a=1&copy=2">x</a>';
console.log('in text     :', s.querySelector('p').textContent, '| in href:', s.querySelector('a').getAttribute('href'));
