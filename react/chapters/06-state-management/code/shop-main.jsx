// tools_run: skip  (browser entry for shop.html — the cart starts with three books so the screenshot shows both panels)
import { createRoot } from 'react-dom/client';
import Shop, { createCatalogApi, useCart } from './shop.jsx';
import './shop.css';

useCart.setState({ lines: { b2: 1, b3: 2 } });     // a store can be seeded from outside React (e.g. from localStorage)
createRoot(document.getElementById('root')).render(<Shop fetchBooks={createCatalogApi(10)} />);
