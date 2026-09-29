// What a build tool does for browsers without nesting: Lightning CSS "lowers" nested CSS to flat CSS.
// targets = Chrome 100 (released before nesting, which arrived in Chrome 112/120).
import { transform } from 'lightningcss';

const src = `
.card {
  padding: 16px;
  .title { font-size: 20px; }
  &:hover { border-color: #2563eb; }
  .dark-page & { background: #0f172a; }
  @media (width >= 600px) { padding: 24px; }
}
#main, .sidebar { .link { color: crimson; } }`;

const { code } = transform({ filename: 'in.css', code: Buffer.from(src), targets: { chrome: 100 << 16 } });
console.log(code.toString());
