// tools_run: skip  (sample project file for 08_import_boundaries.mjs)
import { useCart } from '../hooks/useCart.js';
import { Button } from '../../../shared/ui/Button.jsx';
import { formatPrice } from '../../../shared/lib/formatPrice.js';
export function CartPanel() { const { items, total, clear } = useCart(); return <aside>{items.length} items, {formatPrice(total)} <Button onClick={clear}>Clear</Button></aside>; }
