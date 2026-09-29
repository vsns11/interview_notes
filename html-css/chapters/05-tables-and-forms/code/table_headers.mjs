// What a screen reader announces for each data cell: we rebuild the table grid (respecting
// rowspan/colspan) and look up the header cells that apply, using the scope attribute.
// This is a simplified version of the HTML "assigning header cells" algorithm (enough for scope).
// Run: node table_headers.mjs
import '../../../../tools_dom.mjs';
import { readFileSync } from 'fs';

document.body.innerHTML = readFileSync('01_table.html', 'utf8').match(/<body>([\s\S]*)<\/body>/)[1];

function grid(table) {                       // grid[r][c] = the cell element covering that slot
  const g = [];
  [...table.rows].forEach((tr, r) => {
    g[r] ??= [];
    let c = 0;
    for (const cell of tr.cells) {
      while (g[r][c]) c++;                   // skip slots taken by a rowspan from above
      for (let dr = 0; dr < cell.rowSpan; dr++)
        for (let dc = 0; dc < cell.colSpan; dc++) ((g[r + dr] ??= [])[c + dc] = cell);
      c += cell.colSpan;
    }
  });
  return g;
}

function headersFor(g, r, c) {
  const found = [];
  const add = h => { if (!found.includes(h)) found.push(h); };
  for (let rr = 0; rr < r; rr++) {           // look up the column
    const h = g[rr][c];
    if (h.tagName === 'TH' && ['col', 'colgroup'].includes(h.scope)) add(h);
  }
  for (let cc = 0; cc < c; cc++) {           // look left along the row
    const h = g[r][cc];
    if (h.tagName === 'TH' && ['row', 'rowgroup'].includes(h.scope)) add(h);
  }
  return found.map(h => h.textContent.trim());
}

for (const table of document.querySelectorAll('table')) {
  console.log('Table:', table.caption.textContent.trim());
  const g = grid(table);
  g.forEach((row, r) => row.forEach((cell, c) => {
    const first = row.indexOf(cell) === c && g.findIndex(rw => rw[c] === cell) === r;
    if (cell.tagName === 'TD' && first && cell.closest('tbody'))
      console.log(`  ${cell.textContent.padStart(4)}  <- ${headersFor(g, r, c).join(' / ')}`);
  }));
}
