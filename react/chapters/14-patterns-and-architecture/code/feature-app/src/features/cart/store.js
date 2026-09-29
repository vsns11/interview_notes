// tools_run: skip  (sample project file for 08_import_boundaries.mjs)
let state = { items: [], total: 0 }; const subs = new Set();
export const cartStore = { get: () => state, subscribe: (f) => (subs.add(f), () => subs.delete(f)), add(p) { state = { items: [...state.items, p], total: state.total + p.price }; subs.forEach(f => f()); } };
