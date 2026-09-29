// tools_run: skip  (component used by 07_virtualization.jsx and virtual-main.jsx)
// A hand-rolled "windowing" list: only the rows inside the visible window (+ a few extra) exist in the DOM.
import { useState } from 'react';

export function VirtualList({ items, rowHeight, height, overscan = 3, renderRow }) {
  const [scrollTop, setScrollTop] = useState(0);

  // 1. Which rows are visible? Pure arithmetic - every row has the same height.
  const first = Math.max(0, Math.floor(scrollTop / rowHeight) - overscan);
  const visibleCount = Math.ceil(height / rowHeight) + 2 * overscan;
  const last = Math.min(items.length - 1, first + visibleCount - 1);

  // 2. Render only those rows, absolutely positioned inside a spacer as tall as the WHOLE list,
  //    so the scrollbar looks and behaves as if all rows were there.
  const rows = [];
  for (let i = first; i <= last; i++) {
    rows.push(
      <div key={i} role="listitem" className="row"
           style={{ position: 'absolute', top: i * rowHeight, height: rowHeight, left: 0, right: 0 }}>
        {renderRow(items[i], i)}
      </div>
    );
  }
  return (
    <div role="list" aria-label="virtual list" className="viewport"
         style={{ height, overflowY: 'auto', position: 'relative' }}
         onScroll={e => setScrollTop(e.currentTarget.scrollTop)}>
      <div style={{ height: items.length * rowHeight, position: 'relative' }}>{rows}</div>
    </div>
  );
}
