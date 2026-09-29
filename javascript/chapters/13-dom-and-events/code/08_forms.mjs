import dom from '../../../../tools_dom.mjs';
const { FormData } = dom.window;
document.body.innerHTML = `
<form id="signup" novalidate>
  <label for="email">Email</label>
  <input id="email" name="email" type="email" required>
  <label for="age">Age</label>
  <input id="age" name="age" type="number" min="13" max="120">
  <label><input type="checkbox" name="topics" value="js" checked> JS</label>
  <label><input type="checkbox" name="topics" value="css"> CSS</label>
  <select name="plan"><option value="free">Free</option><option value="pro" selected>Pro</option></select>
  <textarea name="bio">Hello</textarea>
  <button>Sign up</button>
</form>`;
const form = document.getElementById('signup');
const email = form.elements.email, age = form.elements.age;

// 1) Validation API (novalidate = we handle messages ourselves)
age.value = '7';
console.log('1) form valid?', form.checkValidity());
for (const el of [email, age]) {
  const v = el.validity;
  console.log(`   ${el.name}: valid=${v.valid} valueMissing=${v.valueMissing} rangeUnderflow=${v.rangeUnderflow} msg="${el.validationMessage}"`);
}
email.value = 'not-an-email';
console.log('   typeMismatch for "not-an-email":', email.validity.typeMismatch);

// 2) Custom rule with setCustomValidity ('' clears it)
email.value = 'ada@example.com';
email.setCustomValidity(email.value.endsWith('@example.com') ? 'Please use your work email' : '');
console.log('2) customError:', email.validity.customError, '|', email.validationMessage);
email.setCustomValidity('');
age.value = '36';
console.log('   now valid?', form.checkValidity());

// 3) submit event: preventDefault stops the page navigation; FormData reads every field
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

// 4) input vs change
const log = [];
age.addEventListener('input', () => log.push(`input(${age.value})`));
age.addEventListener('change', () => log.push(`change(${age.value})`));
for (const ch of ['4', '42']) { age.value = ch; age.dispatchEvent(new Event('input', { bubbles: true })); }
age.dispatchEvent(new Event('change', { bubbles: true }));   // browsers fire this on commit (blur/Enter)
console.log('4)', log.join(' '));

// 5) preventDefault on other events: a link and a checkbox
const a = document.createElement('a'); a.href = '/leave';
a.addEventListener('click', (e) => e.preventDefault());
console.log('5) link click not cancelled?', a.dispatchEvent(new MouseEvent('click', { cancelable: true })));
const cb = form.elements.topics[0];
cb.addEventListener('click', (e) => e.preventDefault());
cb.click();
console.log('   checkbox still checked after prevented click:', cb.checked);
