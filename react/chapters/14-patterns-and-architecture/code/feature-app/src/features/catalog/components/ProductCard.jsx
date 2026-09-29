// tools_run: skip  (sample project file for 08_import_boundaries.mjs)
import { useCart } from '../../cart';                       // OK: through the cart's public API
import { Button } from '../../../shared/ui/Button.jsx';
export function ProductCard({ p }) { useCart(); return <li>{p.name} <Button>Add</Button></li>; }
