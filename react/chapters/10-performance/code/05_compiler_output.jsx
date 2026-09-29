// What React Compiler does, shown by hand.
// ProductPage below is plain code with NO useMemo/useCallback/memo.
// ProductPageCompiled is a hand-simplified version of the kind of code the compiler emits for it:
// a per-component cache array ($) from react/compiler-runtime, and "if inputs changed, recompute; else reuse".
// (Real compiler output differs in details and variable names; the idea is the same.)
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { c as _c } from 'react/compiler-runtime';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

let listRenders = 0, filterRuns = 0;
function ProductList({ items, onBuy }) {                     // NOT wrapped in memo()
  listRenders++;
  return <ul>{items.map(p => <li key={p.id} onClick={() => onBuy(p.id)}>{p.name}</li>)}</ul>;
}
const PRODUCTS = [{ id: 1, name: 'Desk', inStock: true }, { id: 2, name: 'Lamp', inStock: false }];

// ---- what you write ----
function ProductPage({ products, theme }) {
  filterRuns++;
  const visible = products.filter(p => p.inStock);
  const onBuy = id => console.log('buy', id);
  return <div className={theme}><ProductList items={visible} onBuy={onBuy} /></div>;
}

// ---- roughly what the compiler turns it into ----
function ProductPageCompiled({ products, theme }) {
  const $ = _c(6);                                            // 6 cache slots for this component instance
  let visible;
  if ($[0] !== products) {                                    // input changed -> recompute
    filterRuns++;
    visible = products.filter(p => p.inStock);
    $[0] = products; $[1] = visible;
  } else visible = $[1];                                      // unchanged -> reuse the same array
  let onBuy;
  if ($[2] === Symbol.for('react.memo_cache_sentinel')) {     // first render only: the function uses no props
    onBuy = id => console.log('buy', id);
    $[2] = onBuy;
  } else onBuy = $[2];
  let list;
  if ($[3] !== visible || $[4] !== onBuy) {                   // the JSX element itself is cached
    list = <ProductList items={visible} onBuy={onBuy} />;
    $[3] = visible; $[4] = onBuy; $[5] = list;
  } else list = $[5];
  // (the compiler would cache the outer <div> on `theme` and `list` too; left out for brevity)
  return <div className={theme}>{list}</div>;
}

function Shell({ Page }) {
  const [theme, setTheme] = useState('light');
  return <><button onClick={() => setTheme(t => (t === 'light' ? 'dark' : 'light'))}>theme</button>
    <Page products={PRODUCTS} theme={theme} /></>;
}

for (const [label, Page] of [['hand-written code  ', ProductPage], ['compiler-style code', ProductPageCompiled]]) {
  listRenders = 0; filterRuns = 0;
  render(<Shell Page={Page} />);
  for (let i = 0; i < 3; i++) fireEvent.click(screen.getByText('theme'));
  console.log(`${label}: 4 renders of the page -> filter ran ${filterRuns}x, ProductList rendered ${listRenders}x`);
  cleanup();
}
