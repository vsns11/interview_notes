// tools_run: skip  (browser script loaded by 01_*.html)
// Pretend app code: adds behaviour to the page; nothing needed for the first paint.
document.documentElement.dataset.appLoaded = String(Math.round(performance.now()));
