// tools_run: skip  (components for gallery.html) — the same profile card styled five ways.
import { useState } from 'react';
import './global.css';
import styles from './Card.module.css';
import { cx } from './cx.js';

// 1. Global CSS file + className
export function GlobalCssCard({ name, role }) {
  const [following, setFollowing] = useState(false);
  return (
    <div className="profile-card">
      <h3 className="profile-card__name">{name}</h3>
      <p className="profile-card__role">{role}</p>
      <button className={cx('profile-card__button', following && 'profile-card__button--following')}
              onClick={() => setFollowing(f => !f)}>{following ? 'Following' : 'Follow'}</button>
    </div>
  );
}

// 2. CSS Module
export function ModuleCard({ name, role }) {
  return (
    <div className={styles.card}>
      <h3 className={styles.title}>{name}</h3>
      <p className={styles.role}>{role}</p>
      <button className={styles.primary}>Follow</button>
    </div>
  );
}

// 3. Inline styles (no :hover, no media queries)
const s = {
  card: { border: '1px solid #d0d7de', borderRadius: 12, padding: 16, background: '#fff' },
  name: { margin: '0 0 4px', fontSize: 18 },
  role: { margin: '0 0 12px', color: '#57606a' },
  button: { font: 'inherit', padding: '6px 14px', borderRadius: 8, border: 0, background: '#0969da', color: '#fff' },
};
export function InlineCard({ name, role }) {
  return (
    <div style={s.card}>
      <h3 style={s.name}>{name}</h3>
      <p style={s.role}>{role}</p>
      <button style={s.button}>Follow</button>
    </div>
  );
}

// 4. Tailwind utility classes (compiled by the Tailwind CLI into tailwind.css)
export function TailwindCard({ name, role }) {
  return (
    <div className="rounded-xl border border-gray-300 bg-white p-4">
      <h3 className="mb-1 text-lg font-bold">{name}</h3>
      <p className="mb-3 text-gray-500">{role}</p>
      <button className="rounded-lg bg-blue-600 px-3.5 py-1.5 font-semibold text-white hover:bg-blue-700">Follow</button>
    </div>
  );
}

// 5. The CSS-in-JS idea: styles written in JS, injected as a <style> tag at runtime.
//    React 19 hoists <style href=... precedence=...> into <head> and de-duplicates it by href.
function css(strings) { const text = strings.join(''); const id = 'c' + [...text].reduce((h, c) => (h * 31 + c.charCodeAt(0)) | 0, 7).toString(36).replace('-', 'n'); return { id, text: text.replaceAll('&', '.' + id) }; }
const cardStyle = css`& { border: 1px dashed #bf8700; border-radius: 12px; padding: 16px; background: #fff8c5; }
  & h3 { margin: 0 0 4px; font-size: 18px; } & p { margin: 0 0 12px; color: #57606a; }
  & button { font: inherit; padding: 6px 14px; border-radius: 8px; border: 0; background: #bf8700; color: #fff; }`;
export function CssInJsCard({ name, role }) {
  return (
    <div className={cardStyle.id}>
      <style href={cardStyle.id} precedence="medium">{cardStyle.text}</style>
      <h3>{name}</h3><p>{role}</p><button>Follow</button>
    </div>
  );
}
