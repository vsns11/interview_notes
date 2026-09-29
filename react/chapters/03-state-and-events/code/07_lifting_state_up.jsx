// Two panels that should open one at a time: move the state to their common parent.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

// Before: each Panel owns isActive -> both can be open at once.
function PanelLocal({ title, children }) {
  const [isActive, setIsActive] = useState(false);
  return (
    <section>
      <h3>{title}</h3>
      {isActive ? <p>{children}</p> : <button onClick={() => setIsActive(true)}>Show {title}</button>}
    </section>
  );
}

// After: Panel is controlled by props; the parent owns activeIndex.
function Panel({ title, isActive, onShow, children }) {
  return (
    <section>
      <h3>{title}</h3>
      {isActive ? <p>{children}</p> : <button onClick={onShow}>Show {title}</button>}
    </section>
  );
}
function Accordion() {
  const [activeIndex, setActiveIndex] = useState(0);
  return (
    <>
      <Panel title="About" isActive={activeIndex === 0} onShow={() => setActiveIndex(0)}>Almaty is Kazakhstan's largest city.</Panel>
      <Panel title="Etymology" isActive={activeIndex === 1} onShow={() => setActiveIndex(1)}>The name comes from the word for apple.</Panel>
    </>
  );
}
const openPanels = () => screen.queryAllByRole('paragraph').length;

render(<><PanelLocal title="About">A</PanelLocal><PanelLocal title="Etymology">E</PanelLocal></>);
fireEvent.click(screen.getByText('Show About'));
fireEvent.click(screen.getByText('Show Etymology'));
console.log('local state : open panels after clicking both =', openPanels());
cleanup();

render(<Accordion />);
console.log('lifted state: initially open =', screen.getByRole('paragraph').textContent);
fireEvent.click(screen.getByText('Show Etymology'));
console.log('lifted state: after "Show Etymology" open panels =', openPanels(), '->', screen.getByRole('paragraph').textContent);
