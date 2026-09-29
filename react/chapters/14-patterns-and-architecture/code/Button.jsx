// tools_run: skip  (component module) — a design-system Button: variants + sizes map to classes,
// native props pass through, and `ref` is a normal prop in React 19 (no forwardRef needed).
const variants = { primary: 'btn-primary', secondary: 'btn-secondary', danger: 'btn-danger' };
const sizes = { sm: 'btn-sm', md: 'btn-md' };

export function Button({ variant = 'primary', size = 'md', className = '', type = 'button', ref, ...rest }) {
  return <button ref={ref} type={type} className={`btn ${variants[variant]} ${sizes[size]} ${className}`.trim()} {...rest} />;
}
