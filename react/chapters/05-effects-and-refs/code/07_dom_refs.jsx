// Refs to DOM nodes: focus, scroll, measure. Ref callbacks, lists of refs, and passing refs to your own components.
import '../../../../tools_dom.mjs';
import { useRef, useState, useImperativeHandle, forwardRef, Fragment } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

window.HTMLElement.prototype.scrollIntoView = function () { console.log(`  scrollIntoView -> ${this.alt}`); }; // jsdom has no layout

// 1. The basic pattern: useRef(null) + ref={...}; React fills .current after the DOM node exists.
function FocusForm() {
  const inputRef = useRef(null);
  console.log('  during render, inputRef.current =', inputRef.current?.tagName ?? null);
  return (
    <>
      <input ref={inputRef} aria-label="name" />
      <button onClick={() => inputRef.current.focus()}>Focus the input</button>
    </>
  );
}

let galleryMap;
// 2. A list of refs: one ref holding a Map, filled by ref CALLBACKS. React 19: a callback may return a cleanup.
function CatGallery({ cats }) {
  const itemsRef = useRef(new Map());
  galleryMap = itemsRef.current;                    // (exposed only so this demo can print it)
  return (
    <>
      <nav>{cats.map((c) => <button key={c} onClick={() => itemsRef.current.get(c).scrollIntoView({ behavior: 'smooth' })}>{c}</button>)}</nav>
      <ul>
        {cats.map((c) => (
          <li key={c}>
            <img alt={c} ref={(node) => {
              itemsRef.current.set(c, node);            // called with the node when attached
              return () => itemsRef.current.delete(c);  // cleanup when detached (React 19+)
            }} />
          </li>
        ))}
      </ul>
    </>
  );
}

// 3. React 19: `ref` is an ordinary prop for function components — no forwardRef needed.
function TextField({ label, ref }) {
  return <label>{label} <input ref={ref} /></label>;
}
// Legacy (React 18 and earlier) — still works in 19, but forwardRef is planned for deprecation.
const OldTextField = forwardRef(function OldTextField({ label }, ref) {
  return <label>{label} <input ref={ref} /></label>;
});

// 4. useImperativeHandle: expose a small, deliberate API instead of the raw DOM node.
function VideoLikeInput({ ref }) {
  const real = useRef(null);
  useImperativeHandle(ref, () => ({
    focus: () => real.current.focus(),
    clear: () => { real.current.value = ''; },
  }), []);
  return <input ref={real} aria-label="limited" defaultValue="draft" />;
}

// 5. React 19.3: a ref on a <Fragment> gives a FragmentInstance that acts on its children without a wrapper <div>.
function Toolbar() {
  const group = useRef(null);
  return (
    <>
      <Fragment ref={group}>
        <button>Bold</button><button>Italic</button>
      </Fragment>
      <button onClick={() => group.current.focus()}>focus toolbar</button>
    </>
  );
}

console.log('1 focus:');
render(<FocusForm />);
fireEvent.click(screen.getByText('Focus the input'));
console.log('  focused element:', document.activeElement.getAttribute('aria-label'));
cleanup();

console.log('2 list of refs:');
const g = render(<CatGallery cats={['Tom', 'Maru', 'Jellylorum']} />);
fireEvent.click(screen.getByRole('button', { name: 'Jellylorum' }));
g.rerender(<CatGallery cats={['Tom', 'Maru']} />);
console.log('  after removing Jellylorum, the Map holds:', [...galleryMap.keys()].join(', '));
cleanup();

console.log('3 ref as a prop:');
function Page() {
  const a = useRef(null), b = useRef(null);
  return <>
    <TextField label="New" ref={a} /><OldTextField label="Old" ref={b} />
    <button onClick={() => { a.current.focus(); console.log('  new-style ref ->', a.current.tagName, '| old-style ref ->', b.current.tagName); }}>go</button>
  </>;
}
render(<Page />);
fireEvent.click(screen.getByText('go'));
console.log('  focused:', document.activeElement.closest('label').textContent.trim());
cleanup();

console.log('4 useImperativeHandle:');
function Parent() {
  const handle = useRef(null);
  return <><VideoLikeInput ref={handle} /><button onClick={() => { handle.current.clear(); handle.current.focus(); console.log('  exposed keys:', Object.keys(handle.current)); }}>reset</button></>;
}
render(<Parent />);
fireEvent.click(screen.getByText('reset'));
console.log('  value now', JSON.stringify(screen.getByLabelText('limited').value), '| focused:', document.activeElement.getAttribute('aria-label'));
cleanup();

console.log('5 Fragment ref:');
render(<Toolbar />);
fireEvent.click(screen.getByText('focus toolbar'));
console.log('  focused:', document.activeElement.textContent);
