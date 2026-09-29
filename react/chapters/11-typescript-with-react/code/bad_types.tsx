// tools_run: skip  (deliberately WRONG code - type-checked with tsconfig.bad.json to show real errors)
import { useState, useRef, useReducer } from 'react';
import type { ChangeEvent, ReactNode } from 'react';

type BadgeProps = { label: string; count?: number; tone?: 'info' | 'warning' | 'error' };
function Badge({ label, count = 0, tone = 'info' }: BadgeProps) { return <span className={tone}>{label}{count}</span>; }
const e1 = <Badge />;                                   // 1. required prop missing
const e2 = <Badge label="x" tone="danger" />;           // 2. value outside the union

function Profile() {
  const [user] = useState<{ name: string } | null>(null);
  const [tags, setTags] = useState([]);
  setTags(['a']);                                       // 4. useState([]) without a type -> never[]
  return <p>{user.name} {tags.length}</p>;              // 3. user may be null
}

function Plan() {
  function handle(e: ChangeEvent<HTMLInputElement>) { console.log(e.target.value); }
  return <select onChange={handle} />;                  // 5. input handler on a <select>
}

type StatusProps = { status: 'loading' } | { status: 'success'; count: number };
function Status(props: StatusProps) { return <p>{props.status}</p>; }
const e6 = <Status status="success" />;                 // 6. 'success' requires count

type ImageSource = { src: string; icon?: never } | { icon: 'user' | 'star'; src?: never };
function Thumb(props: ImageSource) { return <i>{props.src ?? props.icon}</i>; }
const e7 = <Thumb src="/a.png" icon="star" />;          // 7. both src and icon

type Column<Row> = { key: keyof Row & string; header: string };
function Table<Row>({ rows, columns }: { rows: Row[]; columns: Column<Row>[] }) { return <p>{rows.length}{columns.length}</p>; }
const e8 = <Table rows={[{ id: 1, title: 'Dune' }]} columns={[{ key: 'author', header: 'Author' }]} />;  // 8. no such key

type Action = { type: 'add'; sku: string } | { type: 'clear' };
function reducer(state: string[], action: Action): string[] {
  switch (action.type) {
    case 'add': return [...state, action.sku];
    default: { const unreachable: never = action; return unreachable; }   // 9. 'clear' not handled
  }
}
function Cart() { const [items, dispatch] = useReducer(reducer, []); dispatch({ type: 'remove' }); return <p>{items}</p>; }  // 10. unknown action

function Focus() {
  const ref = useRef<HTMLInputElement>();               // 11. React 19 types: useRef needs an argument
  return <input ref={ref} />;
}

function Box({ children }: { children: ReactNode }) { return <div>{children}</div>; }
const e12 = <Box>{{ title: 'not renderable' }}</Box>;   // 12. a plain object is not a ReactNode

export { e1, e2, e6, e7, e8, e12, Profile, Plan, Cart, Focus };
