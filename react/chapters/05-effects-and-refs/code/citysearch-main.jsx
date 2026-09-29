// tools_run: skip  (browser entry for citysearch.html — opens with the query "o" and Tokyo's tooltip showing)
import { createRoot } from 'react-dom/client';
import CitySearch, { createCityApi } from './citysearch.jsx';
import './citysearch.css';

createRoot(document.getElementById('root')).render(<CitySearch searchCities={createCityApi(0)} delay={0} initialQuery="o" tipFor="Tokyo" />);
