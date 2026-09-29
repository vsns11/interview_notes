// tools_run: skip  (library module) — light / dark / system theming with CSS variables.
import { createContext, useContext, useEffect, useState, useSyncExternalStore } from 'react';

const ThemeContext = createContext(null);
const QUERY = '(prefers-color-scheme: dark)';

// The OS setting is an external store -> useSyncExternalStore (Chapter 7).
function subscribeSystem(cb) { const m = window.matchMedia(QUERY); m.addEventListener('change', cb); return () => m.removeEventListener('change', cb); }
function useSystemDark() { return useSyncExternalStore(subscribeSystem, () => window.matchMedia(QUERY).matches, () => false); }

export function ThemeProvider({ children }) {
  const [choice, setChoice] = useState(() => localStorage.getItem('theme') ?? 'system'); // 'light' | 'dark' | 'system'
  const systemDark = useSystemDark();
  const resolved = choice === 'system' ? (systemDark ? 'dark' : 'light') : choice;

  useEffect(() => {                        // sync to the <html> element: CSS does the rest
    document.documentElement.dataset.theme = resolved;
    document.documentElement.style.colorScheme = resolved;   // native controls + scrollbars follow
    localStorage.setItem('theme', choice);
  }, [choice, resolved]);

  return <ThemeContext.Provider value={{ choice, resolved, setChoice }}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used inside <ThemeProvider>');
  return ctx;
}

export function ThemeSwitcher() {
  const { choice, setChoice } = useTheme();
  return (
    <div role="radiogroup" aria-label="Theme" className="switcher">
      {['light', 'dark', 'system'].map(t => (
        <button key={t} role="radio" aria-checked={choice === t} onClick={() => setChoice(t)}>{t}</button>
      ))}
    </div>
  );
}
