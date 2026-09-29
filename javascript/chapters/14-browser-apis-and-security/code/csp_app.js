// tools_run: skip  (browser script for csp.html - loaded from 'self', so CSP allows it)
const log = document.getElementById('log');
document.addEventListener('securitypolicyviolation', (e) => {
  log.textContent += `blocked ${e.effectiveDirective.padEnd(15)} ${e.blockedURI || '(inline)'}\n`;
});
// An attacker's comment slipped through into innerHTML. Its inline onerror handler and the
// external image are both blocked by the policy.
document.getElementById('comment').innerHTML =
  'Great article! <img src="https://evil.example/pixel.png" onerror="document.body.style.background=\'red\'">';
setTimeout(() => {
  log.textContent += 'external script csp_app.js ran (allowed: same origin)\n';
}, 300);
