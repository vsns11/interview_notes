// Context: a value provided high in the tree, read by any descendant — no prop drilling.
import '../../../../tools_dom.mjs';
import { createContext, useContext, use, useState } from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';

// 1. Create: the argument is the DEFAULT, used only when there is no provider above.
const ThemeContext = createContext('light');
const LevelContext = createContext(1);

// 2. Read: useContext(Ctx) — or use(Ctx), which (unlike Hooks) may be called conditionally.
function Button({ children }) {
  const theme = useContext(ThemeContext);
  return <button className={`btn-${theme}`}>{children}</button>;
}
function Heading({ children }) {
  if (!children) return null;                   // early return...
  const level = use(LevelContext);              // ...then read context: allowed with use(), not with useContext
  const Tag = `h${Math.min(level, 6)}`;
  return <Tag>{children}</Tag>;
}
// 3. A component can read the nearest value and provide a new one for its subtree.
function Section({ children }) {
  const level = useContext(LevelContext);
  return <section><LevelContext value={level + 1}>{children}</LevelContext></section>;   // React 19: <Ctx value>
}

function App() {
  const [theme, setTheme] = useState('light');
  return (
    <ThemeContext value={theme}>
      <Heading>Title</Heading>
      <Section>
        <Heading>Chapter</Heading>
        <Section>
          <Heading>Sub-section</Heading>
          <Heading />
          <Button>Save</Button>
          <ThemeContext.Provider value="high-contrast">   {/* older syntax, still works */}
            <Button>Nested override</Button>
          </ThemeContext.Provider>
        </Section>
      </Section>
      <button onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}>toggle theme</button>
    </ThemeContext>
  );
}

render(<App />);
console.log('headings :', screen.getAllByRole('heading').map((h) => `${h.tagName}:${h.textContent}`).join('  '));
const classes = () => ['Save', 'Nested override'].map((n) => `${n} -> ${screen.getByText(n).className}`).join(' | ');
console.log('buttons  :', classes());
fireEvent.click(screen.getByText('toggle theme'));
console.log('toggled  :', classes());
cleanup();
render(<Button>No provider</Button>);
console.log('no provider ->', screen.getByRole('button').className, '(the default from createContext)');
