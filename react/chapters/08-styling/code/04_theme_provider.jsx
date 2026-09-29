// Theme switching: context + data-theme on <html> + localStorage + the OS preference.
import '../../../../tools_dom.mjs';
import { act } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider, ThemeSwitcher, useTheme } from './theme.jsx';

// jsdom has no matchMedia: fake one whose answer we can flip, like the user changing the OS setting.
let osDark = false; const listeners = new Set();
window.matchMedia = (q) => ({ get matches() { return osDark; }, media: q,
  addEventListener: (_, cb) => listeners.add(cb), removeEventListener: (_, cb) => listeners.delete(cb) });
const setOsDark = (v) => act(() => { osDark = v; listeners.forEach(cb => cb()); });

function Status() { const { choice, resolved } = useTheme(); return <p>choice={choice} resolved={resolved}</p>; }
const html = document.documentElement;
const show = (label) => console.log(label.padEnd(26), screen.getByText(/choice=/).textContent,
  `| <html data-theme="${html.dataset.theme}"> | saved=${localStorage.getItem('theme')}`);

const app = render(<ThemeProvider><ThemeSwitcher /><Status /></ThemeProvider>);
show('first visit (OS light)');
setOsDark(true);                     show('OS switches to dark');
fireEvent.click(screen.getByRole('radio', { name: 'light' })); show('user picks "light"');
setOsDark(false); setOsDark(true);   show('OS changes again (ignored)');
app.unmount();
render(<ThemeProvider><Status /></ThemeProvider>); show('reload');

try { render(<Status />); } catch (e) { console.log('outside provider ->', e.message); }
