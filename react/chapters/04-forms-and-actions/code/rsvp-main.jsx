// tools_run: skip  (browser entry for rsvp.html — starts in the state "after one invalid submit")
import { createRoot } from 'react-dom/client';
import Rsvp, { createApi } from './rsvp.jsx';
import './rsvp.css';

const initialGuests = [
  { id: 1, name: 'Host', email: 'host@example.com', guests: 1, diet: 'none' },
  { id: 2, name: 'Ada', email: 'ada@example.com', guests: 2, diet: 'vegan' },
];
const initialResult = {
  errors: { email: 'Enter an email like ada@example.com' },
  values: { name: 'Grace', email: 'grace@', guests: '3', diet: 'vegetarian' },
  message: '',
};
createRoot(document.getElementById('root')).render(<Rsvp saveRsvp={createApi(800)} initialGuests={initialGuests} initialResult={initialResult} />);
