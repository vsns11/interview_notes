// Discriminated unions for props: make impossible prop combinations impossible to write.
import '../../../../tools_dom.mjs';
import type { ComponentProps, ReactNode } from 'react';
import { render } from '@testing-library/react';

// 1. A status message: each `status` value comes with exactly the props it needs.
type StatusProps =
  | { status: 'loading' }
  | { status: 'error'; error: string; onRetry: () => void }
  | { status: 'success'; count: number };

function Status(props: StatusProps) {
  switch (props.status) {                    // narrow on the discriminant `status`
    case 'loading': return <p>Loading…</p>;
    case 'error':   return <p role="alert">{props.error} <button onClick={props.onRetry}>Retry</button></p>;
    case 'success': return <p>{props.count} results</p>;
  }
}

// 2. Either a link or a button - never an <a> with onClick-only, never a <button> with href.
type LinkLike = { as: 'a'; href: string } & Omit<ComponentProps<'a'>, 'href'>;
type ButtonLike = { as?: 'button' } & ComponentProps<'button'>;
type ActionProps = (LinkLike | ButtonLike) & { children: ReactNode };

function Action(props: ActionProps) {
  if (props.as === 'a') {
    const { as: _as, ...rest } = props;       // here props is LinkLike
    return <a className="action" {...rest} />;
  }
  const { as: _as, ...rest } = props;         // here props is ButtonLike
  return <button className="action" type="button" {...rest} />;
}

// 3. "One or the other, not both": the `never` trick forbids the other prop.
type ImageSource =
  | { src: string; icon?: never }
  | { icon: 'user' | 'star'; src?: never };
function Thumb(props: ImageSource & { alt: string }) {
  return props.src ? <img src={props.src} alt={props.alt} /> : <span aria-label={props.alt}>[{props.icon}]</span>;
}

const { container } = render(
  <>
    <Status status="loading" />
    <Status status="error" error="Network down" onRetry={() => {}} />
    <Status status="success" count={42} />
    <Action as="a" href="/docs">Docs</Action>
    <Action onClick={() => {}}>Save</Action>
    <Thumb src="/a.png" alt="avatar" />
    <Thumb icon="star" alt="favourite" />
  </>
);
console.log(container.innerHTML.replaceAll('><', '>\n<'));
