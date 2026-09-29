// tools_run: skip  (browser script for 02_fixed.html)
setTimeout(() => {   // same banner, same time — but into a slot whose height is already reserved
  document.getElementById('promo-slot').innerHTML = '<div class="promo"><strong>Free shipping this weekend!</strong><br>Use code TRAIL at checkout.</div>';
}, 1000);

// Yield to the browser so it can paint, then continue the work in small pieces.
const yieldToMain = () => globalThis.scheduler?.yield ? scheduler.yield() : new Promise(r => setTimeout(r, 0));

document.getElementById('save').addEventListener('click', async () => {
  document.getElementById('status').textContent = 'Saved ✓';      // 1. visible feedback first
  await yieldToMain();                                            // 2. let the next frame paint
  for (let i = 0; i < 6; i++) {                                   // 3. same 300 ms of work, in 50 ms chunks
    const end = performance.now() + 50;
    while (performance.now() < end) { /* work */ }
    await yieldToMain();
  }
});
