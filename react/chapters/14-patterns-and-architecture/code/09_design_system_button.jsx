// A design-system primitive: variants and sizes as props, native props passed through, ref as a prop.
import '../../../../tools_dom.mjs';
import { render, screen } from '@testing-library/react';
import { createRef } from 'react';
import { Button } from './Button.jsx';

const ref = createRef();
render(
  <>
    <Button>Add to cart</Button>
    <Button variant="secondary" size="sm" disabled>Save for later</Button>
    <Button variant="danger" aria-label="Delete Trail Mug" ref={ref}>×</Button>
    <Button type="submit" className="wide">Pay</Button>
  </>,
);
for (const b of screen.getAllByRole('button')) {
  console.log(`${b.outerHTML}`);
}
console.log('ref points at the DOM node:', ref.current instanceof HTMLElement, ref.current.tagName);
console.log('found by accessible name  :', screen.getByRole('button', { name: 'Delete Trail Mug' }) === ref.current);
