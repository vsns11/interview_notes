// tools_run: skip  (component module: used by 07_product_catalog.jsx and the browser entry catalog-main.jsx)
export const PRODUCTS = [
  { id: 'p1', name: 'Mechanical keyboard', price: 89, stock: 12, rating: 4.6, tags: ['new'] },
  { id: 'p2', name: 'USB-C hub', price: 35, stock: 0, rating: 4.1, tags: [] },
  { id: 'p3', name: '27" monitor', price: 249, stock: 3, rating: 4.8, tags: ['sale'] },
  { id: 'p4', name: 'Laptop stand', price: 42, stock: 25, rating: 3.9, tags: [] },
];

export function Price({ amount, currency = 'USD' }) {
  const text = new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
  return <span className="price">{text}</span>;
}

export function Badge({ tone = 'grey', children }) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

export function StockLabel({ stock }) {
  if (stock === 0) return <Badge tone="red">Out of stock</Badge>;
  if (stock < 5) return <Badge tone="amber">Only {stock} left</Badge>;
  return null;                                   // plenty in stock: show nothing
}

export function ProductCard({ product }) {
  const { name, price, stock, rating, tags } = product;
  return (
    <article className="product" aria-label={name}>
      <h3>{name}</h3>
      <div className="badges">
        {tags.includes('new') && <Badge tone="blue">New</Badge>}
        {tags.includes('sale') && <Badge tone="green">Sale</Badge>}
        <StockLabel stock={stock} />
      </div>
      <p><Price amount={price} /> · ★ {rating.toFixed(1)}</p>
      <button disabled={stock === 0}>{stock === 0 ? 'Sold out' : 'Add to cart'}</button>
    </article>
  );
}

export function Section({ title, children }) {
  return (
    <section className="section">
      <h2>{title}</h2>
      {children}
    </section>
  );
}

export function Catalog({ products, minRating = 0 }) {
  const visible = products.filter((p) => p.rating >= minRating);
  if (visible.length === 0) return <p>No products match.</p>;
  return (
    <Section title={`Products (${visible.length})`}>
      <div className="grid">
        {visible.map((p) => <ProductCard key={p.id} product={p} />)}
      </div>
    </Section>
  );
}
