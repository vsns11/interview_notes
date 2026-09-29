// Machine coding 5 — accessible tabs (WAI-ARIA tabs pattern, automatic activation, roving tabindex).
import { useId, useRef, useState } from 'react';

export default function Tabs({ tabs, defaultIndex = 0, onChange, label }) {
  const [selected, setSelected] = useState(defaultIndex);
  const tabRefs = useRef([]);                               // one DOM node per tab, for focus()
  const id = useId();

  function select(i) {
    setSelected(i);
    tabRefs.current[i].focus();
    onChange?.(i);
  }

  // Find the next enabled tab in `step` direction, wrapping around the ends.
  function nextEnabled(from, step) {
    for (let k = 1; k <= tabs.length; k++) {
      const i = (from + step * k + tabs.length) % tabs.length;
      if (!tabs[i].disabled) return i;
    }
    return from;
  }

  function handleKeyDown(e) {
    const moves = {
      ArrowRight: () => nextEnabled(selected, 1),
      ArrowLeft: () => nextEnabled(selected, -1),
      Home: () => nextEnabled(-1, 1),                      // first enabled
      End: () => nextEnabled(tabs.length, -1),             // last enabled
    };
    if (!moves[e.key]) return;
    e.preventDefault();                                     // Home/End would scroll the page
    select(moves[e.key]());
  }

  return (
    <div className="card tabs">
      <div role="tablist" aria-label={label} onKeyDown={handleKeyDown}>
        {tabs.map((tab, i) => (
          <button
            key={tab.label}
            ref={(el) => { tabRefs.current[i] = el; }}
            role="tab"
            id={`${id}-tab-${i}`}
            aria-selected={i === selected}
            aria-controls={`${id}-panel-${i}`}
            tabIndex={i === selected ? 0 : -1}                // roving tabindex: only one tab stop
            disabled={tab.disabled}
            onClick={() => select(i)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      {tabs.map((tab, i) => (
        <div
          key={tab.label}
          role="tabpanel"
          id={`${id}-panel-${i}`}
          aria-labelledby={`${id}-tab-${i}`}
          tabIndex={0}
          hidden={i !== selected}                           // keep panels mounted: their state survives
        >
          {tab.content}
        </div>
      ))}
    </div>
  );
}
