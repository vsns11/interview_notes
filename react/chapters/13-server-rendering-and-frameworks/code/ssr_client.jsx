// tools_run: skip  (browser entry — bundled by 04_ssr_server_hydrate.jsx and served as /client.js)
import { hydrateRoot } from 'react-dom/client';
import App from './shop_app.jsx';

// The browser needs the same data the server used. Here it fetches it again (see §13.4 for why
// frameworks serialize server data into the page instead of fetching twice).
const reviewsPromise = fetch('/api/reviews').then(r => r.json());

hydrateRoot(document, <App reviewsPromise={reviewsPromise} />, {
  onRecoverableError(err) { console.error('recoverable:', err.message); },
});
