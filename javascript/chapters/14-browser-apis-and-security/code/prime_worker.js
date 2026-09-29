// tools_run: skip  (browser Web Worker for worker.html)
self.onmessage = (e) => {
  const upTo = e.data;
  let count = 0;
  for (let n = 2; n <= upTo; n++) {
    let prime = true;
    for (let d = 2; d * d <= n; d++) if (n % d === 0) { prime = false; break; }
    if (prime) count++;
  }
  self.postMessage({ upTo, count });
};
