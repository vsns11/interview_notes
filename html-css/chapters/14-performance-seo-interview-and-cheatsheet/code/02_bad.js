// tools_run: skip  (browser script for 02_bad.html)
document.addEventListener('DOMContentLoaded', () => {
  // LCP problem: the hero image is only discovered after this script runs, it is lazy-loaded,
  // and CLS problem: it has no width/height, so the text below jumps when it arrives.
  document.getElementById('hero').innerHTML = '<img src="media/hills-800.jpg" alt="Hills at sunset" loading="lazy">';

  // CLS problem: a promo banner is inserted above the content after 1 s.
  setTimeout(() => {
    document.getElementById('promo-slot').innerHTML = '<div class="promo"><strong>Free shipping this weekend!</strong><br>Use code TRAIL at checkout.</div>';
  }, 1000);

  // INP problem: the click handler blocks the main thread for ~300 ms before the UI updates.
  document.getElementById('save').addEventListener('click', () => {
    const end = performance.now() + 300;
    while (performance.now() < end) { /* "expensive" work: sync analytics, big JSON, … */ }
    document.getElementById('status').textContent = 'Saved ✓';
  });
});
