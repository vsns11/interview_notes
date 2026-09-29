// Composing class names from props: variants, sizes, states — with a clsx-style helper.
import '../../../../tools_dom.mjs';
import { render, screen } from '@testing-library/react';
import { cx } from './cx.js';

console.log(JSON.stringify(cx('btn', false && 'hidden', { active: true, disabled: false }, ['lg', null])));

// A Button whose look is driven by props. The class names map to rules in a CSS file or module.
function Button({ variant = 'primary', size = 'md', loading = false, className, ...rest }) {
  return (
    <button
      className={cx('btn', `btn--${variant}`, `btn--${size}`, { 'btn--loading': loading }, className)}
      disabled={loading || rest.disabled}
      {...rest}
    />
  );
}

render(
  <>
    <Button>Save</Button>
    <Button variant="danger" size="sm">Delete</Button>
    <Button loading>Uploading</Button>
    <Button variant="ghost" className="ml-auto">Cancel</Button>
  </>
);
for (const b of screen.getAllByRole('button')) {
  console.log(`${b.textContent.padEnd(9)} class="${b.className}"${b.disabled ? ' disabled' : ''}`);
}

// The common anti-pattern: string concatenation leaves "undefined"/"false" and double spaces.
const loading = false, extra = undefined;
console.log('concatenated:', JSON.stringify('btn ' + (loading && 'btn--loading') + ' ' + extra));
