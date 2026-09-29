// A hydration mismatch, caught with onRecoverableError, and two correct fixes.
import '../../../../tools_dom.mjs';
import { Suspense, use, useEffect, useState, useSyncExternalStore } from 'react';
import { browser } from 'react-dom';                 // React 19.3
import { renderToString } from 'react-dom/server';
import { hydrateRoot } from 'react-dom/client';
import { act } from 'react';

// Pretend "server" and "browser" differ: on the server there is no window.
let onServer = true;
const isBrowser = () => !onServer;

function BadBanner() {
  // BUG: render output depends on the environment.
  return <p>{isBrowser() ? 'Welcome back!' : 'Hello, guest'}</p>;
}

function GoodBanner() {
  // FIX 1: render the server version first, switch after hydration in an Effect.
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  return <p>{mounted && isBrowser() ? 'Welcome back!' : 'Hello, guest'}</p>;
}

const subscribe = () => () => {};
function GoodBanner2() {
  // FIX 2: useSyncExternalStore with a separate server snapshot (used during hydration too).
  const inBrowser = useSyncExternalStore(subscribe, () => isBrowser(), () => false);
  return <p>{inBrowser ? 'Welcome back!' : 'Hello, guest'}</p>;
}

async function tryHydrate(Component) {
  onServer = true;
  const html = renderToString(<Component />);
  const root = document.createElement('div'); root.innerHTML = html; document.body.append(root);
  onServer = false;
  const errors = [];
  await act(async () => {
    hydrateRoot(root, <Component />, { onRecoverableError: (e) => errors.push(e.message.split('\n')[0]) });
  });
  console.log(`${Component.name.padEnd(12)} server: ${html.padEnd(28)} after hydrate: ${root.innerHTML}`);
  for (const e of errors) console.log(`   recoverable error: ${e}`);
}

await tryHydrate(BadBanner);
await tryHydrate(GoodBanner);
await tryHydrate(GoodBanner2);

// FIX 3 (React 19.3): use(browser()) suspends on the server only, so the server sends the
// Suspense fallback and the browser renders the real content - no mismatch, no error.
function Greeting() { use(browser()); return <p>Welcome back!</p>; }
function GoodBanner3() { return <Suspense fallback={<p>Hello, guest</p>}><Greeting /></Suspense>; }
{
  const html = renderToString(<GoodBanner3 />);
  const root = document.createElement('div'); root.innerHTML = html; document.body.append(root);
  const errors = [];
  await act(async () => { hydrateRoot(root, <GoodBanner3 />, { onRecoverableError: e => errors.push(e) }); });
  const shown = html.replace(/<template[^]*?<\/template>/, '<template…>');   // hide the dev-only message
  console.log(`GoodBanner3  server: ${shown}\n             after hydrate: ${root.innerHTML}  errors: ${errors.length}`);
}

// suppressHydrationWarning: allowed for ONE element's text that must differ (e.g. a timestamp).
function Stamp({ now }) { return <time suppressHydrationWarning>{now}</time>; }
onServer = true;
const html = renderToString(<Stamp now="10:00:00" />);
const box = document.createElement('div'); box.innerHTML = html; document.body.append(box);
const errs = [];
await act(async () => { hydrateRoot(box, <Stamp now="10:00:02" />, { onRecoverableError: e => errs.push(e.message) }); });
console.log(`Stamp        server: ${html.padEnd(28)} after hydrate: ${box.innerHTML}  errors: ${errs.length}`);
