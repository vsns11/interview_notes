// useLayoutEffect runs after React changes the DOM but BEFORE the browser paints; useEffect runs after the paint.
import '../../../../tools_dom.mjs';
import { useState, useEffect, useLayoutEffect, useInsertionEffect, useRef } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { createRoot } from 'react-dom/client';
import { flushSync } from 'react-dom';

// 1. Order of work for one render.
function Order() {
  const [n, setN] = useState(0);
  console.log(`  render (n=${n})`);
  useInsertionEffect(() => { console.log('  useInsertionEffect  (for CSS-in-JS libraries: inject <style> first)'); });
  useLayoutEffect(() => { console.log('  useLayoutEffect     (DOM updated, not painted yet: measure here)'); return () => console.log('  layout cleanup'); });
  useEffect(() => { console.log('  useEffect           (after paint: subscriptions, fetching, logging)'); return () => console.log('  effect cleanup'); });
  return <button onClick={() => setN(n + 1)}>n={n}</button>;
}

// 2. Measuring: place a tooltip above its target, or below if there is no room above.
// jsdom has no layout engine, so heights/positions are faked with data-* attributes here.
window.HTMLElement.prototype.getBoundingClientRect = function () {
  const h = Number(this.dataset.h ?? 0), top = Number(this.dataset.top ?? 0);
  return { top, bottom: top + h, height: h, left: 0, right: 100, width: 100, x: 0, y: top };
};
function Tooltip({ targetTop, useLayout }) {
  const ref = useRef(null);
  const [height, setHeight] = useState(0);                      // unknown until measured
  const measure = () => setHeight(ref.current.getBoundingClientRect().height);
  (useLayout ? useLayoutEffect : useEffect)(measure, []);
  const placeAbove = targetTop - height >= 0;                    // enough room above the target?
  const top = placeAbove ? targetTop - height : targetTop + 30;
  return <div ref={ref} data-h="80" role="tooltip" style={{ position: 'absolute', top }}>{placeAbove ? 'above' : 'below'} the button</div>;
}

console.log('mount:');
render(<Order />);
console.log('click:');
fireEvent.click(screen.getByRole('button'));
cleanup();

// Drive React like a browser does: render in one task; passive Effects (useEffect) run in a LATER task,
// and the browser is free to paint in between. Layout effects finish inside the same task.
globalThis.IS_REACT_ACT_ENVIRONMENT = false;
for (const useLayout of [false, true]) {
  const box = document.createElement('div'); document.body.append(box);
  const root = createRoot(box);
  flushSync(() => root.render(<Tooltip targetTop={50} useLayout={useLayout} />));
  const tip = () => { const t = box.querySelector('[role=tooltip]'); return `"${t.textContent}" top=${t.style.top}`; };
  const firstPaint = tip();                                     // what the browser would paint after this task
  await new Promise((r) => setTimeout(r, 20));
  console.log(`${useLayout ? 'useLayoutEffect' : 'useEffect      '}: first paint ${firstPaint} | settled ${tip()}`);
  root.unmount(); box.remove();
}
