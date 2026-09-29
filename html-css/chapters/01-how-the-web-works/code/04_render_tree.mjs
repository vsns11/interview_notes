// DOM + CSSOM -> which nodes get boxes? (jsdom computes the cascaded 'display'/'visibility' for us.)
import { JSDOM } from 'jsdom';
const html = `<!DOCTYPE html><html><head>
<style>
  .hidden { display: none; }
  .ghost  { visibility: hidden; }
  .faded  { opacity: 0; }
  p       { color: #333; }
</style></head><body>
  <h1>Title</h1>
  <p>Normal paragraph</p>
  <p class="hidden">display: none</p>
  <p class="ghost">visibility: hidden</p>
  <p class="faded">opacity: 0</p>
  <script>/* scripts never render */</script>
</body></html>`;
const { window } = new JSDOM(html);
const { document } = window;

console.log('CSSOM rules:');
for (const rule of document.styleSheets[0].cssRules) console.log('  ' + rule.cssText);

console.log('\nElement           display  visibility  in render tree?  painted visibly?');
for (const el of document.body.children) {
  const cs = window.getComputedStyle(el);
  const inTree = cs.display !== 'none';
  const visible = inTree && cs.visibility !== 'hidden' && cs.opacity !== '0';
  const label = `<${el.localName}${el.className ? ' .' + el.className : ''}>`;
  console.log(label.padEnd(18), cs.display.padEnd(8), cs.visibility.padEnd(11), String(inTree).padEnd(16), visible);
}
