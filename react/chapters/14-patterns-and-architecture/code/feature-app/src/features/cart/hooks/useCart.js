// tools_run: skip  (sample project file for 08_import_boundaries.mjs)
import { useSyncExternalStore } from 'react';
import { cartStore } from '../store.js';
export function useCart() { return useSyncExternalStore(cartStore.subscribe, cartStore.get); }
