// Web Workers run in browsers only; Node's worker_threads use the same idea:
// a separate thread with its own event loop, talking via postMessage (structured clone).
import { Worker } from 'node:worker_threads';

const workerSource = `
  const { parentPort } = require('node:worker_threads');
  parentPort.on('message', ({ upTo, sample }) => {
    let count = 0;
    for (let n = 2; n <= upTo; n++) {           // deliberately slow prime counting
      let prime = true;
      for (let d = 2; d * d <= n; d++) if (n % d === 0) { prime = false; break; }
      if (prime) count++;
    }
    parentPort.postMessage({ upTo, count, sampleIsCopy: sample.when instanceof Date, mapSize: sample.tags.size });
  });`;

const worker = new Worker(workerSource, { eval: true });
const sample = { when: new Date(0), tags: new Map([['a', 1]]) };   // Date and Map survive structured cloning
const ticks = [];
const ticker = setInterval(() => ticks.push('tick'), 5);             // the main thread stays responsive

const result = await new Promise((resolve, reject) => {
  worker.once('message', resolve);
  worker.once('error', reject);
  worker.postMessage({ upTo: 3_000_000, sample });
});
clearInterval(ticker);
console.log('worker result :', result);
console.log('main thread kept running while the worker computed:', ticks.length > 3);

// Functions (and DOM nodes, class prototypes) cannot be cloned
try { worker.postMessage({ fn: () => 1 }); } catch (e) { console.log('clone error   :', e.name); }
console.log('structuredClone keeps Date/Map:', structuredClone(sample).tags instanceof Map);
await worker.terminate();
