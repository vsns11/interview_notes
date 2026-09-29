// Components are capitalised functions returning JSX; parents nest children.
import '../../../../tools_dom.mjs';
import { render } from '@testing-library/react';

function Profile() {
  return <img src="https://i.imgur.com/MK3eW3As.jpg" alt="Katherine Johnson" />;
}

function Gallery() {
  return (
    <section>
      <h1>Amazing scientists</h1>
      <Profile />
      <Profile />
    </section>
  );
}

// Lowercase name: JSX treats <profile /> as an unknown HTML tag, NOT as the function below.
function profile() {
  return <img alt="never shown" />;
}

const errors = [];
const origError = console.error;
console.error = (...args) => errors.push(String(args[0]).split('\n')[0]);

const { container } = render(<Gallery />);
console.log('Gallery      :', container.innerHTML);

const bad = render(<div>{/* eslint-disable-next-line */}<profile /></div>).container;
console.log('<profile />  :', bad.innerHTML);
console.log('React warned :', errors[0]?.replace('%s', 'profile'));
console.error = origError;
void profile;
