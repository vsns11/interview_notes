import '../../../../tools_dom.mjs';
// `this` inside DOM event listeners (jsdom stands in for the browser).

document.body.innerHTML = '<button id="save">Save</button>';
const btn = document.getElementById('save');

btn.addEventListener('click', function (event) {
  // regular function: this === event.currentTarget (the element the listener is on)
  console.log('function listener this:', this.id, '| same as currentTarget?', this === event.currentTarget);
});
btn.addEventListener('click', () => {
  // arrow function: this comes from the module scope -> undefined
  console.log('arrow listener this   :', this);
});

class Form {
  constructor(el) {
    this.name = 'SignupForm';
    el.addEventListener('click', this.handle);                 // loses this
    el.addEventListener('click', (e) => this.handle2(e));      // arrow keeps this
    el.addEventListener('click', this.handle3.bind(this));     // bind keeps this
  }
  handle() { console.log('method passed directly:', `this is the <${this.tagName.toLowerCase()}> element, this.name =`, JSON.stringify(this.name)); }
  handle2() { console.log('wrapped in arrow      :', this.name); }
  handle3() { console.log('bound                 :', this.name); }
}
new Form(btn);
btn.click();
