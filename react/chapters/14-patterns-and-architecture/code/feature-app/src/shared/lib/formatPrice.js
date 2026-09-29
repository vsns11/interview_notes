// tools_run: skip  (sample project file for 08_import_boundaries.mjs)
import { useCart } from '../../features/cart';                  // BAD: shared code must not depend on features
export const formatPrice = (n) => '$' + n.toFixed(2);
