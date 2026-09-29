// Components must be pure while rendering. StrictMode (dev only) calls them twice to expose impurity.
import '../../../../tools_dom.mjs';
import { StrictMode } from 'react';
import { render } from '@testing-library/react';

let guest = 0;
function ImpureCup() {
  guest = guest + 1;              // BAD: changes a variable outside the component during render
  return <h2>Tea cup for guest #{guest}</h2>;
}

function PureCup({ guest }) {     // GOOD: output depends only on props
  return <h2>Tea cup for guest #{guest}</h2>;
}

const noStrict = render(<><ImpureCup /><ImpureCup /><ImpureCup /></>).container;
console.log('impure, no StrictMode:', noStrict.textContent);

guest = 0;
const strict = render(<StrictMode><ImpureCup /><ImpureCup /><ImpureCup /></StrictMode>).container;
console.log('impure, StrictMode   :', strict.textContent);

const pure = render(<StrictMode><PureCup guest={1} /><PureCup guest={2} /><PureCup guest={3} /></StrictMode>).container;
console.log('pure,   StrictMode   :', pure.textContent);

// Local mutation is fine: the array is created during this render.
function TeaGathering() {
  const cups = [];
  for (let i = 1; i <= 3; i++) cups.push(<PureCup key={i} guest={i} />);
  return <>{cups}</>;
}
console.log('local mutation OK    :', render(<StrictMode><TeaGathering /></StrictMode>).container.textContent);
