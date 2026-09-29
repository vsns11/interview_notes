// The same page under emulated OS preferences: which colours the browser actually uses.
import { withPage } from './measure.mjs';

const cases = [
  ['light (default)', []],
  ['dark', [{ name: 'prefers-color-scheme', value: 'dark' }]],
  ['light + more contrast', [{ name: 'prefers-contrast', value: 'more' }]],
];
for (const [label, media] of cases) {
  const r = await withPage('03_preferences.html', page => page.evaluate(() => {
    const cs = s => getComputedStyle(document.querySelector(s));
    return { body: cs('body').backgroundColor, text: cs('body').color,
             border: cs('.card').borderTopColor, note: cs('.note').color,
             input: cs('input').backgroundColor };
  }), { media });
  console.log(`${label.padEnd(22)} bg ${r.body}  text ${r.text}  card border ${r.border}  note ${r.note}  <input> bg ${r.input}`);
}
