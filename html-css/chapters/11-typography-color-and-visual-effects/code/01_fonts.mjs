// Did the web font load, which weights does the one file serve, and does the weight really change the glyphs?
import { withPage } from './measure.mjs';

const r = await withPage('01_fonts.html', page => page.evaluate(async () => {
  await document.fonts.load('400 22px "Inter Var"');
  const faces = [...document.fonts].map(f => `${f.family} weight=${f.weight} status=${f.status} display=${f.display}`);
  // Width of the same word at several weights: a variable font changes the outlines, so widths change
  const c = document.createElement('canvas').getContext('2d');
  const widths = [100, 400, 700, 900].map(w => { c.font = `${w} 40px "Inter Var"`; return `${w}: ${c.measureText('Hamburgefonstiv').width.toFixed(1)}px`; });
  return { faces, widths,
           check700: document.fonts.check('700 22px "Inter Var"'),
           synth: getComputedStyle(document.querySelectorAll('.inter')[2]).fontSynthesis };
}));
console.log(r.faces.join('\n'));
console.log('document.fonts.check("700 22px Inter Var"):', r.check700);
console.log('"Hamburgefonstiv" at 40px, width by weight ->', r.widths.join(', '));
console.log('font-synthesis on the last sample:', r.synth);
