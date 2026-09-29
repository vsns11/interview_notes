// Prints the file size of the same picture saved in four formats (files made by make_assets.py),
// and how much smaller each is than the PNG. Run: node format_sizes.mjs
import { statSync } from 'fs';
for (const stem of ['hills-800', 'photo-800']) {
  const png = statSync(`media/${stem}.png`).size;
  console.log(`${stem}  (${stem.startsWith('hills') ? 'flat graphic' : 'photo-like grain'})`);
  for (const ext of ['png', 'jpg', 'webp', 'avif']) {
    const kb = statSync(`media/${stem}.${ext}`).size / 1024;
    const vsPng = ((kb * 1024) / png * 100).toFixed(0);
    console.log(`  ${ext.padEnd(5)} ${kb.toFixed(1).padStart(7)} KB   ${vsPng.padStart(4)}% of PNG`);
  }
}
