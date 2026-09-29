// Machine coding 4 — modal dialog: portal, focus trap, Escape, backdrop click, focus restore, scroll lock.
import { useEffect, useId, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

const FOCUSABLE = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

export function Modal({ open, onClose, title, children }) {
  const dialogRef = useRef(null);
  const titleId = useId();

  useEffect(() => {
    if (!open) return;
    const previouslyFocused = document.activeElement;          // usually the button that opened us
    const first = dialogRef.current.querySelector(FOCUSABLE);
    (first ?? dialogRef.current).focus();                       // move focus INTO the dialog
    const { overflow } = document.body.style;
    document.body.style.overflow = 'hidden';                    // stop the page behind from scrolling
    return () => {
      document.body.style.overflow = overflow;
      previouslyFocused?.focus?.();                             // give focus back on close
    };
  }, [open]);

  if (!open) return null;

  function handleKeyDown(e) {
    if (e.key === 'Escape') {
      e.stopPropagation();                                      // a parent modal shouldn't close too
      onClose();
      return;
    }
    if (e.key !== 'Tab') return;
    const nodes = [...dialogRef.current.querySelectorAll(FOCUSABLE)];
    if (nodes.length === 0) { e.preventDefault(); return; }
    const first = nodes[0];
    const last = nodes[nodes.length - 1];
    const outside = !nodes.includes(document.activeElement);    // e.g. the dialog box itself, after a click on its text
    if (e.shiftKey && (outside || document.activeElement === first)) { e.preventDefault(); last.focus(); }
    else if (!e.shiftKey && (outside || document.activeElement === last)) { e.preventDefault(); first.focus(); }
  }

  return createPortal(
    <div className="backdrop" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        className="dialog"
        onKeyDown={handleKeyDown}
      >
        <h2 id={titleId}>{title}</h2>
        {children}
      </div>
    </div>,
    document.body,                                              // escape parents' overflow/z-index
  );
}

// A small page that uses the modal the way an app would.
export default function DeleteProjectDemo({ defaultOpen = false }) {
  const [open, setOpen] = useState(defaultOpen);
  const [log, setLog] = useState('Project “Apollo” is active.');
  const [confirm, setConfirm] = useState('');
  const close = () => { setOpen(false); setConfirm(''); };
  return (
    <section className="card">
      <h2>Project settings</h2>
      <p>{log}</p>
      <button onClick={() => setOpen(true)}>Delete project…</button>
      <Modal open={open} onClose={close} title="Delete “Apollo”?">
        <p>This removes the project and its 42 tasks. You can’t undo this.</p>
        <label>Type <b>Apollo</b> to confirm <input value={confirm} onChange={(e) => setConfirm(e.target.value)} /></label>
        <div className="row end">
          <button onClick={close}>Cancel</button>
          <button className="danger" disabled={confirm !== 'Apollo'} onClick={() => { setLog('Project deleted.'); close(); }}>Delete</button>
        </div>
      </Modal>
    </section>
  );
}
