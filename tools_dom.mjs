// Import FIRST in React/DOM examples run under Node:  import '../../../../tools_dom.mjs';
// Gives a browser-like global DOM (jsdom) so react-dom/client and @testing-library/react work in plain Node.
import { JSDOM } from 'jsdom';
const dom = new JSDOM('<!doctype html><html><body><div id="root"></div></body></html>', { url: 'http://localhost/', pretendToBeVisual: true });
const g = globalThis;
for (const k of ['window', 'document', 'HTMLElement', 'Node', 'Event', 'MouseEvent', 'KeyboardEvent', 'getComputedStyle', 'requestAnimationFrame', 'cancelAnimationFrame', 'localStorage', 'sessionStorage', 'location', 'history', 'customElements', 'DocumentFragment', 'MutationObserver']) {
  const v = k === 'window' ? dom.window : dom.window[k];
  try { g[k] = v; } catch { Object.defineProperty(g, k, { value: v, configurable: true, writable: true }); }
}
Object.defineProperty(g, 'navigator', { value: dom.window.navigator, configurable: true });
g.IS_REACT_ACT_ENVIRONMENT = true;
export default dom;
