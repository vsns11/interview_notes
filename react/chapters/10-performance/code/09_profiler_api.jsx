// The <Profiler> API: measure rendering in code (works in development builds; production needs the
// profiling build). Here we collect every commit and print a small report, like a perf dashboard would.
import '../../../../tools_dom.mjs';
import { useState, memo, Profiler } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

const samples = [];
function onRender(id, phase, actualDuration, baseDuration, startTime, commitTime) {
  samples.push({ id, phase, actual: actualDuration, base: baseDuration, commitTime });
}
const busy = ms => { const t = performance.now(); while (performance.now() - t < ms) {} };

const Sidebar = memo(function Sidebar() { busy(3); return <nav>links</nav>; });   // memoized, 3 ms
function Feed({ posts }) { busy(5); return <section>{posts} posts</section>; }    // 5 ms every render

function App() {
  const [posts, setPosts] = useState(10);
  return (
    <Profiler id="app" onRender={onRender}>
      <button onClick={() => setPosts(p => p + 1)}>load more</button>
      <Profiler id="sidebar" onRender={onRender}><Sidebar /></Profiler>
      <Profiler id="feed" onRender={onRender}><Feed posts={posts} /></Profiler>
    </Profiler>
  );
}

render(<App />);
fireEvent.click(screen.getByText('load more'));
fireEvent.click(screen.getByText('load more'));

// Round to whole ms so the report is readable; exact numbers vary from run to run.
console.log('id       phase    actual  base  (ms)');
for (const s of samples)
  console.log(`${s.id.padEnd(8)} ${s.phase.padEnd(8)} ${String(Math.round(s.actual)).padStart(6)} ${String(Math.round(s.base)).padStart(5)}`);
const commits = new Set(samples.map(s => s.commitTime)).size;
console.log(`\n${samples.length} onRender calls in ${commits} commits (profilers in one commit share commitTime)`);
