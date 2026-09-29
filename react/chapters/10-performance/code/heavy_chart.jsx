// tools_run: skip  (imported lazily by 06_lazy_suspense.jsx)
// Imagine this module pulls in a 300 kB charting library - we only want to download it when needed.
console.log('    [module] heavy_chart.jsx evaluated (its code was just loaded)');
export default function HeavyChart({ points }) {
  return <figure>chart with {points.length} points</figure>;
}
export function Legend() { return <small>legend</small>; }
