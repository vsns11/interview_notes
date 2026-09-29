// children = the JSX between a component's tags. Composition = building UIs by nesting components.
import '../../../../tools_dom.mjs';
import { render } from '@testing-library/react';

function Card({ title, children }) {
  return (
    <div className="card">
      {title && <h3>{title}</h3>}
      <div className="card-body">{children}</div>
    </div>
  );
}

// "Slots": pass JSX in named props when a layout has several holes.
function PageLayout({ header, sidebar, children }) {
  return (
    <div className="page">
      <header>{header}</header>
      <aside>{sidebar}</aside>
      <main>{children}</main>
    </div>
  );
}

const { container } = render(
  <PageLayout header={<h1>Dashboard</h1>} sidebar={<nav>Menu</nav>}>
    <Card title="Revenue">
      <p>$12,400 this week</p>
    </Card>
    <Card>
      <p>No title here</p>
    </Card>
  </PageLayout>,
);
console.log(container.innerHTML.replaceAll('><', '>\n<'));

// children can be anything renderable: text, elements, arrays, or nothing.
function Show({ children }) { return <span>[{children}]</span>; }
console.log('\ntext   :', render(<Show>hi</Show>).container.innerHTML);
console.log('array  :', render(<Show>{['a', 'b']}</Show>).container.innerHTML);
console.log('none   :', render(<Show />).container.innerHTML);
