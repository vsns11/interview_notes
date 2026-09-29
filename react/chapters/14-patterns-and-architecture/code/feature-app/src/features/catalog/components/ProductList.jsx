// tools_run: skip  (sample project file for 08_import_boundaries.mjs)
import { ProductCard } from './ProductCard.jsx';
import { cartStore } from '../../cart/store.js';               // BAD: reaches into another feature's internals
export function ProductList() { return <ul><ProductCard p={{ name: 'Trail Mug', price: 18 }} /></ul>; }
