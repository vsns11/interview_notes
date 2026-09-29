// tools_run: skip  (browser entry for ds_demo.html — bundled to ds_demo.bundle.js, screenshotted)
import { createRoot } from 'react-dom/client';
import { useState } from 'react';
import { Button } from './Button.jsx';
import { Tabs } from './Tabs.jsx';
import { Dialog } from './Dialog.jsx';

function Demo() {
  const [open, setOpen] = useState(!!window.OPEN_DIALOG);   // ds_dialog.html starts with the dialog open
  return (
    <main>
      <section className="card">
        <h1>Trail Mug</h1>
        <div className="row">
          <Button>Add to cart</Button>
          <Button variant="secondary">Save for later</Button>
          <Button variant="danger" size="sm" onClick={() => setOpen(true)}>Delete</Button>
        </div>
      </section>
      <section className="card">
        <Tabs defaultValue="reviews">
          <Tabs.List label="Product information">
            <Tabs.Tab value="details">Details</Tabs.Tab>
            <Tabs.Tab value="reviews">Reviews</Tabs.Tab>
            <Tabs.Tab value="shipping">Shipping</Tabs.Tab>
          </Tabs.List>
          <Tabs.Panel value="details">Enamel mug, 350 ml.</Tabs.Panel>
          <Tabs.Panel value="reviews">4.5 stars from 12 reviews.</Tabs.Panel>
          <Tabs.Panel value="shipping">Ships in 2 days.</Tabs.Panel>
        </Tabs>
      </section>
      <Dialog open={open} onClose={() => setOpen(false)} title="Delete Trail Mug?">
        <p>This removes the product from the catalogue. This cannot be undone.</p>
      </Dialog>
    </main>
  );
}
createRoot(document.getElementById('root')).render(<Demo />);
