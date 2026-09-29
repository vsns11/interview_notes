// tools_run: skip  (browser entry for theme.html)
import { createRoot } from 'react-dom/client';
import { ThemeProvider, ThemeSwitcher, useTheme } from './theme.jsx';
import './theme.css';

function Panel() {
  const { resolved } = useTheme();
  return (
    <div className="panel">
      <strong>Weekly report</strong>
      <p>Showing the {resolved} theme. Colours come from CSS variables.</p>
      <button className="cta">Download PDF</button>
    </div>
  );
}
createRoot(document.getElementById('root')).render(
  <ThemeProvider><div className="app"><ThemeSwitcher /><Panel /></div></ThemeProvider>
);
