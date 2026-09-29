// Same "like button" twice: once by hand with DOM calls (imperative), once with React (declarative).
import '../../../../tools_dom.mjs';
import { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { act } from 'react';

// ---- Imperative: YOU write every step that changes the page ----
const box = document.createElement('div');
document.body.append(box);
let likes = 0;
const btn = document.createElement('button');
const label = document.createElement('span');
label.textContent = 'No likes yet';
btn.textContent = 'Like';
btn.addEventListener('click', () => {
  likes++;                                            // 1. change the data
  label.textContent = `${likes} like(s)`;             // 2. remember to update the label
  btn.textContent = likes >= 3 ? 'Popular!' : 'Like'; // 3. ...and the button
});
box.append(btn, ' ', label);
for (let i = 0; i < 3; i++) btn.click();
console.log('imperative:', box.innerHTML);

// ---- Declarative: you describe WHAT the UI looks like for a given state ----
function LikeButton() {
  const [likes, setLikes] = useState(0);
  return (
    <div>
      <button onClick={() => setLikes(likes + 1)}>{likes >= 3 ? 'Popular!' : 'Like'}</button>{' '}
      <span>{likes === 0 ? 'No likes yet' : `${likes} like(s)`}</span>
    </div>
  );
}
const host = document.createElement('div');
document.body.append(host);
const root = createRoot(host);
await act(() => root.render(<LikeButton />));
for (let i = 0; i < 3; i++) await act(() => host.querySelector('button').click());
console.log('declarative:', host.innerHTML);
