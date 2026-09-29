// Typing props and children.
import '../../../../tools_dom.mjs';
import type { ReactNode, ComponentProps, PropsWithChildren, CSSProperties } from 'react';
import { render } from '@testing-library/react';

// 1. A props type: required, optional (?), union of literals, and a default value.
type BadgeProps = {
  label: string;                        // required
  count?: number;                       // optional -> number | undefined inside the component
  tone?: 'info' | 'warning' | 'error';  // only these three strings are allowed
};
function Badge({ label, count = 0, tone = 'info' }: BadgeProps) {
  return <span className={`badge ${tone}`}>{label}: {count}</span>;
}

// 2. children: ReactNode accepts anything React can render (JSX, strings, numbers, null, arrays...).
type CardProps = { title: string; children: ReactNode; footer?: ReactNode };
function Card({ title, children, footer }: CardProps) {
  return <section><h2>{title}</h2>{children}{footer && <footer>{footer}</footer>}</section>;
}
// PropsWithChildren<P> adds an OPTIONAL children?: ReactNode to P.
function Note({ children }: PropsWithChildren) { return <aside>{children}</aside>; }

// 3. Extending a native element's props: accept everything <button> accepts, plus our own prop.
type ButtonProps = ComponentProps<'button'> & { variant?: 'primary' | 'ghost' };
function Button({ variant = 'primary', className, ...rest }: ButtonProps) {
  return <button className={[variant, className].filter(Boolean).join(' ')} {...rest} />;
}

// 4. Other handy prop types.
type AvatarProps = {
  user: { name: string; imageUrl: string };  // an object shape
  size?: number;
  style?: CSSProperties;                      // a style object, checked key by key
  onSelect?: (name: string) => void;          // a callback prop
};
function Avatar({ user, size = 32, style, onSelect }: AvatarProps) {
  return <img src={user.imageUrl} alt={user.name} width={size} style={style} onClick={() => onSelect?.(user.name)} />;
}

const { container } = render(
  <Card title="Inbox" footer={<small>updated now</small>}>
    <Badge label="unread" count={3} tone="warning" />
    <Note>plain text child</Note>
    <Button type="submit" disabled aria-label="send">Send</Button>
    <Avatar user={{ name: 'Ada', imageUrl: '/ada.png' }} style={{ borderRadius: '50%' }} />
  </Card>
);
console.log(container.innerHTML.replaceAll('><', '>\n<'));
