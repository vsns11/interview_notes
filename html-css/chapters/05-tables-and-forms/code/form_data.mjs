// What exactly does a form send? FormData collects the "successful controls" the browser
// would submit; URLSearchParams shows the GET query string / urlencoded POST body.
// Run: node form_data.mjs
import '../../../../tools_dom.mjs';
const { FormData } = window;          // the DOM's FormData (Node also has its own, which can't read forms)

document.body.innerHTML = `
<form action="/signup" method="get">
  <input name="name" value="Ada Lovelace">
  <input name="email" type="email" value="ada@example.com">
  <input name="nick">                                   <!-- empty text: still sent, as "" -->
  <input type="checkbox" name="newsletter" value="yes" checked>
  <input type="checkbox" name="terms" value="yes">       <!-- unchecked: NOT sent -->
  <input type="radio" name="plan" value="free">
  <input type="radio" name="plan" value="pro" checked>
  <select name="skills" multiple>
    <option selected>HTML</option><option selected>CSS</option><option>JS</option>
  </select>
  <input name="coupon" value="SAVE10" disabled>          <!-- disabled: NOT sent -->
  <input name="country" value="IN" readonly>             <!-- readonly: sent -->
  <input value="no name attribute">                      <!-- no name: NOT sent -->
  <input type="hidden" name="source" value="ch5">
  <textarea name="bio">Hi &amp; bye</textarea>
</form>`;

const form = document.querySelector('form');
const data = new FormData(form);
console.log('FormData entries:');
for (const [k, v] of data) console.log(`  ${k.padEnd(10)} = ${JSON.stringify(v)}`);
console.log('\ndata.getAll("skills") =', data.getAll('skills'));
console.log('\nGET URL:\n  ' + form.action + '?' + new URLSearchParams(data));
