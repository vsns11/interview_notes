// Machine coding 3 — infinite scroll with IntersectionObserver.
// `fetchPage(page)` resolves to { items, hasMore }; it is injected so tests and the demo need no network.
import { useEffect, useRef, useState } from 'react';

export default function InfiniteList({ fetchPage, renderItem = (item) => item.name, rootMargin = '200px' }) {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);                      // last page loaded (0 = none yet)
  const [status, setStatus] = useState('idle');             // 'idle' | 'loading' | 'error'
  const [hasMore, setHasMore] = useState(true);
  const scrollerRef = useRef(null);
  const sentinelRef = useRef(null);
  const mounted = useRef(false);
  const inFlight = useRef(false);                           // sync guard: state updates aren't instant

  useEffect(() => {
    mounted.current = true;
    return () => { mounted.current = false; };              // late responses after unmount are dropped
  }, []);

  // Re-created whenever status/page/hasMore change. A new observer reports the sentinel's current
  // state immediately, so if page N didn't fill the box, page N+1 loads without any scrolling.
  useEffect(() => {
    if (status !== 'idle' || !hasMore) return;              // never two requests at once, stop at the end
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry.isIntersecting || inFlight.current) return;
        inFlight.current = true;
        observer.disconnect();
        setStatus('loading');
        fetchPage(page + 1).then(
          (res) => {
            inFlight.current = false;
            if (!mounted.current) return;
            setItems((prev) => [...prev, ...res.items]);
            setPage(page + 1);
            setHasMore(res.hasMore);
            setStatus('idle');
          },
          () => { inFlight.current = false; if (mounted.current) setStatus('error'); },
        );
      },
      { root: scrollerRef.current, rootMargin },           // start loading 200px before the end
    );
    observer.observe(sentinelRef.current);
    return () => observer.disconnect();
  }, [status, page, hasMore, fetchPage, rootMargin]);

  return (
    <section className="card feed" aria-labelledby="feed-h">
      <h2 id="feed-h">Feed <small className="muted">({items.length} loaded)</small></h2>
      <div className="scroller" ref={scrollerRef} tabIndex={0} aria-label="Feed items">
        <ul className="list" aria-busy={status === 'loading'}>
          {items.map((item) => <li key={item.id}>{renderItem(item)}</li>)}
        </ul>
        <div ref={sentinelRef} aria-hidden="true" className="sentinel" />
        {status === 'loading' && <p role="status" className="muted">Loading more…</p>}
        {status === 'error' && (
          <p role="alert" className="error">
            Couldn’t load more. <button onClick={() => setStatus('idle')}>Retry</button>
          </p>
        )}
        {!hasMore && <p className="muted">You’ve reached the end.</p>}
      </div>
    </section>
  );
}
