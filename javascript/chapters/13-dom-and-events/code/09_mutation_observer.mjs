import '../../../../tools_dom.mjs';
// MutationObserver: get notified (as a microtask, in a batch) when the DOM changes
document.body.innerHTML = '<ul id="feed"></ul>';
const feed = document.getElementById('feed');

const observer = new MutationObserver((records) => {
  console.log(`observer got ${records.length} records:`,
    records.map((r) => r.type === 'childList' ? `childList +${r.addedNodes.length} -${r.removedNodes.length}` : `${r.type}:${r.attributeName}`));
});
observer.observe(feed, { childList: true, attributes: true, subtree: true });

for (let i = 1; i <= 3; i++) feed.insertAdjacentHTML('beforeend', `<li>post ${i}</li>`);
feed.firstElementChild.remove();
feed.setAttribute('data-count', '2');
console.log('sync code done - no callback yet (it is a microtask)');
await null;
observer.disconnect();
feed.append('ignored after disconnect');
await null;
console.log('done');
