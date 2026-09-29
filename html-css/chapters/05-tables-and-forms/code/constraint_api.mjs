// The Constraint Validation API from JavaScript (runs under jsdom, which implements it).
// Run: node constraint_api.mjs
import '../../../../tools_dom.mjs';

document.body.innerHTML = `
<form>
  <input id="a" required>
  <input id="b" type="email" value="not-an-email">
  <input id="c" type="number" min="1" max="5" value="9">
  <input id="d" pattern="[A-Z]{3}" value="abc">
  <input id="e" pattern="[A-Z]{3}" value="">
  <input id="f" required disabled>
  <input id="g" minlength="5" value="ab">
</form>`;

const flags = ['valueMissing', 'typeMismatch', 'patternMismatch', 'tooShort', 'rangeOverflow', 'customError'];
const report = el => `${el.id}: willValidate=${el.willValidate} valid=${el.validity.valid} ` +
  (flags.filter(f => el.validity[f]).join(',') || '-');

for (const el of document.querySelectorAll('input')) console.log(report(el));

// setCustomValidity: any non-empty string makes the field invalid (customError)
const d = document.getElementById('d');
d.value = 'ABC';
console.log('\nafter d.value = "ABC":           ', report(d));
d.setCustomValidity('That code is already taken.');
console.log('after setCustomValidity("..."):  ', report(d), '|', d.validationMessage);
d.setCustomValidity('');
console.log('after setCustomValidity(""):     ', report(d));

// checkValidity() fires an "invalid" event on each failing control
const form = document.querySelector('form');
let invalidEvents = 0;
form.addEventListener('invalid', () => invalidEvents++, true);   // capture: invalid doesn't bubble
console.log('\nform.checkValidity() =', form.checkValidity(), '| invalid events fired:', invalidEvents);
