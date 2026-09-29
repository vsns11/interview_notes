// tools_run: skip  (browser entry for catalog.html)
import { createRoot } from 'react-dom/client';
import { Catalog, PRODUCTS } from './catalog.jsx';
import './catalog.css';

createRoot(document.getElementById('root')).render(<Catalog products={PRODUCTS} />);
