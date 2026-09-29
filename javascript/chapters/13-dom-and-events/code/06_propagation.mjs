import '../../../../tools_dom.mjs';
document.body.innerHTML = '<div id="outer"><div id="inner"><button id="btn">Click</button></div></div>';
const els = ['outer', 'inner', 'btn'].map((id) => document.getElementById(id));
const PHASE = { 1: 'CAPTURE', 2: 'TARGET', 3: 'BUBBLE' };
let log = [];

for (const el of [window, document, ...els]) {
  const name = el === window ? 'window' : el === document ? 'document' : el.id;
  el.addEventListener('click', (e) => log.push(`${name}:${PHASE[e.eventPhase]}`), { capture: true });
  el.addEventListener('click', (e) => log.push(`${name}:${PHASE[e.eventPhase]}`));
}

document.getElementById('btn').click();
console.log('1) full path:\n   ' + log.join(' > '));

// 2) stopPropagation on inner (bubble phase): outer never hears it
log = [];
const stop = (e) => e.stopPropagation();
els[1].addEventListener('click', stop);
els[2].click();
console.log('2) stopPropagation at inner:\n   ' + log.join(' > '));
els[1].removeEventListener('click', stop);

// 3) stopImmediatePropagation also skips the remaining listeners on the SAME element
log = [];
const btn = els[2];
btn.addEventListener('click', (e) => { log.push('btn:first-extra'); e.stopImmediatePropagation(); });
btn.addEventListener('click', () => log.push('btn:NEVER'));
btn.click();
console.log('3) stopImmediatePropagation:\n   ' + log.join(' > '));

// 4) Not every event bubbles
const probe = (type) => { const e = new Event(type); return `${type}: bubbles=${e.bubbles}`; };
console.log('4) new Event() defaults to', probe('x'));
const input = document.createElement('input');
document.body.append(input);
const seen = [];
document.body.addEventListener('focus', () => seen.push('focus (bubble listener)'));
document.body.addEventListener('focusin', () => seen.push('focusin'));
input.focus();
console.log('   body heard:', seen, '<- focus does not bubble, focusin does');
