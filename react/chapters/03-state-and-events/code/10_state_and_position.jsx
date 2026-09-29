// State belongs to a position in the tree. Same component at same position keeps state; a key resets it.
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

function Counter({ person }) {
  const [score, setScore] = useState(0);
  return <p>{person}'s score: {score} <button onClick={() => setScore(score + 1)}>+1 {person}</button></p>;
}

function Scoreboard({ mode }) {
  const [isTaylor, setIsTaylor] = useState(true);
  let counter;
  if (mode === 'same-position') counter = isTaylor ? <Counter person="Taylor" /> : <Counter person="Sarah" />;
  if (mode === 'with-key') counter = isTaylor ? <Counter key="Taylor" person="Taylor" /> : <Counter key="Sarah" person="Sarah" />;
  return <div>{counter}<button onClick={() => setIsTaylor(!isTaylor)}>Next player</button></div>;
}

for (const mode of ['same-position', 'with-key']) {
  const { container, unmount } = render(<Scoreboard mode={mode} />);
  fireEvent.click(screen.getByText('+1 Taylor'));
  fireEvent.click(screen.getByText('+1 Taylor'));
  fireEvent.click(screen.getByText('Next player'));
  const shown = () => container.querySelector('p').textContent.replace(/\+1.*/, '').trim();
  const afterSwitch = shown();
  fireEvent.click(screen.getByText('Next player'));
  console.log(`${mode.padEnd(13)}: Taylor 2 -> switch -> ${afterSwitch.padEnd(17)} -> switch back -> ${shown()}`);
  unmount();
}
