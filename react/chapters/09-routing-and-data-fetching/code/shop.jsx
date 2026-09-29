// tools_run: skip  (app module) — React Router loaders + TanStack Query cache working together.
import { Link, NavLink, Outlet, useLoaderData, useNavigation, useRouteError, isRouteErrorResponse, useParams, data } from 'react-router';
import { queryOptions, useQuery, useSuspenseQuery } from '@tanstack/react-query';

export function makeRoutes({ base = '', queryClient }) {
  const getJson = async (path) => {
    const res = await fetch(base + path);
    if (res.status === 404) throw data('This product does not exist', { status: 404 });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  };
  const fresh = 30_000;                                            // treat data as fresh for 30 s
  const productsQuery = queryOptions({ queryKey: ['products'], queryFn: () => getJson('/api/products'), staleTime: fresh });
  const productQuery = (id) => queryOptions({ queryKey: ['products', id], queryFn: () => getJson(`/api/products/${id}`), staleTime: fresh });
  const reviewsQuery = (id) => queryOptions({ queryKey: ['products', id, 'reviews'], queryFn: () => getJson(`/api/products/${id}/reviews`), staleTime: fresh });

  function Layout() {
    const navigation = useNavigation();
    const { data: products } = useSuspenseQuery(productsQuery);
    return (
      <div className="shop">
        <header><strong>Gadget Shop</strong>{navigation.state !== 'idle' && <span className="busy">loading…</span>}</header>
        <nav>{products.map(p => <NavLink key={p.id} to={`/products/${p.id}`}>{p.name}</NavLink>)}</nav>
        <main><Outlet /></main>
      </div>
    );
  }
  function Home() { return <p>Choose a product.</p>; }
  function Product() {
    const { id } = useParams();
    const { data: product } = useSuspenseQuery(productQuery(id));   // already in the cache thanks to the loader
    const reviews = useQuery(reviewsQuery(id));                      // not awaited by the loader: streams in after
    return (
      <article>
        <h1>{product.name}</h1>
        <p className="price">${product.price} · {product.category}</p>
        <h2>Reviews</h2>
        {reviews.isPending ? <p className="muted">Loading reviews…</p>
          : reviews.data.length ? <ul>{reviews.data.map(r => <li key={r}>{r}</li>)}</ul>
          : <p className="muted">No reviews yet.</p>}
        <Link to="/">Back</Link>
      </article>
    );
  }
  function RouteError() {
    const error = useRouteError();
    return <p role="alert">{isRouteErrorResponse(error) ? `${error.status} - ${error.data}` : error.message}</p>;
  }

  return [{
    path: '/', Component: Layout, ErrorBoundary: RouteError, HydrateFallback: () => <p>Loading shop…</p>,
    loader: () => queryClient.ensureQueryData(productsQuery),        // fetch if missing, reuse if cached
    children: [
      { index: true, Component: Home },
      { path: 'products/:id', Component: Product, ErrorBoundary: RouteError,
        loader: async ({ params }) => {
          queryClient.prefetchQuery(reviewsQuery(params.id));          // start, but don't wait (non-critical)
          await queryClient.ensureQueryData(productQuery(params.id));  // wait (critical)
          return null;
        } },
    ],
  }];
}
