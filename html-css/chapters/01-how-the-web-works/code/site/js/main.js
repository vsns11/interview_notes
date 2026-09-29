// tools_run: skip  (browser script, loaded by site/index.html)
document.getElementById('hi').addEventListener('click', () => {
  document.getElementById('msg').textContent = 'Hi! JavaScript ran.';
});
