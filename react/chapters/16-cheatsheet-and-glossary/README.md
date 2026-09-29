# Chapter 16 — Cheat Sheets and Glossary

This last chapter squeezes Chapters 1–15 into pages you can scan in a minute. There is one **cheat sheet** per area (JSX, components, every Hook, state, the render/commit lifecycle, Effects, refs, forms, state management, routing and data, performance, styling, TypeScript, testing, server rendering, patterns, accessibility), a **"common errors and fixes"** table built from the real messages React printed in earlier chapters, an **index of the 53 interview questions** in Chapter 15, a **"where to go next"** plan, and an alphabetical **glossary** of 222 terms, each linked to the section that explains it properly.

Nothing here is new. If a line doesn't make sense, follow its link back to the full explanation. References look like **§9.5** — Chapter 9, section 5.

**What you'll learn:** how to use this chapter as a revision tool. Read a sheet, cover the right-hand column, and say the answer before you look. The glossary is a good last check before an interview: if you can't define a term in one sentence, follow its link.

**How the facts were checked.** Every concrete behaviour quoted on these sheets — batching, updater queues, key resets, StrictMode double calls, Effect order, default values, error messages — is asserted by [`code/01_cheatsheet_checks.jsx`](code/01_cheatsheet_checks.jsx). It runs React 19.3 in jsdom (plus React Router 8.4 and TanStack Query 5.104) and fails loudly if React behaves differently from what a sheet says. Run it with `bash tools_run_js.sh react` from the repository root. Real output:

```text
OK   16.1 JSX rules
OK   16.2 components and props
OK   16.3 hooks table (identity, lazy init, memo caches, useId, deferred value)
OK   16.4 state: snapshot, updaters, batching, bail-out, immutability, position & key
OK   16.5 render/commit lifecycle order
OK   16.6 effects: dependency arrays, cleanup, StrictMode double run
OK   16.7 refs: no re-render, null during first render, callback cleanup, ref as a prop
OK   16.8 forms: FormData rules, form actions, useActionState, useFormStatus, useOptimistic
OK   16.9 context: default, nearest provider, memo does not block it, use() may be conditional
OK   16.10 React Router params and ranking; TanStack Query defaults and de-duplication
OK   16.11 performance: memo, unstable props, children from above, useMemo, lazy
OK   16.14 testing: get/query/find semantics, role + name, user-event vs fireEvent
OK   16.15 server rendering: markers, no effects on the server, suspended boundaries, hydration reuse, use(browser()), resume APIs
OK   16.16 patterns: portals bubble through React, boundaries catch render errors only, HOC in render remounts
OK   16.18 common errors: the exact messages React 19.3 produces
all 15 cheat-sheet checks passed (React 19.3.0)

messages seen in 16.18:
  Too many re-renders.
  Objects are not valid as a React child (found: object with keys {name}).
  Rendered more hooks than during the previous render.
  Rendered fewer hooks than expected. This may be caused by an accidental early return statement.
  Invalid hook call. Hooks can only be called inside of the body of a function component.
  The result of getSnapshot should be cached to avoid an infinite loop
  Maximum update depth exceeded.
  Element type is invalid. Received a promise that resolves to: undefined. Lazy element type must resolve to a class or function.
  Each child in a list should have a unique "key" prop.
  Encountered two children with the same key, `1`.
  A component is changing an uncontrolled input to be controlled.
  You provided a `value` prop to a form field without an `onChange` handler.
  Invalid DOM property `class`.
  The tag <profile> is unrecognized in this browser.
  Unsupported style property @media (max-width: 600px).
  A component was suspended by an uncached promise.
  React has blocked a javascript: URL as a security precaution.
  An update to T inside a test was not wrapped in act(...).
  Hydration failed because the server rendered text didn't match the client.
```

(§16.12 Styling, §16.13 TypeScript and §16.17 Accessibility summarise results that Chapters 8, 11 and 14 already verified with a real browser, the TypeScript compiler and role queries, so they have no separate check here.)

---

## 16.1 JSX rules

| Rule | Write | Not | Why |
|---|---|---|---|
| One root element | `<>…</>` or `<div>…</div>` | two siblings at the top | A function returns one value; JSX becomes one object |
| Close every tag | `<img />`, `<br />`, `<input />` | `<img>` | JSX is stricter than HTML |
| camelCase attributes | `className`, `htmlFor`, `onClick`, `tabIndex` | `class`, `for`, `onclick` | Attributes become JS object keys; `class`/`for` are reserved. `aria-*` and `data-*` keep dashes |
| `{}` holds an **expression** | `{user.name}`, `{a ? b : c}`, `{list.map(…)}` | `{if (…) …}` | Statements (`if`, `for`, `const`) go above the `return` |
| Strings in quotes, everything else in braces | `alt="text"`, `size={50}`, `admin` (= `true`) | `size="50"` for a number | Quotes always pass a string |
| `style` is an object | `style={{ fontSize: 14, lineHeight: 1.5 }}` → `font-size:14px;line-height:1.5` | `style="font-size:14px"` | Numbers get `px`, except unitless properties (`lineHeight`, `zIndex`, `opacity`, `flexGrow`) |
| Comments | `{/* note */}` | `<!-- note -->` | JSX is JavaScript |
| Keyed fragment | `<Fragment key={id}>…</Fragment>` | `<key={id}>` | The short `<>` takes no props |

**What renders what**

| Value in `{}` | Screen shows |
|---|---|
| `null`, `undefined`, `true`, `false` | nothing |
| `0`, `NaN` | "0", "NaN" — the **`{count && <Badge/>}` trap**; write `count > 0 &&` |
| `''` | an empty text node (invisible) |
| strings, numbers | the text, **HTML-escaped** (`<script>` shows as text — built-in XSS protection) |
| arrays of elements | each element (give each a `key`) |
| a plain object | throws *"Objects are not valid as a React child"* |

**Remember**
- JSX compiles to `jsx(type, props)` / `createElement(type, props, ...children)` and returns an **immutable element object** `{ type, props, key }`. Since React 17 (automatic runtime) you don't `import React`. [§1.8](../01-why-react-and-setup/README.md#18-jsx)
- Capitalised tag = your component (`<Profile />`); lowercase = HTML tag (`<profile>` is just an unknown element, and React warns). [§1.7](../01-why-react-and-setup/README.md#17-your-first-component), [§2.1](../02-components-and-props/README.md#21-function-components)
- Raw HTML only through `dangerouslySetInnerHTML={{ __html: sanitised }}`. React 19 replaces a `javascript:` URL in `href` with one that throws. [§1.8](../01-why-react-and-setup/README.md#18-jsx)
- Conditional rendering: `if` + early `return` for big branches, `? :` for either/or, `&&` with a **boolean** on the left, `null` for nothing. [§2.7](../02-components-and-props/README.md#27-conditional-rendering)

---

## 16.2 Components and props

```jsx
// A component: capitalised function, one props object in, JSX out, declared at module top level.
export function Avatar({ person, size = 100, className, ...rest }) {   // destructure, default, collect the rest
  return <img className={className} src={person.url} alt={person.name} width={size} {...rest} />;
}
<Avatar person={ada} size={50} />         // strings in quotes, other values in braces
```

| Topic | Rule |
|---|---|
| Name and place | Capitalised; defined at **module top level**, never inside another component (a new type every render → state and DOM reset) [§1.7](../01-why-react-and-setup/README.md#17-your-first-component) |
| Use | `<Profile />`, never `Profile()` — calling it directly gives it no place in the tree and its Hooks belong to the caller [§2.1](../02-components-and-props/README.md#21-function-components) |
| Props | One **read-only** object from the parent; frozen in development (mutating throws `TypeError: Cannot assign to read only property`) [§2.2](../02-components-and-props/README.md#22-props) |
| Defaults | Destructuring default `({ size = 100 })` applies when the prop is **missing or `undefined`** — not for `null` or `0`. `defaultProps`/`propTypes` are gone for function components in React 19 [§2.2](../02-components-and-props/README.md#22-props) |
| Special props | `key` is consumed by React (`props.key` is `undefined`); `children` = whatever is nested; `ref` is an ordinary prop in React 19 [§2.2](../02-components-and-props/README.md#22-props), [§5.10](../05-effects-and-refs/README.md#510-passing-refs-to-your-own-components) |
| Forwarding | `({ variant, ...rest }) => <button {...rest} />`; later props override earlier ones [§2.2](../02-components-and-props/README.md#22-props) |
| `children` | Anything renderable; render it as `{children}` to build wrappers (`Card`, `Layout`) [§2.3](../02-components-and-props/README.md#23-the-children-prop) |
| Composition | Pass JSX as `children` or **slot** props (`header={…}`) instead of inheritance or boolean-prop explosions; first cure for prop drilling [§2.4](../02-components-and-props/README.md#24-composition), [§14.1](../14-patterns-and-architecture/README.md#141-composition-first) |
| Purity | Same props/state/context → same JSX; no changes to outside variables during render (local mutation of values created in this render is fine) [§2.5](../02-components-and-props/README.md#25-pure-components) |
| Size | One job per component; extract when JSX has a name, repeats, has its own logic or needs its own state [§2.6](../02-components-and-props/README.md#26-keeping-components-small) |
| Lists | `items.map(i => <Row key={i.id} … />)`; key on the outermost element returned by `map` [§2.8](../02-components-and-props/README.md#28-rendering-lists-and-keys) |

**Keys in one table** [§2.8](../02-components-and-props/README.md#28-rendering-lists-and-keys), [§3.10](../03-state-and-events/README.md#310-state-is-tied-to-a-position-in-the-tree)

| Key choice | Result |
|---|---|
| Stable ID from the data | Correct: items keep DOM, state and focus when moved |
| Array index | OK only for static lists; on insert/sort/filter, state stays at the **position** (the "note moves to Eggs" bug) |
| `Math.random()` / `crypto.randomUUID()` in JSX | Every item remounts every render — slow, loses input focus |
| Missing | Warning, index used implicitly |
| Duplicate | Warning, items duplicated or dropped |
| Changing the key of one component on purpose | Fresh instance: the idiomatic way to **reset** state (`<Profile key={userId} />`) |

---

## 16.3 Every Hook in one table

"Since" = the React version that made it stable. All Hooks follow the Rules of Hooks (top level of a component or custom Hook, same order every render) — except `use`, which may be called in `if`/loops (not in `try/catch`). [§7.2](../07-custom-hooks-and-hook-rules/README.md#72-the-rules-of-hooks)

| Hook | Signature | What it's for | Gotchas | Since |
|---|---|---|---|---|
| `useState` | `const [v, setV] = useState(init \| () => init)` | Memory that changes with interaction; setter schedules a re-render | Value is a snapshot; initial value used on first render only; pass a function for lazy init; setter identity is stable; `Object.is`-equal value → bail-out [§3.2](../03-state-and-events/README.md#32-state-a-components-memory--usestate) | 16.8 |
| `useReducer` | `const [s, dispatch] = useReducer(reducer, initialArg, init?)` | Many related updates in one pure `(state, action) => next` | Reducer must be pure (StrictMode calls it twice); `dispatch` is stable; throw on unknown actions [§6.2](../06-state-management/README.md#62-usereducer--consolidate-update-logic) | 16.8 |
| `useContext` | `const v = useContext(Ctx)` | Read the nearest provider's value | Default only when **no** provider; re-renders on every value change, even through `memo` [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props), [§6.4](../06-state-management/README.md#64-context-performance-caveats) | 16.8 |
| `use` | `const v = use(promise \| Ctx)` | Read a promise (suspends; rejection → error boundary) or a context | Promise must be cached/created outside render — a new promise each render triggers "suspended by an uncached promise"; allowed in `if`/loops [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use) | 19.0 |
| `useRef` | `const r = useRef(init)` → `{ current }` | Values that don't affect rendering (timer IDs, DOM nodes, instances) | Changing `r.current` never re-renders; don't read/write it during render (except lazy init); DOM refs are `null` until commit [§5.8](../05-effects-and-refs/README.md#58-useref-for-values), [§5.9](../05-effects-and-refs/README.md#59-refs-to-dom-nodes) | 16.8 |
| `useImperativeHandle` | `useImperativeHandle(ref, () => api, deps?)` | Expose a limited API (`focus`, `clear`) instead of the DOM node | Pair with `ref` as a prop (React 19) [§5.10](../05-effects-and-refs/README.md#510-passing-refs-to-your-own-components) | 16.8 |
| `useEffect` | `useEffect(() => { setup; return cleanup }, deps?)` | Synchronise with an external system after commit | No array = every commit; `[]` = mount only; `[a]` = when `a` changes (`Object.is`); setup can't be `async`; doesn't run on the server [§5.2](../05-effects-and-refs/README.md#52-useeffect-basics) | 16.8 |
| `useLayoutEffect` | same as `useEffect` | Measure layout and fix it **before paint** (no flicker) | Blocks painting; does nothing during SSR; default to `useEffect` [§5.11](../05-effects-and-refs/README.md#511-uselayouteffect--before-the-browser-paints) | 16.8 |
| `useInsertionEffect` | same shape | CSS-in-JS libraries inserting `<style>` before layout effects | Not for app code [§5.11](../05-effects-and-refs/README.md#511-uselayouteffect--before-the-browser-paints) | 18.0 |
| `useEffectEvent` | `const onX = useEffectEvent(fn)` | Non-reactive logic inside an Effect that must see the **latest** props/state | Call only from Effects; never list it in dependencies; don't pass it to other components [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth), [§7.7](../07-custom-hooks-and-hook-rules/README.md#77-hooks-that-take-callbacks--useeffectevent) | 19.2 |
| `useMemo` | `const v = useMemo(() => calc(a), [a])` | Cache an expensive result (~1 ms+) or keep an object's identity stable | Must be pure; a cache, not a guarantee; unstable deps make it useless [§10.6](../10-performance/README.md#106-usememo-for-expensive-calculations), [§10.5](../10-performance/README.md#105-usecallback-and-usememo-for-stable-props) | 16.8 |
| `useCallback` | `const fn = useCallback(fn, [a])` | Same function identity for `memo` children / Effect deps | = `useMemo(() => fn, deps)`; missing deps → stale closure [§10.5](../10-performance/README.md#105-usecallback-and-usememo-for-stable-props) | 16.8 |
| `useTransition` | `const [isPending, startTransition] = useTransition()` | Mark updates non-urgent; keep old UI during Suspense | Can't wrap a controlled input's value; since 19 the callback may be `async` (an Action) — updates **after an `await`** need their own `startTransition` [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates) | 18.0 |
| `useDeferredValue` | `const d = useDeferredValue(v, initial?)` | A lagging copy of a value for a slow child | Needs `memo` on the slow child; not a debounce (no timer, no fewer requests); `initialValue` added in 19 [§7.11](../07-custom-hooks-and-hook-rules/README.md#711-usedeferredvalue--let-part-of-the-ui-lag-behind) | 18.0 |
| `useId` | `const id = useId()` | IDs for `htmlFor`/`aria-*` that match between server and client | One call + suffixes (`${id}-hint`); never for list keys; format is not stable API [§7.8](../07-custom-hooks-and-hook-rules/README.md#78-useid--ids-for-accessibility) | 18.0 |
| `useSyncExternalStore` | `useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot?)` | Subscribe to data outside React without tearing | `getSnapshot` must return the **same** value if nothing changed (else "should be cached" + infinite loop); keep `subscribe` stable [§7.9](../07-custom-hooks-and-hook-rules/README.md#79-usesyncexternalstore--subscribing-to-outside-data) | 18.0 |
| `useDebugValue` | `useDebugValue(label)` | Label a custom Hook in React DevTools | Library Hooks only [§7.3](../07-custom-hooks-and-hook-rules/README.md#73-custom-hooks--reusing-stateful-logic) | 16.8 |
| `useActionState` | `const [state, action, isPending] = useActionState(fn, initial, permalink?)` | Result of the last Action (errors, success) + pending flag | `fn(prevState, payload)` — previous state comes **first**; calls are queued; manual dispatch must be inside `startTransition` [§4.5](../04-forms-and-actions/README.md#45-useactionstate--the-result-of-the-last-submission) | 19.0 |
| `useFormStatus` (`react-dom`) | `const { pending, data, method, action } = useFormStatus()` | Status of the **parent** `<form>` from any child | Always `pending: false` in the component that renders the `<form>`; import from `react-dom` [§4.6](../04-forms-and-actions/README.md#46-useformstatus--status-of-the-parent-form) | 19.0 |
| `useOptimistic` | `const [opt, addOpt] = useOptimistic(value, reducer?)` | Show the expected result while an Action runs | Call `addOpt` inside an Action/transition; rolls back automatically when the Action ends; reducer must be pure [§4.7](../04-forms-and-actions/README.md#47-useoptimistic--instant-feedback-automatic-rollback) | 19.0 |

**Related non-Hook APIs:** `memo` [§10.4](../10-performance/README.md#104-memo--skipping-a-childs-re-render), `lazy` + `<Suspense>` [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense), `startTransition` (standalone) [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates), `createContext` / `<Ctx value>` [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props), `createPortal` [§14.8](../14-patterns-and-architecture/README.md#148-portals), `flushSync` [§3.4](../03-state-and-events/README.md#34-batching), `<Profiler>` [§10.3](../10-performance/README.md#103-measure-first-devtools-profiler-and-profiler), `<StrictMode>` [§5.4](../05-effects-and-refs/README.md#54-strictmodes-double-run-development-only), `<Activity>` (19.2) [§5.4](../05-effects-and-refs/README.md#54-strictmodes-double-run-development-only), `cache()` for Server Components [§13.7](../13-server-rendering-and-frameworks/README.md#137-react-server-components). Library Hooks (React Router, TanStack Query, Redux, Zustand) are in §16.9 and §16.10.

---

## 16.4 State update rules

| Rule | Example | Result |
|---|---|---|
| State is a **snapshot** per render | `setN(n + 1); console.log(n)` | Logs the old value; a timeout created in that render also sees the old value forever [§3.3](../03-state-and-events/README.md#33-state-as-a-snapshot) |
| Plain value = **replace** | three × `setN(n + 1)` from 0 | **1** |
| Updater = **compute from the queued value** | three × `setN(m => m + 1)` from 0 | **3** |
| Mixed queue, processed in order | `setN(n + 5); setN(m => m + 1)` from 0 | **6** |
| Last replace wins | `setN(m => m + 1); setN(n + 5)` from 0 | **5** [§3.5](../03-state-and-events/README.md#35-updater-functions--queueing-updates) |
| **Batching** | several setters in one event — also inside `setTimeout`, promises, native listeners (React 18+) | **one** re-render [§3.4](../03-state-and-events/README.md#34-batching) |
| Opt out of batching | `flushSync(() => setItems(next))` | DOM updated before the next line (e.g. to scroll to a new item) |
| Same value | `setN(n)` | Bail-out: no re-render of that subtree (React may call the component once more and discard it) [§3.2](../03-state-and-events/README.md#32-state-a-components-memory--usestate), [§10.1](../10-performance/README.md#101-the-render-cycle-trigger--render--commit) |
| Never mutate | `p.name = 'x'; setP(p)` | Same reference → **no re-render**, stale screen. Use `setP({ ...p, name: 'x' })` [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably) |
| State lives at a **position** | same component type at the same place | State kept, even with different props |
| … | different type at that place, or a **different `key`** | State destroyed, fresh instance [§3.10](../03-state-and-events/README.md#310-state-is-tied-to-a-position-in-the-tree) |

**Immutable update recipes** [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably)

| Goal | Use (returns new) | Avoid (mutates) |
|---|---|---|
| Change a field | `{ ...obj, field: v }`; nested: copy every level `{ ...u, address: { ...u.address, zip } }` | `obj.field = v` |
| Add | `[...arr, x]`, `[x, ...arr]` | `push`, `unshift` |
| Insert at `i` | `arr.toSpliced(i, 0, x)` or `[...arr.slice(0, i), x, ...arr.slice(i)]` | `splice` |
| Remove | `arr.filter(it => it.id !== id)` | `splice`, `pop`, `shift` |
| Update one | `arr.map(it => it.id === id ? { ...it, done: true } : it)`, `arr.with(i, x)` | `arr[i] = x` |
| Sort / reverse | `toSorted()`, `toReversed()`, or `[...arr].sort()` | `sort()`, `reverse()` in place |
| Deep changes | Immer: `produce(draft => { draft.a.b = 1 })`, `useImmer` | hand-copying 4 levels |

**Choosing what to store** [§3.8](../03-state-and-events/README.md#38-derived-state--choosing-the-minimal-state), [§6.1](../06-state-management/README.md#61-kinds-of-state--and-where-each-belongs)
- Keep the **minimal** state; compute the rest in render (full names, totals, filtered lists, "is valid").
- Store **IDs**, not copies of objects; don't mirror props into state (`useState(prop)` only reads the first value — name it `initialX` if that's the intent).
- One `status: 'idle' | 'sending' | 'sent'` instead of contradictory booleans.
- State as **low as possible, as high as necessary**: lift shared state to the closest common parent [§3.7](../03-state-and-events/README.md#37-lifting-state-up).
- `useState` for independent values; `useReducer` when many handlers change the same state in related ways [§6.2](../06-state-management/README.md#62-usereducer--consolidate-update-logic).

---

## 16.5 The render and commit lifecycle

Every update goes **trigger → render → commit**. "Re-render" means *React called your function again* — the DOM changes only where the output differs. [§10.1](../10-performance/README.md#101-the-render-cycle-trigger--render--commit), [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)

```mermaid
flowchart TD
    T["1 Trigger: first root.render() or a state setter"] --> Q["Updates queued and batched"]
    Q --> R["2 Render: call components - twice in dev StrictMode"]
    R --> D["Reconcile: diff with previous tree by type, position, key"]
    D --> C1["3 Commit: apply only the DOM differences, set refs"]
    C1 --> C2["useInsertionEffect - before any layout effect"]
    C2 --> C3["Layout effects: cleanups then setups, child first"]
    C3 --> P["Browser paints"]
    P --> E1["useEffect: all cleanups, then all setups, child first"]
    R -.->|"throws"| EB["Nearest error boundary fallback"]
    R -.->|"suspends"| SU["Nearest Suspense fallback, or old UI in a transition"]
```

What the check program observed for a `Parent` → `Child` pair when a prop changes from 1 to 2 (§16.5 check):

| Step | Order |
|---|---|
| Mount | render parent → render child → child **layout** setup → child **effect** setup → parent **effect** setup |
| Update | render parent → render child → child layout cleanup (v=1) → child layout setup (v=2) → child effect cleanup (v=1) → parent effect cleanup (v=1) → child effect setup (v=2) → parent effect setup (v=2) |
| Unmount | every remaining cleanup runs, with the values of the render that created it |

| Phase | Must be | What happens there |
|---|---|---|
| Render | **Pure** — may run twice, be paused (concurrent rendering) or thrown away | Call components, compute JSX and derived values |
| Commit | Runs once per committed update | DOM mutations, refs set, layout effects |
| After paint | — | `useEffect` setups (subscriptions, fetching, logging) |

**What triggers a re-render** — own state change; **parent re-rendered** (all children by default, props are *not* compared unless `memo`); a **context** it reads changed; a subscribed **external store** changed. Not: mutating props, changing `ref.current`, changing a plain variable. [§10.2](../10-performance/README.md#102-when-components-re-render)

---

## 16.6 Effects: decision guide

**Ask "why does this code run?"** [§5.1](../05-effects-and-refs/README.md#51-where-side-effects-belong), [§5.6](../05-effects-and-refs/README.md#56-you-might-not-need-an-effect)

Go down the list and stop at the first "yes":

| # | Question | If yes |
|---|---|---|
| 1 | Can it be computed from props or state? | Compute it during render (`useMemo` only if expensive) |
| 2 | Is it caused by a specific user action (click, submit)? | Event handler or form Action |
| 3 | Must **all** state reset when a prop changes? | Give the component a `key` |
| 4 | Is it reading data that lives outside React and changes on its own? | `useSyncExternalStore` |
| 5 | Is it fetching server data? | TanStack Query or a router loader — or an Effect **with cleanup** |
| 6 | Must it stay in sync with an external system while the component is on screen? | `useEffect` with a cleanup |
| 7 | None of the above | Probably no Effect at all |

**You might not need an Effect** [§5.6](../05-effects-and-refs/README.md#56-you-might-not-need-an-effect)

| You want to… | Instead of an Effect |
|---|---|
| Compute a value from props/state | A variable in render (an Effect costs an extra render and a stale frame) |
| Cache an expensive calculation | `useMemo` |
| Reset all state when `userId` changes | `<Profile key={userId} />` |
| Adjust some state when a prop changes | Derive it (store an ID, look the object up) |
| Run logic because the user clicked/submitted | The event handler / form Action |
| Chain updates (A → B → C) | Compute all next state in one handler |
| Notify the parent of a change | Call its callback in the same handler, or lift the state |
| Initialise the app once | Module-level code or a `didInit` flag |
| Subscribe to an external store | `useSyncExternalStore` |

**What legitimately needs an Effect:** sockets and connections, browser events (`resize`, `online`, key shortcuts), timers while visible, non-React widgets (maps, video, `dialog.showModal()`), `document.title`, logging an impression because something was shown, fetching (with cleanup).

**Dependencies** [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth)

| Situation | Fix |
|---|---|
| The linter wants a value you don't want to react to | Don't silence it — split the logic: reactive part in the Effect, non-reactive part in `useEffectEvent` |
| Object/function created in render is a dependency | New every render → Effect runs every render. Create it **inside** the Effect or outside the component; depend on primitives |
| Interval reads state | `setCount(c => c + 1)` so `count` isn't a dependency |
| Two unrelated jobs in one Effect | Two Effects |
| Can be left out | setters, `dispatch`, refs, module constants |

**Cleanup pairs** [§5.3](../05-effects-and-refs/README.md#53-cleanup--undo-what-setup-did): `connect`/`disconnect` · `setInterval`/`clearInterval` · `addEventListener`/`removeEventListener` (same function reference) · `subscribe`/`unsubscribe` · `new AbortController` + `fetch(url, { signal })`/`controller.abort()` · `observer.observe`/`observer.disconnect` · `showModal`/`close`.

**StrictMode (development only)** — you opt in by wrapping the app in `<StrictMode>`, which Vite and most templates do — calls component bodies, state initialisers, updaters and reducers **twice**, runs every Effect **setup → cleanup → setup** on mount, and re-runs ref callbacks. Production runs once. The fix for a visible double is a correct cleanup, never a "has run" flag. [§5.4](../05-effects-and-refs/README.md#54-strictmodes-double-run-development-only), [§2.5](../02-components-and-props/README.md#25-pure-components)

**Fetching in an Effect** [§5.7](../05-effects-and-refs/README.md#57-fetching-data-in-effects-and-race-conditions), [§7.6](../07-custom-hooks-and-hook-rules/README.md#76-usefetch--loading-error-success-and-no-races): loading/error/success in one `status` value; `ignore` flag or `AbortController` in cleanup (else the slower old response wins); check `res.ok` (`fetch` doesn't reject on 404); ignore `AbortError`. For real apps prefer TanStack Query or loaders (cache, dedupe, retries, no waterfalls).

---

## 16.7 Refs

| | `useState` | `useRef` |
|---|---|---|
| Returns | `[value, setValue]` | `{ current }` — the same object every render |
| Changing it | setter → **re-render** | `ref.current = x` → **no re-render**, visible immediately |
| Read during render? | Yes (a snapshot) | No — except lazy init `if (r.current === null) r.current = new Engine()` |
| Use for | anything shown on screen | timer IDs, DOM nodes, `AbortController`, third-party instances, "in flight" flags |

[§5.8](../05-effects-and-refs/README.md#58-useref-for-values)

**DOM refs** [§5.9](../05-effects-and-refs/README.md#59-refs-to-dom-nodes), [§5.10](../05-effects-and-refs/README.md#510-passing-refs-to-your-own-components)
- `const inputRef = useRef(null)` + `<input ref={inputRef} />` → `inputRef.current` is the node **after commit** (`null` during the first render), `null` again after removal.
- Good uses: focus, scroll into view, measure (`getBoundingClientRect` in `useLayoutEffect`), media `play()`, `dialog.showModal()`, non-React libraries. Not for changing text/classes React renders.
- Lists: one ref holding a `Map` + a **ref callback** per item. In React 19 a ref callback may **return a cleanup**; use a block body (`ref={n => { x = n; }}`), because an implicit-return arrow now looks like a cleanup to TypeScript.
- To scroll to an item you just added: `flushSync(() => setItems(next))`, then scroll.

**Refs to your own components**

| React 19 | Legacy |
|---|---|
| `function Field({ ref, ...p }) { return <input ref={ref} {...p} /> }` | `forwardRef((props, ref) => …)` — still works, planned for deprecation |
| `useImperativeHandle(ref, () => ({ focus, clear }))` to expose a limited API | same |
| `<Fragment ref={r}>` (19.3) → `FragmentInstance` with `focus()`, `addEventListener`, `observeUsing` | wrapper `<div>` just to hold a ref |

---

## 16.8 Forms and Actions (React 19)

**Two ways to own a field** [§4.2](../04-forms-and-actions/README.md#42-controlled-vs-uncontrolled-inputs), [§3.9](../03-state-and-events/README.md#39-controlled-inputs-basics)

| | Controlled | Uncontrolled |
|---|---|---|
| Value lives in | React state: `value={v}` + `onChange` (`checked` for boxes/radios) | The DOM: `defaultValue` / `defaultChecked` |
| Read it | From state | `FormData` on submit, or a ref |
| Re-renders | Every keystroke | None while typing |
| Good for | Live formatting, limits, dependent fields, counters | Most "fill in and submit" forms; pairs with form Actions |
| Traps | `value` without `onChange` → read-only + warning; `useState()` (undefined) → "uncontrolled to controlled" warning — start with `''` | Changing `defaultValue` later does nothing (remount with `key`); fields without `name` are skipped |

`<input type="file">` is always uncontrolled. React's `onChange` fires on every keystroke (like the DOM `input` event).

**`FormData` rules** [§4.1](../04-forms-and-actions/README.md#41-how-a-form-submission-works): only fields with a `name` are sent · every value is a **string** (`Number(fd.get('age'))`) · an **unchecked** checkbox is **absent** · a checked box without `value` sends `"on"` · `getAll('topic')` for repeated names · `Object.fromEntries(fd)` keeps only the last of repeated names · disabled fields are skipped.

**The React 19 form toolkit**

| API | Shape | Remember |
|---|---|---|
| Form Action | `<form action={fn}>` → `fn(formData)` | React prevents navigation, runs `fn` in a Transition (may be async), **resets uncontrolled fields after success**; throwing goes to the error boundary — return expected errors instead [§4.4](../04-forms-and-actions/README.md#44-form-actions--form-actionfn) |
| `formAction` | `<button formAction={saveDraft}>` | Per-button override of the form's action [§4.4](../04-forms-and-actions/README.md#44-form-actions--form-actionfn) |
| `useActionState` | `const [state, action, isPending] = useActionState(async (prev, formData) => next, initial)` | Return errors **and** submitted values (use them as `defaultValue`s so a failed submit doesn't wipe the form); calls queue sequentially [§4.5](../04-forms-and-actions/README.md#45-useactionstate--the-result-of-the-last-submission) |
| `useFormStatus` | `const { pending, data } = useFormStatus()` (from `react-dom`) | Only in a **child** of the `<form>` — perfect for a reusable Submit button [§4.6](../04-forms-and-actions/README.md#46-useformstatus--status-of-the-parent-form) |
| `useOptimistic` | `const [opt, add] = useOptimistic(real, (cur, x) => …)` | Call `add` inside the Action; update the real state on success; rollback is automatic [§4.7](../04-forms-and-actions/README.md#47-useoptimistic--instant-feedback-automatic-rollback) |
| `requestFormReset(form)` | from `react-dom`, inside a Transition | Reset by hand [§4.4](../04-forms-and-actions/README.md#44-form-actions--form-actionfn) |
| Server Function | `'use server'` async function used as `action` | Works before hydration (progressive enhancement); arguments must be serializable; authenticate and validate inside [§4.8](../04-forms-and-actions/README.md#48-server-functions-and-progressive-enhancement-overview), [§13.8](../13-server-rendering-and-frameworks/README.md#138-directives-use-client-and-use-server) |

**Validation** [§4.3](../04-forms-and-actions/README.md#43-validation): three layers — HTML constraints (`required`, `type="email"`, `minLength`, `pattern`), a **pure `validate(values)`** computed during render, and **server validation (mandatory)**. Show errors on blur or after the first submit, then live while fixing; `aria-invalid="true"` + `aria-describedby` to the message; focus the first invalid field; `noValidate` hands the UI to you while `validity` keeps working. Big forms: React Hook Form + Zod (`zodResolver`), with the same schema on the server [§4.9](../04-forms-and-actions/README.md#49-form-libraries--react-hook-form-and-zod-overview).

---

## 16.9 Context and choosing state management

**Context in three steps** [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props): `const Ctx = createContext(default)` → `<Ctx value={v}>…</Ctx>` (React 19; `<Ctx.Provider>` still works) → `useContext(Ctx)` or `use(Ctx)` (conditional allowed). Nearest provider wins; the default applies **only when there is no provider** (a provider with `value={undefined}` gives `undefined`).

**Context performance** [§6.4](../06-state-management/README.md#64-context-performance-caveats), [§10.10](../10-performance/README.md#1010-common-performance-bugs)

| Problem | Fix |
|---|---|
| `value={{ user, setUser }}` is a new object every render → every consumer re-renders, `memo` can't stop it | `useMemo` the value (`useCallback` for functions) — or let the React Compiler do it |
| Consumers that need only actions re-render when data changes | Split into a state context and an actions/dispatch context |
| One big context with unrelated values | One context per concern |
| High-frequency values (typing, mouse, timers) | Keep local, or use a store with selectors |

**Reducer + context store** [§6.5](../06-state-management/README.md#65-reducer--context--a-store-with-only-react): `useReducer` in a provider component, two contexts (state, dispatch), and `useTasks()` / `useTasksDispatch()` Hooks that **throw** outside the provider (use `null` as the default so the check is possible).

**Where does this state live?** [§6.1](../06-state-management/README.md#61-kinds-of-state--and-where-each-belongs), [§6.10](../06-state-management/README.md#610-choosing--comparison-and-decision-flow)

| Kind of state | Examples | Home |
|---|---|---|
| Local UI | open/closed, current tab, input text | `useState` in the component |
| Complex local | wizard, editor | `useReducer` |
| Shared by nearby components | selected item | Lift to the closest common parent |
| App-wide, changes rarely | theme, locale, current user | Context |
| App-wide, changes often, read by many | cart, editor document, dashboard filters | **Zustand** (small, no provider) or **Redux Toolkit** (structure, DevTools, middleware) |
| Server data | lists, profiles, search results | **TanStack Query** / RTK Query / router loaders / Server Components |
| Shareable / bookmarkable | filters, sort, page, selected ID | The **URL** (`useSearchParams`, route params) |
| Form fields | values, errors, pending | Form Actions + `useActionState`, or React Hook Form |
| Derived | totals, filtered lists | **Not state** — compute in render |

**Library essentials** [§6.7](../06-state-management/README.md#67-redux-toolkit), [§6.8](../06-state-management/README.md#68-zustand)

| Redux Toolkit | Zustand |
|---|---|
| `createSlice({ name, initialState, reducers })` → reducer + action creators; reducers may "mutate" (Immer) — mutate the draft **or** return new state, not both | `const useStore = create((set, get) => ({ n: 0, inc: () => set(s => ({ n: s.n + 1 })) }))` — `set` merges one level |
| `configureStore` adds thunks, DevTools, mutation/serializability checks | No provider; `useStore.getState()/setState()/subscribe()` work outside React |
| `useSelector(sel)` re-renders only when the selected value changes; `useDispatch()` | `useStore(s => s.n)`; several values → wrap in `useShallow` |
| Selector returning a new array/object → re-render on every dispatch; memoise with `createSelector` | Selector returning a new object without `useShallow` → "Maximum update depth exceeded" loop |
| Async: `createAsyncThunk` (pending/fulfilled/rejected) or **RTK Query** | Middleware: `persist`, `devtools`, `immer` |

---

## 16.10 Routing (React Router v8) and TanStack Query

**React Router v8** — one package (`npm i react-router`; `react-router-dom` is gone), requires React 19.2.7+ and Node 22.22+, ESM-only, middleware on by default. `RouterProvider` comes from `react-router/dom`. [§9.2](../09-routing-and-data-fetching/README.md#92-react-router-v8-and-its-three-modes)

| Mode | You write | You get |
|---|---|---|
| Declarative | `<BrowserRouter><Routes><Route path element/></Routes></BrowserRouter>` | Matching, links, params |
| Data | `createBrowserRouter([{ path, Component, loader, action, ErrorBoundary, children }])` + `<RouterProvider>` | + loaders, actions, pending UI, route error boundaries, fetchers, middleware |
| Framework | `app/routes.ts` + route modules + Vite plugin | + SSR / SPA / prerender, per-route code splitting, generated types [§13.10](../13-server-rendering-and-frameworks/README.md#1310-react-router-framework-mode) |

| Task | API [§9.3](../09-routing-and-data-fetching/README.md#93-routes-links-and-url-parameters-declarative-mode)–[§9.7](../09-routing-and-data-fetching/README.md#97-middleware-lazy-routes-and-framework-mode) |
|---|---|
| Dynamic segment | `path="/products/:id"` → `useParams()` → `{ id: '42' }` (**always strings**) |
| Ranking | Static segments beat `:params`, which beat `*` — order in the file doesn't matter; `path="*"` = 404 |
| Links | `<Link to>`; `<NavLink>` gets `isActive` (`end` for exact); relative links resolve against the **route**, `..` = parent route |
| Query string | `const [params, setParams] = useSearchParams()` — shareable UI state |
| Navigate in code | `const navigate = useNavigate(); navigate('/x')`, `navigate(-1)`, `{ replace: true }`; or render `<Navigate to>` |
| Layouts | Parent renders `<Outlet />`; `index` route for the default child; pathless route for shared UI; `<Outlet context>` + `useOutletContext()` |
| Load before render | `loader({ params, request, context })` → `useLoaderData()`; loaders of all matched routes run **in parallel**; `useNavigation().state` = `'idle' \| 'loading' \| 'submitting'` |
| Errors | `throw data('Not found', { status: 404 })` → route `ErrorBoundary`, check `isRouteErrorResponse(error)` |
| Writes | `<Form method="post">` → route `action` → `return data({ errors }, { status: 400 })` (`useActionData`) or `redirect('/list')`; loaders **revalidate** automatically |
| Without navigating | `useFetcher()` — `fetcher.Form`, `fetcher.state`, `fetcher.data` |
| Guards, logging | `middleware: [fn]` with `await next()`; `createContext()` + `context.get/set` (UX only — the server still authorises) |
| Code splitting | `{ path, lazy: () => import('./route.jsx') }` |
| Tests | `MemoryRouter initialEntries` / `createMemoryRouter` |

**TanStack Query essentials** [§6.9](../06-state-management/README.md#69-tanstack-query--server-state), [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics)–[§9.10](../09-routing-and-data-fetching/README.md#910-mutations-invalidation-and-optimistic-updates)

| Concept | Remember |
|---|---|
| Setup | One `QueryClient` created **once** (module scope or `useState(() => new QueryClient())`), `<QueryClientProvider client>` |
| Read | `useQuery({ queryKey: ['product', id], queryFn })` — every input of `queryFn` goes in the key; `queryFn` must **throw** on failure (check `res.ok`) |
| Defaults | `staleTime` **0** (stale at once → background refetch on mount, window focus, reconnect), `gcTime` **5 min**, **3 retries** in the browser with backoff |
| Flags | `isPending` = no data yet · `isFetching` = request in flight (incl. background) · `isLoading` = both · `isError`, `error`, `refetch()` |
| Dedupe & cache | Same key at the same time → one request; remount within `staleTime` → no request |
| Keys | Arrays, compared by value, hierarchical: invalidating `['products']` also hits `['products', 7]` |
| Reuse | `queryOptions({ queryKey, queryFn })` shared by `useQuery`, `useSuspenseQuery`, `ensureQueryData`, `prefetchQuery` |
| Pagination / dependent | `placeholderData: keepPreviousData` (`isPlaceholderData`) · `enabled: !!id` · `useInfiniteQuery` for "load more" |
| Writes | `useMutation({ mutationFn, onSuccess: () => qc.invalidateQueries({ queryKey }) })` |
| Optimistic | `onMutate`: `cancelQueries` → snapshot → `setQueryData` → return rollback · `onError`: restore · `onSettled`: invalidate |
| With a router | Loader calls `queryClient.ensureQueryData(opts)` (critical) or `prefetchQuery` (non-critical); component reads the same `opts` [§9.13](../09-routing-and-data-fetching/README.md#913-putting-it-together--loaders--tanstack-query) |
| Tests | New `QueryClient` per test with `retry: false` [§12.6](../12-testing-react/README.md#126-mocking-fetch-and-when-to-use-msw) |

**Suspense and `use()`** [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use): `<Suspense fallback>` shows a placeholder while `lazy` code, `use(promise)` or Suspense-enabled data (`useSuspenseQuery`, loaders, async Server Components) isn't ready — data fetched in `useEffect` does **not** suspend. A **transition** keeps already-visible content instead of flashing the fallback. Pair each important boundary with an error boundary. Loading UX: skeletons for first loads, old content + progress bar for navigation, nothing for fast refreshes, inline errors for validation, Retry buttons for failures [§9.12](../09-routing-and-data-fetching/README.md#912-loading-and-error-states-that-users-like).

---

## 16.11 Performance: checklist, memo and the React Compiler

**Checklist — in this order, stop when fast enough** [§10.11](../10-performance/README.md#1011-a-performance-checklist)
1. Reproduce with a **production build**, CPU throttled, realistic data.
2. Find the slow interaction: React DevTools **Profiler** (enable "why did this render"), "Highlight updates", Chrome Performance panel with React tracks (19.2+).
3. Slow **load**: bundle analyser → `lazy` routes and heavy widgets, images, SSR/SSG.
4. Slow **interaction**, too much re-renders → **structure first**: colocate state, pass JSX as `children`, stable keys, split/memoise context; then `memo` + stable props, or the React Compiler.
5. One slow component → `useMemo` the calculation (≥ ~1 ms) or fix the algorithm.
6. Too many DOM nodes → virtualize (TanStack Virtual, react-window) or paginate.
7. Typing lags behind results → `useDeferredValue` / `useTransition` with a `memo`'d slow child.
8. Measure again; keep only changes that made a measurable difference.

**`memo` rules** [§10.4](../10-performance/README.md#104-memo--skipping-a-childs-re-render), [§10.5](../10-performance/README.md#105-usecallback-and-usememo-for-stable-props)
- `memo(C)` skips a re-render **caused by the parent** when every prop is `Object.is`-equal. It does not block the component's own state changes or context changes.
- Inline objects, arrays, functions and JSX (`style={{…}}`, `onAdd={() => …}`, `children`) are new every render → `memo` does nothing (V2 in §10.4). Stabilise with `useCallback`/`useMemo`, module constants, or primitives.
- Don't bother for cheap components or props that change anyway; a custom `arePropsEqual` that ignores a callback causes stale closures.
- Free wins without `memo`: state moved down, and **elements passed as `children` from above** keep their identity (the check in §16.11 shows `Shell` re-rendering with 0 child renders).

**The four common performance bugs** [§10.10](../10-performance/README.md#1010-common-performance-bugs): component declared inside a component (remount every render) · unstable keys · a new context object each render · state owned too high (every keystroke re-renders the page). Also: Effects that set state every render, fetch waterfalls, huge images/libraries, development builds in production.

**React Compiler** [§10.7](../10-performance/README.md#107-react-compiler)
| Fact | Detail |
|---|---|
| What | Build-time Babel plugin (`babel-plugin-react-compiler`); v1.0 stable 7 October 2025; best on React 19 (17/18 need `react-compiler-runtime`) |
| Does | Memoizes values, callbacks and JSX per component with a cache (`useMemoCache` from `react/compiler-runtime`) — `memo`/`useMemo`/`useCallback` become mostly unnecessary |
| Needs | The Rules of React: pure render, no mutation of props/state, no `ref.current` during render — it **skips** components that break them |
| Verify | "Memo ✨" badge in React DevTools; lint rules ship in `eslint-plugin-react-hooks` |
| Escape hatches | `"use no memo"` directive; keep `useMemo`/`useCallback` when an Effect dependency's identity must be guaranteed |
| Doesn't fix | Slow algorithms, huge DOM trees, big bundles |

**Code splitting** [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense): `const Chart = lazy(() => import('./Chart.jsx'))` at **module level**, rendered inside `<Suspense fallback>` with an error boundary for failed chunks; `lazy` needs a **default export** (map named ones: `.then(m => ({ default: m.Chart }))`); preload on hover/focus by calling the same `import()`.

**Virtualization math** (fixed rows) [§10.9](../10-performance/README.md#109-list-virtualization): `first = floor(scrollTop / rowHeight) − overscan`, `count = ceil(height / rowHeight) + 2 × overscan`, absolutely positioned rows inside a spacer `items.length × rowHeight` tall — 300 px / 30 px rows / overscan 3 → **16 DOM rows** for 10,000 items.

---

## 16.12 Styling

| Approach | Write | Scoping | Runtime | Pick when [§8.10](../08-styling/README.md#810-choosing-an-approach) |
|---|---|---|---|---|
| Plain CSS | `import './Card.css'`, `className="card"` | **None** — imports are global; BEM or `@layer` | none | Content sites, small apps |
| Inline `style` | `` style={{ width: `${p}%` }} `` | Per element | tiny | Truly dynamic values, ideally setting a CSS variable (`style={{ '--progress': … }}`); no `:hover`/media queries |
| CSS Modules | `Card.module.css`, `className={styles.card}` | Automatic per file (`composes`, `:global`) | none | Teams that like writing CSS |
| Tailwind v4 | `className="p-4 rounded-xl hover:bg-blue-700"` | Nothing to scope | none | Fast iteration; configure tokens with `@theme` |
| Runtime CSS-in-JS | `` styled.button`…` `` (styled-components, Emotion) | Automatic | **yes** | Existing codebases; styled-components is in maintenance mode (March 2025); awkward with Server Components |
| Zero-runtime CSS-in-JS | vanilla-extract, StyleX, Panda | Automatic | none | Typed, colocated styles without the runtime |

**Rules worth memorising** [§8.3](../08-styling/README.md#83-inline-styles--the-style-prop)–[§8.8](../08-styling/README.md#88-theming-and-dark-mode)
- `style` numbers get `px` except unitless properties; vendor prefixes are capitalised (`WebkitLineClamp`, but `msTransform`); inline styles beat stylesheets (only `!important` overrides them).
- Build class strings with `clsx`; always accept a `className` prop; use variant maps, not ternary chains. Tailwind: write **complete** class names (`bg-${color}-500` is never generated); resolve conflicts with `tailwind-merge`.
- Theming: tokens as CSS custom properties on `:root`, overridden under `[data-theme="dark"]`; a provider writes the attribute; a blocking `<head>` script prevents the flash of the wrong theme; `color-scheme` for native controls; CSS recolours without re-rendering components.
- React 19 hoists `<link rel="stylesheet" precedence>` and `<style href precedence>` into `<head>` and de-duplicates them.
- Component libraries: styled kits (MUI, Mantine) for speed; headless (Radix, React Aria, Headless UI) for custom design with accessibility; copy-in (shadcn/ui) to own the code [§8.9](../08-styling/README.md#89-component-libraries).

---

## 16.13 TypeScript + React types

Bundlers (Oxc, esbuild, SWC) only **strip** types; `tsc --noEmit` **checks** them — run it in CI. Use `strict: true` and `"jsx": "react-jsx"`. [§11.1](../11-typescript-with-react/README.md#111-setup-and-how-type-checking-fits-in)

| Need | Type / pattern |
|---|---|
| Props | `type Props = { label: string; count?: number; tone?: 'info' \| 'error' }; function Badge({ label, count = 0 }: Props)` [§11.2](../11-typescript-with-react/README.md#112-typing-props) |
| Callback prop | `onSelect?: (id: string) => void`, called as `onSelect?.(id)` |
| Children, slots | `children: ReactNode` (anything renderable) · `PropsWithChildren<P>` · `ReactElement` only when it must be one element [§11.3](../11-typescript-with-react/README.md#113-typing-children-and-extending-native-elements) |
| Native element props | `ComponentProps<'button'> & { variant?: 'primary' \| 'ghost' }` (includes `ref`) · `ComponentPropsWithoutRef<'input'>` · `ComponentProps<typeof MyComp>` |
| Change event | `ChangeEvent<HTMLInputElement>` / `HTMLSelectElement` / `HTMLTextAreaElement` — or `ChangeEventHandler<…>` [§11.4](../11-typescript-with-react/README.md#114-typing-events) |
| Submit | `SubmitEvent<HTMLFormElement>` in recent `@types/react` 19 (`FormEvent` still compiles, now deprecated) |
| Click, keys, focus | `MouseEvent<HTMLButtonElement>`, `KeyboardEvent<HTMLInputElement>`, `FocusEvent<…>` — imported from `react`, not the DOM globals |
| Inline handler | No annotation — contextually typed |
| State | Inferred from the initial value; add `<T>` for `useState<User \| null>(null)`, `useState<string[]>([])` (else `never[]`), literal unions [§11.5](../11-typescript-with-react/README.md#115-typing-state-and-reducers) |
| Setter as a prop | `Dispatch<SetStateAction<T>>` |
| Reducer actions | Discriminated union `{ type: 'add'; sku: string } \| { type: 'clear' }` + `const _: never = action` in `default` [§11.5](../11-typescript-with-react/README.md#115-typing-state-and-reducers) |
| DOM ref | `useRef<HTMLInputElement>(null)` → `current: HTMLInputElement \| null`; React 19 types **require** an argument [§11.6](../11-typescript-with-react/README.md#116-typing-refs) |
| `ref` prop / handle | `ref?: Ref<HTMLInputElement>`; custom handle `Ref<Handle>` + `useImperativeHandle` |
| Tuple from a Hook | `return [on, toggle] as const` — or return an object [§11.7](../11-typescript-with-react/README.md#117-typing-custom-hooks-and-context) |
| Context with no default | `createContext<Auth \| null>(null)` + `useAuth(): Auth` that throws outside the provider |
| Generic component | `function List<T>({ items, renderItem }: { items: T[]; renderItem: (t: T) => ReactNode })`; `<T,>` in arrow functions; constraints with `extends`; field names with `keyof T & string` [§11.8](../11-typescript-with-react/README.md#118-generic-components) |
| Mutually exclusive props | Union per case; `prop?: never` forbids a prop; narrow `props` before destructuring [§11.9](../11-typescript-with-react/README.md#119-discriminated-unions-for-props) |
| Untrusted data | `unknown` + narrowing, or a Zod schema — `as T` is not validation [§11.7](../11-typescript-with-react/README.md#117-typing-custom-hooks-and-context) |

Read type errors bottom-up: *"Type A is not assignable to type B"*, then the last indented line names the property that differs [§11.10](../11-typescript-with-react/README.md#1110-reading-type-errors-and-common-mistakes).

---

## 16.14 Testing: query priority and user-event

**Philosophy** [§12.1](../12-testing-react/README.md#121-what-to-test-and-the-testing-library-philosophy): render → find like a user → act like a user → assert on what the user sees. Don't assert on state, props, Hook calls or class names.

**Query priority** (Testing Library) [§12.3](../12-testing-react/README.md#123-queries-and-their-priority)
1. `getByRole(role, { name })` — almost everything (`button`, `link`, `heading` + `level`, `textbox`, `checkbox`, `combobox`, `dialog`, `alert`, `status`, `list`/`listitem`…)
2. `getByLabelText` — form fields
3. `getByPlaceholderText` — only if there's truly no label
4. `getByText` — non-interactive text
5. `getByDisplayValue` — a field by its current value
6. `getByAltText`, `getByTitle`
7. `getByTestId` — last resort

| Variant | 0 matches | 1 | 2+ | Waits? | Use to… |
|---|---|---|---|---|---|
| `getBy…` | throws | element | throws | no | find what must be there now |
| `queryBy…` | `null` | element | throws | no | assert absence |
| `findBy…` | rejects | element | rejects | yes (default 1000 ms) | wait for something to appear |
| `getAllBy…` / `queryAllBy…` / `findAllBy…` | throws / `[]` / rejects | array | array | as above | several |

**user-event** [§12.4](../12-testing-react/README.md#124-user-event--interacting-like-a-user): `const user = userEvent.setup();` then **`await`** `user.click`, `user.type(el, 'hi{Enter}')`, `user.keyboard('{Escape}')`, `user.tab()`, `user.selectOptions`, `user.clear`, `user.upload`, `user.hover`. It produces the full sequence (focus, keydown, input, keyup…), respects `disabled` and focus order; `fireEvent` dispatches exactly one event (keep it for `scroll` and custom events).

**Async** [§12.5](../12-testing-react/README.md#125-async-tests): `await screen.findByRole(…)` (appear) · `await waitForElementToBeRemoved(() => screen.queryByText('Loading…'))` (disappear) · `await waitFor(() => assertion)` (anything else — assertions only inside) · never `sleep`. Forgetting `await` → passes by accident + `act(...)` warning.

**Everything else** [§12.6](../12-testing-react/README.md#126-mocking-fetch-and-when-to-use-msw)–[§12.9](../12-testing-react/README.md#129-setting-up-vitest-or-jest-in-a-real-project): mock the **network boundary** (MSW, or `mock.method(globalThis, 'fetch')` / `vi.spyOn` returning real `Response`s) and cover success, HTTP error, network error and races · Hooks through a component or `renderHook` + `act` + `result.current` · fake timers for debounce (`advanceTimers` option for user-event) · a `renderWithProviders` with `wrapper` creating a **fresh** store / `QueryClient` per test · Vitest: `environment: 'jsdom'`, `globals: true`, setup file importing `@testing-library/jest-dom/vitest` · jsdom has no layout (sizes are 0; `IntersectionObserver`, `matchMedia` need mocks).

---

## 16.15 Rendering strategies and directives

| Strategy | HTML made where / when | First HTML has | Needs a server | Good for |
|---|---|---|---|---|
| **CSR** (SPA) | Browser, after JS loads | empty `<div id="root">` | No (static hosting) | Dashboards and tools behind a login |
| **SSR** | Server, every request | full content | Yes | Personalised or fast-changing public pages |
| **SSG** | Build machine, once per deploy | full content | No (CDN) | Docs, blogs, marketing |
| **ISR** | Build, then re-built in the background after a window or on demand (framework feature) | full content, stale for at most N s | Yes | Catalogues, news |
| **Streaming SSR** | Server, in chunks per `<Suspense>` boundary | shell + fallbacks, slow parts later | Yes | Fast frame, slow widgets |
| **PPR** | Static shell at build + dynamic holes per request (`prerender` → `postponed` → `resume`) | static shell with placeholders | Yes | Mixed static/per-user pages (Next.js Cache Components) |
| **RSC** | Some components run **only** on the server (build or request) | works with any row above | Yes (or build time) | Data-heavy pages with little interactivity; zero JS for server parts |

[§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg), [§13.1](../13-server-rendering-and-frameworks/README.md#131-rendering-strategies-csr-ssr-ssg-isr-streaming-ppr), [§13.6](../13-server-rendering-and-frameworks/README.md#136-static-generation-and-partial-prerendering)

**Server APIs** [§13.2](../13-server-rendering-and-frameworks/README.md#132-the-server-rendering-apis)

| Function | Package | Waits for Suspense data? | Use |
|---|---|---|---|
| `renderToString` | `react-dom/server` | No — emits the fallback marked `<!--$!-->` (client renders it) | Legacy / simple pages |
| `renderToStaticMarkup` | `react-dom/server` | No; no hydration markers (`<!-- -->`) | Emails, static snippets (can't be hydrated) |
| `renderToPipeableStream` | `react-dom/server` (Node) | Streams later | SSR on Node: status in `onShellReady`, `onAllReady` for crawlers, always `abort()` on a timeout |
| `renderToReadableStream` | `react-dom/server` (Web Streams) | Streams later | Edge, Deno, Bun, Workers |
| `prerenderToNodeStream` / `prerender` | `react-dom/static` | **Yes** | SSG; `prerender` + abort → PPR |
| `resume` / `resumeToPipeableStream` | `react-dom/server` (Web / Node) | Streams | Fill PPR holes per request |
| `resumeAndPrerender` / `resumeAndPrerenderToNodeStream` | `react-dom/static` | **Yes** | Continue a postponed prerender at build time |
| `hydrateRoot(domNode, <App/>)` | `react-dom/client` | — | Adopt server HTML in the browser (never `createRoot` on server HTML) |

**Streaming markers** [§13.3](../13-server-rendering-and-frameworks/README.md#133-streaming-ssr-with-suspense): `<!--$-->` complete boundary · `<!--$?-->` pending (fallback inside) · `<!--$!-->` failed on the server → client renders it · `<template id="B:0">` where content will go · `<div hidden id="S:0">` finished content · `$RC("B:0","S:0")` swap script.

**Hydration** [§13.4](../13-server-rendering-and-frameworks/README.md#134-hydration-end-to-end), [§13.5](../13-server-rendering-and-frameworks/README.md#135-hydration-mismatches): same tree + same data on the client; React **reuses** the DOM and attaches handlers; streaming allows **selective hydration** (the shell is interactive before slow parts arrive; clicked boundaries hydrate first); visible ≠ interactive. Mismatch sources: `window`/`localStorage` checks, `Date.now()`, `Math.random()`, locale/time zone, invalid nesting (`<div>` in `<p>`), refetched data, extensions. Fix: render the server version first and switch after mount (`useEffect` flag, or `useSyncExternalStore` with `getServerSnapshot`); for a whole browser-only component, `use(browser())` (React 19.3, `react-dom`) inside `<Suspense>` — it suspends only on the server, so the client renders it; `useId` for IDs; `suppressHydrationWarning` only for one expected difference (it keeps the **server** text). Hooks run on the server once; **Effects never run on the server**.

**Server vs Client Components** [§13.7](../13-server-rendering-and-frameworks/README.md#137-react-server-components)

| | Server Component | Client Component |
|---|---|---|
| Marked by | Nothing — the default in RSC frameworks | A `'use client'` entry file (and everything it imports) |
| Runs | Server only (build or request) | Server for SSR HTML **and** the browser |
| Code shipped | None | Yes |
| Can | be `async`, `await` a database, use secrets and files | use state, Effects, event handlers, browser APIs, context |
| Can't | use state, Effects, handlers, `createContext`/`useContext`/`use(Ctx)` — but since 19.3 it may **render** a context provider imported from a `'use client'` module | import a Server Component (but can render one passed as `children`) |

**Crossing the boundary** — props from server to client must be serializable: primitives, plain objects/arrays, `Date`, `Map`, `Set`, typed arrays, JSX, **promises** (read with `use`), **Server Functions**. Not: ordinary functions, class instances, null-prototype objects, local symbols.

**Directives** [§13.8](../13-server-rendering-and-frameworks/README.md#138-directives-use-client-and-use-server)

| Directive | Where | Means |
|---|---|---|
| `'use client'` | First statement of a **file** | Start of client code: the module and its imports ship to the browser. Push it to the leaves |
| `'use server'` | Top of a file, or first line of an `async` function | **Server Function**: runs on the server, callable from the client (a public POST endpoint — authenticate, authorise, validate) |
| *(none)* | — | Server Component (there is **no** "use server component" directive) |
| `'use cache'` | Next.js only | Cache a function/component under Cache Components [§13.9](../13-server-rendering-and-frameworks/README.md#139-nextjs-app-router-overview) |
| `'use no memo'` | Component/file | Opt out of the React Compiler [§10.7](../10-performance/README.md#107-react-compiler) |
| `import 'server-only'` | Module | Build fails if a Client Component imports it |

**Frameworks** [§13.9](../13-server-rendering-and-frameworks/README.md#139-nextjs-app-router-overview)–[§13.11](../13-server-rendering-and-frameworks/README.md#1311-choosing-a-framework): **Next.js 16** App Router — folders are routes; `page`, `layout`, `loading`, `error` (client), `not-found`, `route` files; `await params`; `revalidatePath`/`revalidateTag`/`updateTag`; `proxy.js` (was `middleware.js`); opt-in Cache Components. **React Router 8 framework mode** — `routes.ts`, route modules with `loader`/`clientLoader`/`action`/`ErrorBoundary`, `ssr`/`prerender` config, RSC experimental. **TanStack Start** — full-stack on TanStack Router, v1 release candidate, RSC experimental. **Choose**: public + SEO → Next.js or React Router framework; behind a login → Vite SPA + React Router + TanStack Query; mobile → Expo; mostly content → SSG/Astro.

---

## 16.16 Patterns

| Need | Pattern | Shape | Watch out |
|---|---|---|---|
| Flexible UI structure | **Composition** | `children` + slot props | Prefer to growing lists of boolean props [§14.1](../14-patterns-and-architecture/README.md#141-composition-first) |
| Share **stateful logic** | **Custom Hook** | `useProductSearch(query)` → small API | Shares logic, not state; name `use…` only if it calls Hooks [§7.3](../07-custom-hooks-and-hook-rules/README.md#73-custom-hooks--reusing-stateful-logic), [§14.3](../14-patterns-and-architecture/README.md#143-extracting-custom-hooks) |
| Separate data from looks | **Container / presentational** | Hook or Server Component + props-only view | Not by rule — where reuse/testing benefits [§14.2](../14-patterns-and-architecture/README.md#142-container--presentational) |
| Parts sharing implicit state | **Compound components** | `<Tabs><Tabs.List><Tabs.Tab/></Tabs.List><Tabs.Panel/></Tabs>` via private context | Throw a clear error outside the root; support controlled + uncontrolled [§14.4](../14-patterns-and-architecture/README.md#144-compound-components) |
| Component decides where/how often caller markup renders | **Render prop** | `renderItem={(item) => …}`, `children` as a function | Callback pyramids; a Hook is simpler for logic only [§14.5](../14-patterns-and-architecture/README.md#145-render-props) |
| Wrap many components with the same behaviour | **HOC** | `withAuth(Page)` | Create at module level (in render → remount every time); `displayName`; prop collisions [§14.6](../14-patterns-and-architecture/README.md#146-higher-order-components-hocs) |
| Escape `overflow`/stacking | **Portal** | `createPortal(node, document.body)` | Events bubble through the **React** tree; manage focus; read the latest `onClose` in the Escape-key Effect with `useEffectEvent`; consider native `<dialog>` [§14.8](../14-patterns-and-architecture/README.md#148-portals) |
| Contain crashes | **Error boundary** | Class with `getDerivedStateFromError` + `componentDidCatch` (or `react-error-boundary`) | Catches render/lifecycle/Effect errors below it, `use()` rejections and `lazy` failures — **not** event handlers, async callbacks, SSR or itself [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries) |
| Wait for code/data | **Suspense boundary** | `<Suspense fallback>` | Pair with an error boundary; transitions keep old UI [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use) |
| Organise files | **By feature** | `features/cart/index.js` public API; `app → features → shared` | Enforce with lint; avoid giant barrels [§14.11](../14-patterns-and-architecture/README.md#1411-folder-structure) |
| Consistency | **Design system** | Tokens → accessible primitives → docs; closed variants, `type="button"` default, pass-through props and `ref` | Build on headless primitives [§14.12](../14-patterns-and-architecture/README.md#1412-design-systems) |

React 19 root error options: `createRoot(el, { onCaughtError, onUncaughtError, onRecoverableError })`. With **no** boundary, an uncaught render error unmounts the **whole root** [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries).

---

## 16.17 Accessibility checklist

[§14.10](../14-patterns-and-architecture/README.md#1410-accessibility-in-react), [§7.8](../07-custom-hooks-and-hook-rules/README.md#78-useid--ids-for-accessibility), [§4.3](../04-forms-and-actions/README.md#43-validation)
- [ ] **Semantic elements first**: `<button>` (not `<div onClick>`), `<a href>`, `<nav>`, `<main>`, headings in order, lists, `<form>` + `type="submit"` (Enter works for free).
- [ ] Every input has a **label**: `<label htmlFor={id}>` with `id` from `useId()` (never hard-coded in reusable components); placeholders aren't labels.
- [ ] Images have `alt` (empty `alt=""` for decoration); icon-only buttons have `aria-label` and `aria-hidden="true"` on the icon.
- [ ] ARIA driven by state and spelled with dashes: `aria-expanded={open}`, `aria-selected`, `aria-current`, `aria-controls`, `aria-invalid`, `aria-describedby`. First rule of ARIA: use a native element when one exists.
- [ ] **Live regions** (`role="status"` polite, `role="alert"` urgent) exist in the DOM *before* their text changes.
- [ ] **Focus management** with refs: into a dialog on open, trapped (or background `inert`), back to the trigger on close; to the first invalid field on a failed submit; to the new page's `<h1>` (`tabIndex={-1}`) after client-side navigation.
- [ ] Keyboard patterns: Escape closes, arrow keys move inside tabs/menus/listboxes (roving `tabIndex`), everything reachable with Tab, visible `:focus-visible` styles (never remove outlines without a replacement).
- [ ] Contrast ≥ 4.5:1 for normal text (WCAG 2.2), in both themes; don't signal errors by colour alone.
- [ ] Update `document.title` (React 19 lets any component render `<title>`).
- [ ] Test with `getByRole(role, { name })`, `eslint-plugin-jsx-a11y`, axe (`vitest-axe`, Playwright), and a real keyboard + screen reader.
- [ ] Complex widgets (menus, comboboxes, date pickers): use headless libraries (Radix, React Aria) rather than hand-rolling ARIA.

---

## 16.18 Common errors and fixes

Every message below appeared in the output of an earlier chapter's program. The React, Testing Library and props messages are reproduced verbatim by the check program at the top of this chapter (first sentence shown); the Hook-order warning comes from Chapter 7's run and the TypeScript errors from Chapter 11's `tsc` run.

| Message (React 19.3, development) | Usual cause | Fix | Read |
|---|---|---|---|
| *Too many re-renders. React limits the number of renders to prevent an infinite loop.* | A setter called **during render** — often `onClick={setOpen(true)}` or `onClick={handle()}` | Pass a function: `onClick={() => setOpen(true)}`; move logic to a handler | [§3.1](../03-state-and-events/README.md#31-responding-to-events) |
| *Objects are not valid as a React child (found: object with keys {…}).* | Rendering an object (`{user}`, a `Date`, a Promise) | Render a field `{user.name}`, format it, or `map` an array | [§1.8](../01-why-react-and-setup/README.md#18-jsx) |
| *Each child in a list should have a unique "key" prop.* | `map` without `key`, or key on the wrong element | `key={item.id}` on the outermost element returned by `map` | [§2.8](../02-components-and-props/README.md#28-rendering-lists-and-keys) |
| *Encountered two children with the same key, `1`.* | Duplicate IDs, or keys built from non-unique fields | Use a truly unique, stable ID | [§2.8](../02-components-and-props/README.md#28-rendering-lists-and-keys) |
| *The tag &lt;profile&gt; is unrecognized in this browser.* | Component name starts with a lowercase letter | Capitalise: `Profile` | [§2.1](../02-components-and-props/README.md#21-function-components) |
| *Invalid DOM property `class`. Did you mean `className`?* | HTML attribute names in JSX | `className`, `htmlFor`, camelCase events | [§1.8](../01-why-react-and-setup/README.md#18-jsx) |
| *Unsupported style property @media (max-width: 600px).* | Selectors/media queries in the `style` object | Use CSS (a class, CSS Module, Tailwind variant) | [§8.3](../08-styling/README.md#83-inline-styles--the-style-prop) |
| *You provided a `value` prop to a form field without an `onChange` handler.* | Controlled `value` with no handler → read-only field | Add `onChange`, or use `defaultValue`, or `readOnly` | [§3.9](../03-state-and-events/README.md#39-controlled-inputs-basics) |
| *A component is changing an uncontrolled input to be controlled.* | `value` was `undefined`/`null`, later a string | Initialise with `''` | [§4.2](../04-forms-and-actions/README.md#42-controlled-vs-uncontrolled-inputs) |
| *Rendered more hooks than during the previous render.* / *Rendered fewer hooks than expected. This may be caused by an accidental early return statement.* | A Hook inside an `if`, loop, or after an early `return` | Call every Hook unconditionally at the top; put the condition inside the Hook | [§7.2](../07-custom-hooks-and-hook-rules/README.md#72-the-rules-of-hooks) |
| *React has detected a change in the order of Hooks called by …* | Same count, different Hook kinds between renders (conditional Hooks) | Same fix — stable call order | [§7.2](../07-custom-hooks-and-hook-rules/README.md#72-the-rules-of-hooks) |
| *Invalid hook call. Hooks can only be called inside of the body of a function component.* | Hook called in a plain function, handler or module scope; calling a Hook outside `renderHook` in tests; **two copies of React** in the bundle | Call Hooks only in components/custom Hooks; dedupe React (`npm ls react`) | [§7.2](../07-custom-hooks-and-hook-rules/README.md#72-the-rules-of-hooks), [§12.7](../12-testing-react/README.md#127-testing-custom-hooks) |
| *The result of getSnapshot should be cached to avoid an infinite loop* → *Maximum update depth exceeded.* | `useSyncExternalStore` `getSnapshot` returns a new object each call (same with a Zustand selector returning a new object) | Return the same reference until the store changes; select primitives; `useShallow` | [§7.9](../07-custom-hooks-and-hook-rules/README.md#79-usesyncexternalstore--subscribing-to-outside-data), [§6.8](../06-state-management/README.md#68-zustand) |
| *A component was suspended by an uncached promise.* | `use(fetch(...))` — a new promise on every render | Create the promise once (cache, loader, Server Component prop) or use `useSuspenseQuery` | [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use) |
| *Element type is invalid. Received a promise that resolves to: undefined. Lazy element type must resolve to a class or function.* | `lazy(() => import('./X'))` where `X` has no **default** export | `.then(m => ({ default: m.X }))` | [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense) |
| *Hydration failed because the server rendered text didn't match the client.* | Render output differs between server and client (`window`, dates, random, locale, invalid nesting) | Same first render; switch after mount; `useId`; fix the HTML nesting | [§13.5](../13-server-rendering-and-frameworks/README.md#135-hydration-mismatches) |
| *An update to T inside a test was not wrapped in act(...).* | Something updated state after the test stopped waiting (missing `await`, un-awaited timer or fetch) | `await findBy…` / `waitFor`; `act` around direct Hook calls and fake-timer advances | [§12.2](../12-testing-react/README.md#122-your-first-component-test), [§12.5](../12-testing-react/README.md#125-async-tests) |
| *React has blocked a javascript: URL as a security precaution.* (in the rewritten `href`) | A `javascript:` URL in `href` | Validate URLs; use a button with a handler | [§1.8](../01-why-react-and-setup/README.md#18-jsx) |
| `TypeError: Cannot assign to read only property 'title' of object` | Mutating props (frozen in development) | Ask the parent via a callback, or use local state | [§2.2](../02-components-and-props/README.md#22-props) |
| `Cannot read properties of null (reading 'price')` inside a boundary | Rendering before data exists / missing provider | Handle loading states; error boundary + reset keys; context Hook that throws a clear message | [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries), [§6.5](../06-state-management/README.md#65-reducer--context--a-store-with-only-react) |
| `useTasks must be used inside <TasksProvider>` *(your own message)* | Component rendered outside its provider | Wrap the tree (or the test) in the provider | [§6.5](../06-state-management/README.md#65-reducer--context--a-store-with-only-react), [§12.8](../12-testing-react/README.md#128-providers-routers-and-custom-render) |
| RTL: *Unable to find an accessible element with the role "button" and name "…"* | Wrong name — or the element isn't accessible (a `<div onClick>`, an unlabelled icon button) | Read the printed role list; fix the query **or** the markup | [§12.3](../12-testing-react/README.md#123-queries-and-their-priority), [§14.10](../14-patterns-and-architecture/README.md#1410-accessibility-in-react) |
| RTL: *Found multiple elements with the role "button"* | Query not specific enough; DOM left over from a previous test | Add `{ name }`, use `within()`, run `cleanup` after each test | [§12.3](../12-testing-react/README.md#123-queries-and-their-priority), [§12.2](../12-testing-react/README.md#122-your-first-component-test) |
| TS2322 *Type 'string' is not assignable to type 'never'.* | `useState([])` inferred `never[]` | `useState<string[]>([])` | [§11.10](../11-typescript-with-react/README.md#1110-reading-type-errors-and-common-mistakes) |
| TS18047 *'user' is possibly 'null'.* | Using nullable state without narrowing | `user ? … : …`, `user?.name` | [§11.10](../11-typescript-with-react/README.md#1110-reading-type-errors-and-common-mistakes) |
| TS2554 *Expected 1 arguments, but got 0.* | `useRef<HTMLInputElement>()` with React 19 types | `useRef<HTMLInputElement>(null)` | [§11.10](../11-typescript-with-react/README.md#1110-reading-type-errors-and-common-mistakes) |
| TS2741 *Property 'label' is missing in type '{}' but required in type 'BadgeProps'.* | Required prop not passed | Pass it, or make it optional with a default | [§11.10](../11-typescript-with-react/README.md#1110-reading-type-errors-and-common-mistakes) |

**Silent bugs (no message at all)** — the ones to recognise by symptom:

| Symptom | Cause | Fix |
|---|---|---|
| Counter goes up by 1 instead of 3 | Snapshot + plain values | Updater functions [§3.5](../03-state-and-events/README.md#35-updater-functions--queueing-updates) |
| Screen doesn't update after "changing" state | Mutation, same reference | New object/array [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably) |
| Typed text jumps to another row | Index keys + insert/sort | Stable IDs [§2.8](../02-components-and-props/README.md#28-rendering-lists-and-keys) |
| Input loses focus on every keystroke | Component defined inside a component, or random keys | Top-level components, stable keys [§10.10](../10-performance/README.md#1010-common-performance-bugs) |
| Effect runs twice in development | StrictMode double run | Correct cleanup [§5.4](../05-effects-and-refs/README.md#54-strictmodes-double-run-development-only) |
| Old search results overwrite new ones | Fetch race | `AbortController` / ignore flag [§5.7](../05-effects-and-refs/README.md#57-fetching-data-in-effects-and-race-conditions) |
| `memo` child still re-renders | Inline object/function props, or context | Stable props, split/memoise context [§10.4](../10-performance/README.md#104-memo--skipping-a-childs-re-render) |
| Interval always sees the first value | Stale closure (`[]` deps) | Updater, or `useEffectEvent` [§7.7](../07-custom-hooks-and-hook-rules/README.md#77-hooks-that-take-callbacks--useeffectevent) |
| Changing a prop doesn't reset the child's state | Same type at same position keeps state | `key` [§3.10](../03-state-and-events/README.md#310-state-is-tied-to-a-position-in-the-tree) |
| Clicks inside a modal trigger the parent's "click outside" | Portal events bubble through React | Check `e.target`, or stop propagation deliberately [§14.8](../14-patterns-and-architecture/README.md#148-portals) |
| Error in `onClick` doesn't show the fallback | Boundaries don't catch handler errors | `try/catch` → state [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries) |
| Page reloads on submit, state lost | No `preventDefault` / no function `action` | `e.preventDefault()` or `<form action={fn}>` [§4.1](../04-forms-and-actions/README.md#41-how-a-form-submission-works) |
| 404 when refreshing `/products/3` in an SPA | Server doesn't fall back to `index.html` | Configure the fallback [§9.3](../09-routing-and-data-fetching/README.md#93-routes-links-and-url-parameters-declarative-mode) |

---

## 16.19 Interview question index

The 53 questions of Chapter 15, grouped as there. Each link opens the section that holds the question; the question numbers are in bold there.

| Area | Questions |
|---|---|
| Rendering, reconciliation, keys | [Q1](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) what happens after a setter · [Q2](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) render vs commit, which must be pure · [Q3](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) reconciliation and "virtual DOM" · [Q4](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) why keys, why not the index · [Q5](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) changing a component's `key` · [Q6](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) when a component re-renders · [Q7](../15-react-interview-and-machine-coding/README.md#152-interview-questions-rendering-reconciliation-and-keys) why `{items.length && …}` renders 0 |
| State, events, forms | [Q8](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) three `setCount(count + 1)` · [Q9](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) batching and React 18 · [Q10](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) immutability · [Q11](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) derived state, props copied into state · [Q12](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) where state should live · [Q13](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) controlled vs uncontrolled · [Q14](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) `useActionState`, `useFormStatus`, `useOptimistic` · [Q15](../15-react-interview-and-machine-coding/README.md#153-interview-questions-state-events-and-forms) React vs DOM events |
| Effects and refs | [Q16](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) what `useEffect` is for · [Q17](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) no array vs `[]` vs `[a, b]` · [Q18](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) cleanup timing and Effect order · [Q19](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) why things run twice in development · [Q20](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) fetch race conditions · [Q21](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) `useEffect` vs `useLayoutEffect` · [Q22](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) `useRef` vs `useState` · [Q23](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) refs to your own components in React 19 · [Q24](../15-react-interview-and-machine-coding/README.md#154-interview-questions-effects-and-refs) `useEffectEvent` |
| Hooks, context, state management | [Q25](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) Rules of Hooks · [Q26](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) custom Hooks share state? · [Q27](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) `useState` or `useReducer` · [Q28](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) is Context state management? · [Q29](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) Context performance · [Q30](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) choosing an approach · [Q31](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) server vs client state · [Q32](../15-react-interview-and-machine-coding/README.md#155-interview-questions-hooks-context-and-state-management) `useSyncExternalStore` |
| Performance and the Compiler | [Q33](../15-react-interview-and-machine-coding/README.md#156-interview-questions-performance-and-the-react-compiler) `memo` · [Q34](../15-react-interview-and-machine-coding/README.md#156-interview-questions-performance-and-the-react-compiler) `useMemo` vs `useCallback` · [Q35](../15-react-interview-and-machine-coding/README.md#156-interview-questions-performance-and-the-react-compiler) React Compiler · [Q36](../15-react-interview-and-machine-coding/README.md#156-interview-questions-performance-and-the-react-compiler) investigating a slow page · [Q37](../15-react-interview-and-machine-coding/README.md#156-interview-questions-performance-and-the-react-compiler) code splitting · [Q38](../15-react-interview-and-machine-coding/README.md#156-interview-questions-performance-and-the-react-compiler) 10,000 rows |
| Suspense, `use()`, transitions | [Q39](../15-react-interview-and-machine-coding/README.md#157-interview-questions-suspense-use-and-transitions) what can suspend · [Q40](../15-react-interview-and-machine-coding/README.md#157-interview-questions-suspense-use-and-transitions) `use()` · [Q41](../15-react-interview-and-machine-coding/README.md#157-interview-questions-suspense-use-and-transitions) `useTransition` vs `useDeferredValue` · [Q42](../15-react-interview-and-machine-coding/README.md#157-interview-questions-suspense-use-and-transitions) fallback on a suspending update |
| Server rendering and RSC | [Q43](../15-react-interview-and-machine-coding/README.md#158-interview-questions-server-rendering-hydration-and-server-components) CSR/SSR/SSG/streaming · [Q44](../15-react-interview-and-machine-coding/README.md#158-interview-questions-server-rendering-hydration-and-server-components) hydration and mismatches · [Q45](../15-react-interview-and-machine-coding/README.md#158-interview-questions-server-rendering-hydration-and-server-components) Server vs Client Components · [Q46](../15-react-interview-and-machine-coding/README.md#158-interview-questions-server-rendering-hydration-and-server-components) `'use server'` · [Q47](../15-react-interview-and-machine-coding/README.md#158-interview-questions-server-rendering-hydration-and-server-components) `'use client'` on the root layout |
| Errors, portals, patterns, testing, a11y, TS | [Q48](../15-react-interview-and-machine-coding/README.md#159-interview-questions-error-boundaries-portals-patterns-testing-and-accessibility) what error boundaries catch · [Q49](../15-react-interview-and-machine-coding/README.md#159-interview-questions-error-boundaries-portals-patterns-testing-and-accessibility) portals and event bubbling · [Q50](../15-react-interview-and-machine-coding/README.md#159-interview-questions-error-boundaries-portals-patterns-testing-and-accessibility) compound vs render props vs HOCs vs Hooks · [Q51](../15-react-interview-and-machine-coding/README.md#159-interview-questions-error-boundaries-portals-patterns-testing-and-accessibility) Testing Library philosophy · [Q52](../15-react-interview-and-machine-coding/README.md#159-interview-questions-error-boundaries-portals-patterns-testing-and-accessibility) accessible components · [Q53](../15-react-interview-and-machine-coding/README.md#159-interview-questions-error-boundaries-portals-patterns-testing-and-accessibility) typing props, children, events |

**Machine-coding builds** (each with requirements, state design, code, an RTL test and a screenshot): [§15.10](../15-react-interview-and-machine-coding/README.md#1510-build-1--todo-list-with-filters) todo list with filters · [§15.11](../15-react-interview-and-machine-coding/README.md#1511-build-2--autocomplete-debounce-abort-keyboard) autocomplete (debounce, abort, combobox keyboard) · [§15.12](../15-react-interview-and-machine-coding/README.md#1512-build-3--infinite-scroll) infinite scroll (`IntersectionObserver`) · [§15.13](../15-react-interview-and-machine-coding/README.md#1513-build-4--modal-dialog-portal-focus-trap-escape) modal (portal, focus trap, Escape) · [§15.14](../15-react-interview-and-machine-coding/README.md#1514-build-5--accessible-tabs) accessible tabs (roving tabindex) · [§15.15](../15-react-interview-and-machine-coding/README.md#1515-build-6--star-rating) star rating (native radios) · [§15.16](../15-react-interview-and-machine-coding/README.md#1516-build-7--pagination) pagination (pure range function) · [§15.17](../15-react-interview-and-machine-coding/README.md#1517-build-8--multi-step-form-wizard-with-validation) multi-step form wizard · [§15.18](../15-react-interview-and-machine-coding/README.md#1518-other-common-prompts--which-techniques-they-need) ten more prompts mapped to techniques. The method: requirements → component tree → state design → happy path → keyboard/a11y → test → edge cases [§15.1](../15-react-interview-and-machine-coding/README.md#151-how-react-interviews-are-run).

---

## 16.20 Where to go next

**Step 1 — Lock in the core (1–2 weeks).** Rebuild three of the Chapter 15 builds from a blank file without looking, with tests. If you get stuck, the sheets in this chapter tell you which section to reread. Make sure you can explain snapshot + updaters, keys, Effects vs handlers and Context re-renders out loud — they are the most common interview topics.

**Step 2 — Build one real app (2–4 weeks).** Something with routing, server data, a form and auth — for example a reading list or a small shop:
- Vite + React Router data mode + TanStack Query + one form with `useActionState`, deployed as an SPA;
- then move it to a framework (React Router framework mode or Next.js App Router) and compare SSR, loaders vs Server Components, and the bundle size;
- add TypeScript (`strict`), Vitest + Testing Library tests, and turn on the React Compiler.

**Step 3 — Go deeper where your job needs it.**

| Direction | Read / do next |
|---|---|
| Framework work | Next.js docs (App Router, Cache Components), React Router framework docs; Chapter 13 again |
| Performance | React DevTools Profiler on your own app; web.dev on Core Web Vitals (LCP, INP, CLS) and the last chapter of the HTML & CSS book ([critical rendering path](../../../html-css/chapters/14-performance-seo-interview-and-cheatsheet/README.md#141-the-critical-rendering-path), [Core Web Vitals](../../../html-css/chapters/14-performance-seo-interview-and-cheatsheet/README.md#143-core-web-vitals-lcp-inp-cls)); [responsive images](../../../html-css/chapters/04-images-media-and-embeds/README.md) |
| Design systems / accessibility | WAI-ARIA Authoring Practices patterns; Radix / React Aria source; the HTML & CSS book's [accessibility chapter](../../../html-css/chapters/03-semantic-html-and-accessibility/README.md) |
| JavaScript depth | The JavaScript book: [closures](../../../javascript/chapters/04-functions-scope-and-closures/README.md), [async and the event loop](../../../javascript/chapters/12-asynchronous-javascript/README.md), its [cheat sheets](../../../javascript/chapters/18-cheatsheet-and-glossary/README.md) |
| Front-end system design | Component architecture and machine coding in the LLD book: [UI components case](../../../lld/chapters/20-case-components/README.md), [machine coding](../../../lld/chapters/21-case-machine-coding/README.md) |
| Mobile | Expo (React Native) — the same components-and-Hooks model on iOS and Android |

**Step 4 — Keep up to date.** Primary sources, in this order: **react.dev** (Learn + Reference + the blog, where every release and deprecation is announced), the React GitHub releases, then the docs of the libraries you use (React Router, TanStack Query, Redux Toolkit, Zustand, Testing Library, Next.js). Check the date on any tutorial: anything using class components, Create React App, `ReactDOM.render`, `forwardRef` for new code, or `useFormState` predates the React 19 era this book teaches.

---

## 16.21 Glossary (A–Z)

Each term links to the section that explains it (the first link is the main one).

**A**

- **Accessible name** — What assistive technology announces for an element — a button's text, an input's label, `aria-label`/`aria-labelledby`, an image's `alt`; what `getByRole(role, { name })` matches. [§12.3](../12-testing-react/README.md#123-queries-and-their-priority), [§14.10](../14-patterns-and-architecture/README.md#1410-accessibility-in-react)
- **`act()`** — Test helper that makes React finish every pending render and Effect before the next line runs; RTL wraps its helpers in it, so you call it only for direct updates (e.g. `renderHook` calls, fake timers). [§12.2](../12-testing-react/README.md#122-your-first-component-test), [§12.7](../12-testing-react/README.md#127-testing-custom-hooks)
- **Action** — In React 19, a function run inside a Transition — passed to `<form action>`, `formAction`, or `startTransition(async …)`; React tracks its pending state. [§4.4](../04-forms-and-actions/README.md#44-form-actions--form-actionfn), [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates)
- **Action (React Router)** — A route's function for writes; `<Form method="post">` calls it, then the router re-runs loaders (revalidation). [§9.6](../09-routing-and-data-fetching/README.md#96-actions-form-and-fetchers)
- **Action (reducer)** — A plain object describing what happened, e.g. `{ type: 'added', text }`, sent to a reducer with `dispatch`. [§6.2](../06-state-management/README.md#62-usereducer--consolidate-update-logic)
- **Action creator** — A function returning an action object; Redux Toolkit's `createSlice` generates them (`added('p1')` → `{ type: 'cart/added', payload: 'p1' }`). [§6.7](../06-state-management/README.md#67-redux-toolkit)
- **`<Activity>`** — React 19.2 component that hides a subtree while keeping its state; one of the real remounts StrictMode's double run prepares you for. [§5.4](../05-effects-and-refs/README.md#54-strictmodes-double-run-development-only)
- **`aria-describedby`** — Links an element to text that describes it (e.g. a field's error message), making that text its accessible description. [§4.3](../04-forms-and-actions/README.md#43-validation), [§14.10](../14-patterns-and-architecture/README.md#1410-accessibility-in-react)
- **`aria-invalid`** — Attribute set to `true` on a field whose value failed validation, so screen readers announce it. [§4.3](../04-forms-and-actions/README.md#43-validation), [§14.10](../14-patterns-and-architecture/README.md#1410-accessibility-in-react)
- **`aria-live` / live region** — An element (`role="status"` or `role="alert"`) whose text changes are announced; it must already exist before the text changes. [§14.10](../14-patterns-and-architecture/README.md#1410-accessibility-in-react)
- **Automatic batching** — Since React 18, all state updates from one event — including in timeouts, promises and native listeners — are combined into one re-render. [§3.4](../03-state-and-events/README.md#34-batching)
- **Automatic JSX runtime** — The React 17+ compile mode that imports `jsx` from `react/jsx-runtime` for you, so files don't need `import React`. [§1.8](../01-why-react-and-setup/README.md#18-jsx)

**B**

- **Bail-out** — When a setter receives a value `Object.is`-equal to the current state, React skips re-rendering that component and its children. [§3.2](../03-state-and-events/README.md#32-state-a-components-memory--usestate), [§10.1](../10-performance/README.md#101-the-render-cycle-trigger--render--commit)
- **Barrel file** — An `index.js` that re-exports a module's contents; one per feature is fine, giant ones slow tools and hurt tree-shaking. [§14.11](../14-patterns-and-architecture/README.md#1411-folder-structure)
- **`baseDuration`** — `<Profiler>` value: estimated time for a full re-render of the subtree — compare with `actualDuration` to see what memoization saves. [§10.3](../10-performance/README.md#103-measure-first-devtools-profiler-and-profiler)
- **BEM** — Block__Element--Modifier CSS naming convention that avoids clashes in global stylesheets. [§8.2](../08-styling/README.md#82-plain-css-files)
- **`browser()`** — React 19.3 `react-dom` function for `use(browser())`: suspends only on the server, so the nearest `<Suspense>` fallback is sent and the client renders the real content — no hydration mismatch. [§13.5](../13-server-rendering-and-frameworks/README.md#135-hydration-mismatches)
- **Bundler** — Tool (Vite, which bundles with Rolldown since Vite 8; esbuild; webpack; Turbopack) that combines modules into files for the browser and splits chunks for `import()`. [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense), [§1.5](../01-why-react-and-setup/README.md#15-setting-up-a-react-project)

**C**

- **Cache Components** — Next.js 16 opt-in model: everything is dynamic unless cached with `'use cache'`; the build prerenders a static shell (PPR). [§13.9](../13-server-rendering-and-frameworks/README.md#139-nextjs-app-router-overview)
- **Children (`children` prop)** — Whatever is nested between a component's tags; render it with `{children}` to build wrappers. [§2.3](../02-components-and-props/README.md#23-the-children-prop)
- **Class component** — Legacy component style (`class X extends Component` with `render()`); still required for error boundaries. [§2.1](../02-components-and-props/README.md#21-function-components), [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries)
- **Cleanup function** — The function an Effect's setup returns; it undoes the setup and runs before the next setup and on unmount. [§5.3](../05-effects-and-refs/README.md#53-cleanup--undo-what-setup-did)
- **Client Component** — A component whose code ships to the browser (marked by a `'use client'` entry point); may use state, Effects and events. [§13.7](../13-server-rendering-and-frameworks/README.md#137-react-server-components)
- **Client-side routing** — Changing screens without a page load: the router intercepts links, updates the URL with the History API, and renders the matching route. [§9.1](../09-routing-and-data-fetching/README.md#91-client-side-routing)
- **Closure** — A function that remembers the variables of the render that created it — why handlers see a state snapshot. [§3.3](../03-state-and-events/README.md#33-state-as-a-snapshot)
- **`clsx`** — Tiny helper that builds a `className` string from strings, conditions and objects, dropping falsy values. [§8.5](../08-styling/README.md#85-composing-class-names-from-props)
- **Code splitting** — Splitting the bundle into chunks loaded on demand, e.g. `lazy(() => import('./Chart'))`. [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense)
- **Colocation** — Keeping state, files and tests next to where they are used, so changes stay local and re-renders small. [§14.11](../14-patterns-and-architecture/README.md#1411-folder-structure), [§10.10](../10-performance/README.md#1010-common-performance-bugs)
- **Commit phase** — The step where React applies DOM changes, sets refs and runs layout effects; runs once per committed update. [§10.1](../10-performance/README.md#101-the-render-cycle-trigger--render--commit), [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)
- **Component** — A capitalised JavaScript function that takes props and returns JSX (or `null`). [§1.7](../01-why-react-and-setup/README.md#17-your-first-component), [§2.1](../02-components-and-props/README.md#21-function-components)
- **Component tree** — The hierarchy of components React renders; visible in React DevTools, not in the DOM. [§1.7](../01-why-react-and-setup/README.md#17-your-first-component)
- **Composition** — Building UIs by nesting components and passing JSX (`children`, slots) instead of inheritance or many boolean props. [§2.4](../02-components-and-props/README.md#24-composition), [§14.1](../14-patterns-and-architecture/README.md#141-composition-first)
- **Compound components** — A family like `Tabs`/`Tabs.Tab`/`Tabs.Panel` that shares state through a private context. [§14.4](../14-patterns-and-architecture/README.md#144-compound-components)
- **Concurrent rendering** — React 18+ ability to prepare UI in the background, pause it for urgent work, and discard stale work. [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates), [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)
- **Container / presentational** — Splitting "where data comes from" (container, Hook or Server Component) from "how it looks" (props-only view). [§14.2](../14-patterns-and-architecture/README.md#142-container--presentational)
- **Context** — `createContext` + `<Ctx value>` + `useContext`/`use`: passes a value to any depth without prop drilling; a transport, not a store. [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props)
- **Controlled component** — A component whose value comes from props/state (`value` + `onChange`, or `value`/`onValueChange` in design systems). [§3.9](../03-state-and-events/README.md#39-controlled-inputs-basics), [§4.2](../04-forms-and-actions/README.md#42-controlled-vs-uncontrolled-inputs), [§14.4](../14-patterns-and-architecture/README.md#144-compound-components)
- **Controlled input** — A form field whose displayed value is React state: `value={state}` plus `onChange`. [§3.9](../03-state-and-events/README.md#39-controlled-inputs-basics), [§4.2](../04-forms-and-actions/README.md#42-controlled-vs-uncontrolled-inputs)
- **`createPortal`** — `react-dom` API that renders children into another DOM node while keeping them in the same React tree. [§14.8](../14-patterns-and-architecture/README.md#148-portals)
- **`createRoot`** — `react-dom/client` API that creates a React root for a DOM element; `root.render(<App />)` starts rendering. [§1.6](../01-why-react-and-setup/README.md#16-project-structure)
- **`createSlice`** — Redux Toolkit function that generates a reducer and action creators; its reducers use Immer. [§6.7](../06-state-management/README.md#67-redux-toolkit)
- **CSR** — Client-side rendering: the server sends a near-empty page and JavaScript builds the UI in the browser (an SPA). [§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg), [§13.1](../13-server-rendering-and-frameworks/README.md#131-rendering-strategies-csr-ssr-ssg-isr-streaming-ppr)
- **CSS Modules** — `*.module.css` files whose class names are renamed per file, so `styles.card` can't clash. [§8.4](../08-styling/README.md#84-css-modules)
- **CSS-in-JS** — Writing styles in JavaScript; runtime libraries (styled-components, Emotion) inject styles while rendering, zero-runtime ones extract CSS at build. [§8.7](../08-styling/README.md#87-css-in-js)
- **Custom Hook** — A function named `useSomething` that calls other Hooks; shares logic, never state. [§7.3](../07-custom-hooks-and-hook-rules/README.md#73-custom-hooks--reusing-stateful-logic), [§14.3](../14-patterns-and-architecture/README.md#143-extracting-custom-hooks)

**D**

- **Debounce** — Wait until input has been quiet for N ms before acting (e.g. searching). [§7.5](../07-custom-hooks-and-hook-rules/README.md#75-usedebounce--wait-until-the-user-stops)
- **`defaultValue`** — Starting value of an uncontrolled input; the DOM owns the value afterwards. [§4.2](../04-forms-and-actions/README.md#42-controlled-vs-uncontrolled-inputs)
- **Dependency array** — The second argument of `useEffect`/`useMemo`/`useCallback`: every reactive value the callback reads; compared with `Object.is`. [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth), [§10.5](../10-performance/README.md#105-usecallback-and-usememo-for-stable-props)
- **Derived state** — A value computed from props/state during render instead of being stored; never kept in `useState`. [§3.8](../03-state-and-events/README.md#38-derived-state--choosing-the-minimal-state)
- **Design system** — Tokens + accessible primitives + usage docs shared across a product. [§14.12](../14-patterns-and-architecture/README.md#1412-design-systems)
- **Design tokens** — Named design values (colours, spacing, radii) usually stored as CSS custom properties. [§8.8](../08-styling/README.md#88-theming-and-dark-mode), [§14.12](../14-patterns-and-architecture/README.md#1412-design-systems)
- **Directive** — A string literal at the top of a file or function (`'use client'`, `'use server'`) that bundlers read. [§13.8](../13-server-rendering-and-frameworks/README.md#138-directives-use-client-and-use-server)
- **Discriminated union** — A TypeScript union of object types sharing a literal field (e.g. `status`) that narrows to one member. [§11.9](../11-typescript-with-react/README.md#119-discriminated-unions-for-props), [§11.5](../11-typescript-with-react/README.md#115-typing-state-and-reducers)
- **Dispatch** — Sending an action to a reducer: `dispatch({ type: 'added' })`; stable identity. [§6.2](../06-state-management/README.md#62-usereducer--consolidate-update-logic)
- **Double fetch** — When the client refetches data the server already used for SSR; frameworks serialize data into the page instead. [§13.4](../13-server-rendering-and-frameworks/README.md#134-hydration-end-to-end)

**E**

- **Effect** — Code in `useEffect` that runs after commit to synchronise a component with an external system. [§5.2](../05-effects-and-refs/README.md#52-useeffect-basics)
- **Effect Event** — A function created by `useEffectEvent`: called from Effects, always sees the latest props/state, and is not a dependency. [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth), [§7.7](../07-custom-hooks-and-hook-rules/README.md#77-hooks-that-take-callbacks--useeffectevent)
- **Error boundary** — A class component with `getDerivedStateFromError`/`componentDidCatch` that shows a fallback when a descendant throws while rendering. [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries)
- **Event delegation** — React attaches one listener per event type at the root container and dispatches to your handlers. [§3.1](../03-state-and-events/README.md#31-responding-to-events)
- **Event handler** — A function passed as an `on…` prop; the right place for side effects caused by a user action. [§3.1](../03-state-and-events/README.md#31-responding-to-events)
- **`exhaustive-deps`** — `eslint-plugin-react-hooks` rule that checks dependency arrays of Effects, `useMemo` and `useCallback`. [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth), [§7.2](../07-custom-hooks-and-hook-rules/README.md#72-the-rules-of-hooks)
- **Exhaustiveness check** — Assigning a value to `never` in a `default` branch so an unhandled union case becomes a compile error. [§11.5](../11-typescript-with-react/README.md#115-typing-state-and-reducers)
- **External store** — Data that lives outside React and changes on its own (browser APIs, Zustand, Redux); read it with `useSyncExternalStore`. [§7.9](../07-custom-hooks-and-hook-rules/README.md#79-usesyncexternalstore--subscribing-to-outside-data)

**F**

- **Fetcher** — React Router `useFetcher()`: submit or load without navigating (like buttons, inline edits). [§9.6](../09-routing-and-data-fetching/README.md#96-actions-form-and-fetchers)
- **Fiber** — React's internal record for each component instance, holding its Hook slots; also the name of the reconciler since React 16. [§7.1](../07-custom-hooks-and-hook-rules/README.md#71-what-a-hook-really-is), [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)
- **`flushSync`** — `react-dom` function that commits a state update synchronously, opting out of batching; use sparingly. [§3.4](../03-state-and-events/README.md#34-batching), [§5.9](../05-effects-and-refs/README.md#59-refs-to-dom-nodes)
- **`<Form>` (React Router)** — Router form that serialises fields, calls the route `action`, then revalidates loaders. [§9.6](../09-routing-and-data-fetching/README.md#96-actions-form-and-fetchers)
- **Form Action** — React 19 `<form action={fn}>`: React prevents navigation, calls `fn(formData)` in a Transition, and resets uncontrolled fields after success. [§4.4](../04-forms-and-actions/README.md#44-form-actions--form-actionfn)
- **`formAction`** — Prop on a submit button that overrides the form's action for that button. [§4.4](../04-forms-and-actions/README.md#44-form-actions--form-actionfn)
- **`FormData`** — Browser object holding a form's name/value pairs; values are strings, unchecked boxes are missing, `getAll` for repeated names. [§4.1](../04-forms-and-actions/README.md#41-how-a-form-submission-works)
- **`forwardRef`** — Legacy API to pass a ref to a function component; React 19 passes `ref` as a normal prop instead. [§5.10](../05-effects-and-refs/README.md#510-passing-refs-to-your-own-components)
- **Fragment** — `<>…</>` or `<Fragment key>`: groups children without adding a DOM node. [§1.8](../01-why-react-and-setup/README.md#18-jsx)
- **Fragment ref** — React 19.3: a `ref` on `<Fragment>` gives an instance with `focus()`, `addEventListener`, `observeUsing` for its children. [§5.10](../05-effects-and-refs/README.md#510-passing-refs-to-your-own-components)
- **Framework mode** — React Router's full-stack mode: route modules, SSR/prerendering, per-route code splitting, type generation. [§9.7](../09-routing-and-data-fetching/README.md#97-middleware-lazy-routes-and-framework-mode), [§13.10](../13-server-rendering-and-frameworks/README.md#1310-react-router-framework-mode)

**G**

- **`gcTime`** — TanStack Query: how long unused cached data stays in memory (default 5 minutes). [§6.9](../06-state-management/README.md#69-tanstack-query--server-state), [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics)
- **Generic component** — A component with a type parameter, e.g. `List<T>`, whose `T` is inferred from props. [§11.8](../11-typescript-with-react/README.md#118-generic-components)
- **`getDerivedStateFromError`** — Static error-boundary method that returns state so the next render shows the fallback. [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries)
- **`getServerSnapshot`** — Third argument of `useSyncExternalStore`: the value used on the server and during hydration. [§7.9](../07-custom-hooks-and-hook-rules/README.md#79-usesyncexternalstore--subscribing-to-outside-data), [§13.5](../13-server-rendering-and-frameworks/README.md#135-hydration-mismatches)

**H**

- **Headless UI library** — Components that provide behaviour and accessibility but no styles (Radix, React Aria, Headless UI, Base UI). [§8.9](../08-styling/README.md#89-component-libraries), [§14.12](../14-patterns-and-architecture/README.md#1412-design-systems)
- **Higher-order component (HOC)** — A function that takes a component and returns a wrapped one (`withAuth(Page)`); must be created at module level. [§14.6](../14-patterns-and-architecture/README.md#146-higher-order-components-hocs)
- **HMR** — Hot Module Replacement: the dev server swaps an edited module in the running page, keeping state. [§1.5](../01-why-react-and-setup/README.md#15-setting-up-a-react-project)
- **Hook** — A function starting with `use` that lets a component use a React feature; called at the top level only. [§7.1](../07-custom-hooks-and-hook-rules/README.md#71-what-a-hook-really-is)
- **`hydrateRoot`** — `react-dom/client` API that hydrates existing server HTML instead of creating new DOM. [§13.2](../13-server-rendering-and-frameworks/README.md#132-the-server-rendering-apis), [§13.4](../13-server-rendering-and-frameworks/README.md#134-hydration-end-to-end)
- **Hydration** — React in the browser adopting server-rendered HTML: it renders the same tree, reuses the DOM and attaches handlers. [§13.4](../13-server-rendering-and-frameworks/README.md#134-hydration-end-to-end), [§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg)
- **Hydration mismatch** — The client's first render differs from the server HTML; React client-renders that tree and reports a recoverable error. [§13.5](../13-server-rendering-and-frameworks/README.md#135-hydration-mismatches)

**I**

- **Immer** — Library that lets you "mutate" a draft and produces a new immutable state with structural sharing; used by Redux Toolkit. [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably), [§6.7](../06-state-management/README.md#67-redux-toolkit)
- **Immutability** — Never changing existing state objects/arrays; create new ones so `Object.is` sees the change. [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably)
- **Index route** — A child route with `index` that renders at the parent's exact URL. [§9.4](../09-routing-and-data-fetching/README.md#94-nested-routes-and-layouts)
- **Invalidation** — Marking cached queries stale so they refetch: `queryClient.invalidateQueries({ queryKey })`. [§9.10](../09-routing-and-data-fetching/README.md#910-mutations-invalidation-and-optimistic-updates), [§6.9](../06-state-management/README.md#69-tanstack-query--server-state)
- **`isFetching`** — TanStack Query flag: a request is in flight, including background refetches. [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics)
- **`isPending`** — TanStack Query: no data yet. `useTransition`/`useActionState`: the transition or Action is still running. [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics), [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates), [§4.5](../04-forms-and-actions/README.md#45-useactionstate--the-result-of-the-last-submission)
- **ISR** — Incremental static regeneration: static pages re-built in the background after a revalidation window. [§13.1](../13-server-rendering-and-frameworks/README.md#131-rendering-strategies-csr-ssr-ssg-isr-streaming-ppr)

**J**

- **jsdom** — A JavaScript implementation of the DOM used to run component tests in Node; no layout or painting. [§12.9](../12-testing-react/README.md#129-setting-up-vitest-or-jest-in-a-real-project), [§1.5](../01-why-react-and-setup/README.md#15-setting-up-a-react-project)
- **JSX** — HTML-like syntax in JavaScript that compiles to `jsx()`/`createElement` calls returning element objects. [§1.8](../01-why-react-and-setup/README.md#18-jsx)

**K**

- **Key** — A string/number that identifies an element among its siblings; also resets a component when it changes. [§2.8](../02-components-and-props/README.md#28-rendering-lists-and-keys), [§3.10](../03-state-and-events/README.md#310-state-is-tied-to-a-position-in-the-tree)

**L**

- **Layout (route)** — A parent route that renders shared UI and an `<Outlet />` for the matching child. [§9.4](../09-routing-and-data-fetching/README.md#94-nested-routes-and-layouts)
- **`lazy`** — `lazy(() => import('./X'))`: a component whose code loads on first render; needs a default export and a `<Suspense>` above it. [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense)
- **Lifting state up** — Moving shared state to the closest common parent and passing it down with a callback. [§3.7](../03-state-and-events/README.md#37-lifting-state-up)
- **Loader** — React Router data-mode function that loads a route's data before it renders; read with `useLoaderData()`. [§9.5](../09-routing-and-data-fetching/README.md#95-loaders--data-before-render-data-mode)
- **Local mutation** — Mutating values created during the same render (a local array) — allowed inside a pure component. [§2.5](../02-components-and-props/README.md#25-pure-components)

**M**

- **`memo`** — Wraps a component so it skips re-rendering when its parent re-renders with shallow-equal props. [§10.4](../10-performance/README.md#104-memo--skipping-a-childs-re-render)
- **Middleware (React Router)** — Functions that run before a route's loaders/actions and can wrap them with `await next()`; on by default in v8. [§9.7](../09-routing-and-data-fetching/README.md#97-middleware-lazy-routes-and-framework-mode)
- **Middleware (Redux)** — Code between `dispatch` and the reducer (thunks, logging). [§6.7](../06-state-management/README.md#67-redux-toolkit)
- **Mount / unmount** — A component instance being created and inserted into the tree / removed from it (running every cleanup). [§5.2](../05-effects-and-refs/README.md#52-useeffect-basics)
- **MPA** — Multi-page application: every navigation loads a new HTML page from the server. [§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg)
- **MSW** — Mock Service Worker: intercepts HTTP requests at the network level for tests and development. [§12.6](../12-testing-react/README.md#126-mocking-fetch-and-when-to-use-msw)
- **Mutation (TanStack Query)** — A server write run with `useMutation`; not cached by key; followed by invalidation or a cache update. [§9.10](../09-routing-and-data-fetching/README.md#910-mutations-invalidation-and-optimistic-updates)

**N**

- **`noValidate`** — Form attribute that turns off the browser's validation bubbles while keeping the `validity` API. [§4.3](../04-forms-and-actions/README.md#43-validation)

**O**

- **`Object.is`** — The comparison React uses for state, dependencies and `memo` props: same value for primitives, same reference for objects. [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably), [§10.4](../10-performance/README.md#104-memo--skipping-a-childs-re-render)
- **Optimistic update** — Showing the expected result before the server confirms it, rolling back on failure (`useOptimistic`, `onMutate`). [§4.7](../04-forms-and-actions/README.md#47-useoptimistic--instant-feedback-automatic-rollback), [§9.10](../09-routing-and-data-fetching/README.md#910-mutations-invalidation-and-optimistic-updates)
- **Outlet** — `<Outlet />`: where a layout route renders its matched child route. [§9.4](../09-routing-and-data-fetching/README.md#94-nested-routes-and-layouts)
- **Overscan** — Extra rows rendered above and below the visible window of a virtualized list to avoid blank gaps. [§10.9](../10-performance/README.md#109-list-virtualization)

**P**

- **Partial prerendering (PPR)** — Prerender a static shell at build time, save a `postponed` state, and `resume()` only the dynamic holes per request. [§13.6](../13-server-rendering-and-frameworks/README.md#136-static-generation-and-partial-prerendering)
- **Pending UI** — What the user sees while something loads: old content + indicator, skeletons, or disabled buttons. [§9.12](../09-routing-and-data-fetching/README.md#912-loading-and-error-states-that-users-like)
- **Portal** — Content rendered into another DOM node with `createPortal`; context and events still follow the React tree. [§14.8](../14-patterns-and-architecture/README.md#148-portals)
- **Position (in the tree)** — Where a component sits among its parent's children; React keys state to type + position + key. [§3.10](../03-state-and-events/README.md#310-state-is-tied-to-a-position-in-the-tree)
- **Preflight** — Tailwind's CSS reset, which removes default styles such as heading sizes. [§8.6](../08-styling/README.md#86-tailwind-css)
- **Prerender** — Rendering HTML ahead of time; `react-dom/static` `prerender*` waits for all Suspense data. [§13.6](../13-server-rendering-and-frameworks/README.md#136-static-generation-and-partial-prerendering), [§13.2](../13-server-rendering-and-frameworks/README.md#132-the-server-rendering-apis)
- **Profiler** — React DevTools tab (and `<Profiler onRender>` component) that records which components rendered, why, and how long. [§10.3](../10-performance/README.md#103-measure-first-devtools-profiler-and-profiler)
- **Progressive enhancement** — The basic feature works with plain HTML; JavaScript makes it nicer (forms with Server Functions work before hydration). [§4.8](../04-forms-and-actions/README.md#48-server-functions-and-progressive-enhancement-overview)
- **Prop drilling** — Passing a prop through components that don't use it, only to reach a deep child; fixed by composition or context. [§2.4](../02-components-and-props/README.md#24-composition), [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props)
- **Props** — The read-only object of inputs a parent passes to a component. [§2.2](../02-components-and-props/README.md#22-props)
- **Pure component** — Given the same props, state and context, returns the same JSX and changes nothing outside itself during render. [§2.5](../02-components-and-props/README.md#25-pure-components)

**Q**

- **Query function** — TanStack Query `queryFn`: async function that returns data or throws (check `res.ok`). [§6.9](../06-state-management/README.md#69-tanstack-query--server-state), [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics)
- **Query key** — Array that identifies cached data, e.g. `['product', id]`; every input of the query function belongs in it. [§9.9](../09-routing-and-data-fetching/README.md#99-query-keys-pagination-and-dependent-queries), [§6.9](../06-state-management/README.md#69-tanstack-query--server-state)
- **`queryOptions`** — TanStack Query helper that bundles a key and function for reuse in `useQuery`, loaders and prefetching. [§9.9](../09-routing-and-data-fetching/README.md#99-query-keys-pagination-and-dependent-queries)

**R**

- **Race condition (fetch)** — A slower, older response arriving last and overwriting newer data; fixed with an ignore flag or `AbortController` in cleanup. [§5.7](../05-effects-and-refs/README.md#57-fetching-data-in-effects-and-race-conditions), [§7.6](../07-custom-hooks-and-hook-rules/README.md#76-usefetch--loading-error-success-and-no-races)
- **React Compiler** — Build-time Babel plugin (1.0 since October 2025) that inserts memoization automatically. [§10.7](../10-performance/README.md#107-react-compiler)
- **React DevTools** — Browser extension to inspect the component tree, props, state and profile renders. [§10.3](../10-performance/README.md#103-measure-first-devtools-profiler-and-profiler), [§1.5](../01-why-react-and-setup/README.md#15-setting-up-a-react-project)
- **React element** — A plain immutable object (`{ type, props, key }`) describing a piece of UI; what JSX creates. [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief), [§1.8](../01-why-react-and-setup/README.md#18-jsx)
- **React Hook Form** — Form library that manages big forms with uncontrolled inputs and refs. [§4.9](../04-forms-and-actions/README.md#49-form-libraries--react-hook-form-and-zod-overview)
- **React Router** — The most used React router; v8 has declarative, data and framework modes and ships as one `react-router` package. [§9.2](../09-routing-and-data-fetching/README.md#92-react-router-v8-and-its-three-modes)
- **Reactive value** — Anything declared in the component body that can change between renders: props, state, and values computed from them. [§5.2](../05-effects-and-refs/README.md#52-useeffect-basics), [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth)
- **Reconciliation** — Comparing the new element tree with the previous one to find the minimal DOM changes. [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)
- **Reducer** — A pure function `(state, action) => nextState`. [§6.2](../06-state-management/README.md#62-usereducer--consolidate-update-logic)
- **Redux Toolkit (RTK)** — The official way to write Redux: `createSlice`, `configureStore`, `createAsyncThunk`, RTK Query. [§6.7](../06-state-management/README.md#67-redux-toolkit)
- **Ref** — A `{ current }` object from `useRef` that persists across renders; changing it doesn't re-render. [§5.8](../05-effects-and-refs/README.md#58-useref-for-values)
- **Ref callback** — A function passed as `ref`; called with the node on attach; in React 19 may return a cleanup. [§5.9](../05-effects-and-refs/README.md#59-refs-to-dom-nodes)
- **Render (phase)** — React calling component functions to compute the next element tree; must be pure; may repeat or be discarded. [§10.1](../10-performance/README.md#101-the-render-cycle-trigger--render--commit), [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)
- **Render prop** — A prop whose value is a function returning JSX, called by the component to decide what to render. [§14.5](../14-patterns-and-architecture/README.md#145-render-props)
- **`renderHook`** — RTL helper that runs a Hook inside a hidden test component; read `result.current`. [§12.7](../12-testing-react/README.md#127-testing-custom-hooks)
- **`renderToPipeableStream`** — `react-dom/server` API for streaming SSR on Node; `onShellReady`, `onAllReady`, `abort()`. [§13.2](../13-server-rendering-and-frameworks/README.md#132-the-server-rendering-apis), [§13.3](../13-server-rendering-and-frameworks/README.md#133-streaming-ssr-with-suspense)
- **`renderToString`** — Legacy SSR API returning an HTML string; can't wait for Suspense data. [§13.2](../13-server-rendering-and-frameworks/README.md#132-the-server-rendering-apis)
- **Reset keys** — Values that, when changed, clear an error boundary's error state. [§14.9](../14-patterns-and-architecture/README.md#149-error-boundaries)
- **Revalidation** — Re-running loaders (React Router) or refetching/regenerating cached data so the UI shows fresh data. [§9.5](../09-routing-and-data-fetching/README.md#95-loaders--data-before-render-data-mode), [§13.9](../13-server-rendering-and-frameworks/README.md#139-nextjs-app-router-overview)
- **Roving tabindex** — Only the active item in a group has `tabIndex=0`; arrow keys move focus within the group. [§14.4](../14-patterns-and-architecture/README.md#144-compound-components)
- **RSC payload** — The serialized output of Server Components (rendered elements + client references + props) sent to the browser. [§13.7](../13-server-rendering-and-frameworks/README.md#137-react-server-components)
- **Rules of Hooks** — Call Hooks only at the top level of components/custom Hooks, and only from React functions. [§7.2](../07-custom-hooks-and-hook-rules/README.md#72-the-rules-of-hooks)
- **Rules of React** — Components and Hooks are pure, props/state are immutable, Hooks follow their rules — assumptions the React Compiler relies on. [§10.7](../10-performance/README.md#107-react-compiler), [§2.5](../02-components-and-props/README.md#25-pure-components)

**S**

- **Search params** — The `?key=value` part of a URL; read/write with `useSearchParams()` for shareable UI state. [§9.3](../09-routing-and-data-fetching/README.md#93-routes-links-and-url-parameters-declarative-mode)
- **Selective hydration** — With streaming, React hydrates boundaries independently and prioritises one the user interacts with. [§13.4](../13-server-rendering-and-frameworks/README.md#134-hydration-end-to-end)
- **Selector** — A function that reads part of a store (`state => state.cart.total`); components re-render only when its result changes. [§6.7](../06-state-management/README.md#67-redux-toolkit), [§6.8](../06-state-management/README.md#68-zustand)
- **Server Component** — A component that runs only on the server, can be `async`, and ships no JavaScript; can't create or read context, but since 19.3 may render a provider imported from a `'use client'` module. [§13.7](../13-server-rendering-and-frameworks/README.md#137-react-server-components)
- **Server Function** — An async function marked `'use server'` that runs on the server but can be called from the client; a public endpoint. [§13.8](../13-server-rendering-and-frameworks/README.md#138-directives-use-client-and-use-server), [§4.8](../04-forms-and-actions/README.md#48-server-functions-and-progressive-enhancement-overview)
- **Server state** — A client-side cache of data owned by a server; needs fetching, caching, invalidation — a TanStack Query job. [§6.1](../06-state-management/README.md#61-kinds-of-state--and-where-each-belongs), [§6.9](../06-state-management/README.md#69-tanstack-query--server-state)
- **Shallow comparison** — Comparing each top-level property with `Object.is`, as `memo` does with props. [§10.4](../10-performance/README.md#104-memo--skipping-a-childs-re-render)
- **Shell (streaming)** — Everything outside pending Suspense boundaries, sent first with fallbacks in place. [§13.3](../13-server-rendering-and-frameworks/README.md#133-streaming-ssr-with-suspense)
- **Side effect** — Anything code does besides computing its return value: DOM changes, requests, timers, logging. [§5.1](../05-effects-and-refs/README.md#51-where-side-effects-belong), [§2.5](../02-components-and-props/README.md#25-pure-components)
- **Skeleton** — Grey placeholder shapes in the final layout shown during a first load; avoids layout shift. [§9.12](../09-routing-and-data-fetching/README.md#912-loading-and-error-states-that-users-like)
- **Slot** — A named prop that receives JSX (`header={…}`), next to `children`. [§2.4](../02-components-and-props/README.md#24-composition), [§14.1](../14-patterns-and-architecture/README.md#141-composition-first)
- **Snapshot (state)** — The fixed state values of one render; setters request a new render but never change the variable you hold. [§3.3](../03-state-and-events/README.md#33-state-as-a-snapshot)
- **SPA** — Single-page application: one HTML page; JavaScript renders screens and handles navigation. [§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg), [§9.1](../09-routing-and-data-fetching/README.md#91-client-side-routing)
- **SSG** — Static site generation: HTML produced once at build time and served from a CDN. [§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg), [§13.1](../13-server-rendering-and-frameworks/README.md#131-rendering-strategies-csr-ssr-ssg-isr-streaming-ppr)
- **SSR** — Server-side rendering: HTML rendered per request on the server, then hydrated. [§1.4](../01-why-react-and-setup/README.md#14-spa-vs-mpa-vs-ssr-and-ssg), [§13.1](../13-server-rendering-and-frameworks/README.md#131-rendering-strategies-csr-ssr-ssg-isr-streaming-ppr)
- **Stale closure** — A function that keeps reading values from an old render (missing dependency, or a timer set up once). [§3.3](../03-state-and-events/README.md#33-state-as-a-snapshot), [§7.7](../07-custom-hooks-and-hook-rules/README.md#77-hooks-that-take-callbacks--useeffectevent)
- **Stale-while-revalidate** — Serve cached data now and refresh it in the background (TanStack Query, ISR, HTTP caches). [§6.9](../06-state-management/README.md#69-tanstack-query--server-state), [§13.1](../13-server-rendering-and-frameworks/README.md#131-rendering-strategies-csr-ssr-ssg-isr-streaming-ppr)
- **`staleTime`** — TanStack Query: how long fetched data counts as fresh (default 0). [§6.9](../06-state-management/README.md#69-tanstack-query--server-state), [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics)
- **State** — Data a component remembers between renders that changes over time; setting it schedules a re-render. [§3.2](../03-state-and-events/README.md#32-state-a-components-memory--usestate)
- **State updater** — A function passed to a setter, `setN(n => n + 1)`, computed from the queued value. [§3.5](../03-state-and-events/README.md#35-updater-functions--queueing-updates)
- **Streaming SSR** — Sending HTML in chunks as each Suspense boundary resolves, with inline swap scripts. [§13.3](../13-server-rendering-and-frameworks/README.md#133-streaming-ssr-with-suspense)
- **`StrictMode`** — Development-only checks you opt into by wrapping the app (Vite and most templates do): renders twice, runs Effects setup→cleanup→setup on mount, re-runs ref callbacks. [§5.4](../05-effects-and-refs/README.md#54-strictmodes-double-run-development-only), [§2.5](../02-components-and-props/README.md#25-pure-components), [§1.6](../01-why-react-and-setup/README.md#16-project-structure)
- **Structural sharing** — Reusing unchanged parts of an old object in a new version (Immer, TanStack Query). [§3.6](../03-state-and-events/README.md#36-updating-objects-and-arrays-immutably)
- **`suppressHydrationWarning`** — Silences a hydration mismatch warning for one element's own text/attributes; keeps the server value. [§13.5](../13-server-rendering-and-frameworks/README.md#135-hydration-mismatches)
- **Suspense** — `<Suspense fallback>` shows a placeholder while something inside (lazy code, `use(promise)`, suspense-enabled data) isn't ready. [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use), [§10.8](../10-performance/README.md#108-code-splitting-with-lazy-and-suspense)
- **Synthetic event** — React's cross-browser wrapper around a native event, with the same interface; `e.nativeEvent` gives the original. [§3.1](../03-state-and-events/README.md#31-responding-to-events)

**T**

- **Tailwind CSS** — Utility-first CSS framework; generates only the classes it finds in your source; v4 configures with `@theme`. [§8.6](../08-styling/README.md#86-tailwind-css)
- **TanStack Query** — Server-state library: caching, de-duplication, background refetch, retries, mutations and invalidation. [§6.9](../06-state-management/README.md#69-tanstack-query--server-state), [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics)
- **TanStack Start** — Full-stack React framework built on TanStack Router (type-safe routes, server functions, streaming); v1 release candidate, RSC experimental. [§13.11](../13-server-rendering-and-frameworks/README.md#1311-choosing-a-framework)
- **Tearing** — Different parts of the UI showing different versions of the same external data in one render; prevented by `useSyncExternalStore`. [§7.9](../07-custom-hooks-and-hook-rules/README.md#79-usesyncexternalstore--subscribing-to-outside-data)
- **Thunk** — A function dispatched to a Redux store for async logic; `createAsyncThunk` dispatches pending/fulfilled/rejected. [§6.7](../06-state-management/README.md#67-redux-toolkit)
- **Transition** — A non-urgent state update (`startTransition`) React can render in the background and interrupt; keeps old UI during suspense. [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates), [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use)
- **Tree-shaking** — Removing unused exports from a bundle at build time. [§14.11](../14-patterns-and-architecture/README.md#1411-folder-structure), [§10.10](../10-performance/README.md#1010-common-performance-bugs)

**U**

- **Uncontrolled input** — A form field whose value lives in the DOM (`defaultValue`), read with `FormData` or a ref. [§4.2](../04-forms-and-actions/README.md#42-controlled-vs-uncontrolled-inputs)
- **`use`** — React 19 API that reads a promise (suspending) or a context; may be called conditionally, not in `try/catch`. [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use), [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props)
- **`'use cache'`** — Next.js directive (not React's) that caches a function or component's output under Cache Components. [§13.9](../13-server-rendering-and-frameworks/README.md#139-nextjs-app-router-overview)
- **`'use client'`** — Directive marking the entry of client code: that module and its imports ship to the browser. [§13.8](../13-server-rendering-and-frameworks/README.md#138-directives-use-client-and-use-server)
- **`'use no memo'`** — Directive that opts one component or file out of the React Compiler. [§10.7](../10-performance/README.md#107-react-compiler)
- **`'use server'`** — Directive marking Server Functions (a file's exports, or one async function); not a marker for Server Components. [§13.8](../13-server-rendering-and-frameworks/README.md#138-directives-use-client-and-use-server)
- **`useActionState`** — Hook returning `[state, formAction, isPending]`; the action gets `(prevState, payload)` and its return value becomes state. [§4.5](../04-forms-and-actions/README.md#45-useactionstate--the-result-of-the-last-submission)
- **`useCallback`** — Caches a function between renders until a dependency changes; `useMemo(() => fn, deps)`. [§10.5](../10-performance/README.md#105-usecallback-and-usememo-for-stable-props)
- **`useContext`** — Reads the nearest provider's value of a context (or the default); a Hook, top level only. [§6.3](../06-state-management/README.md#63-context--pass-data-deeply-without-props)
- **`useDeferredValue`** — Returns a lagging copy of a value so a slow `memo` child renders in the background; React 19 adds `initialValue`. [§7.11](../07-custom-hooks-and-hook-rules/README.md#711-usedeferredvalue--let-part-of-the-ui-lag-behind)
- **`useEffect`** — Runs setup after commit (normally after paint) and cleanup before the next run/unmount; for external systems. [§5.2](../05-effects-and-refs/README.md#52-useeffect-basics)
- **`useEffectEvent`** — React 19.2 Hook creating an Effect Event: latest values inside Effects without re-running them. [§7.7](../07-custom-hooks-and-hook-rules/README.md#77-hooks-that-take-callbacks--useeffectevent), [§5.5](../05-effects-and-refs/README.md#55-dependencies-in-depth)
- **`useFormStatus`** — `react-dom` Hook for a component inside a `<form>`: `{ pending, data, method, action }` of that form. [§4.6](../04-forms-and-actions/README.md#46-useformstatus--status-of-the-parent-form)
- **`useId`** — Unique, SSR-stable ID per component instance for `htmlFor`/`aria-*`; never for keys. [§7.8](../07-custom-hooks-and-hook-rules/README.md#78-useid--ids-for-accessibility)
- **`useImperativeHandle`** — Exposes a custom, limited object through a ref instead of the raw DOM node. [§5.10](../05-effects-and-refs/README.md#510-passing-refs-to-your-own-components)
- **`useInsertionEffect`** — Runs before layout effects; for CSS-in-JS libraries to insert `<style>` tags. [§5.11](../05-effects-and-refs/README.md#511-uselayouteffect--before-the-browser-paints), [§8.7](../08-styling/README.md#87-css-in-js)
- **`useLayoutEffect`** — Runs after DOM updates but before paint; for measuring and positioning without flicker; blocks paint. [§5.11](../05-effects-and-refs/README.md#511-uselayouteffect--before-the-browser-paints)
- **`useLoaderData`** — React Router Hook that returns the current route's loader data. [§9.5](../09-routing-and-data-fetching/README.md#95-loaders--data-before-render-data-mode)
- **`useMemo`** — Caches a computed value until a dependency changes; for expensive calculations (~1 ms+) and stable identities. [§10.6](../10-performance/README.md#106-usememo-for-expensive-calculations), [§10.5](../10-performance/README.md#105-usecallback-and-usememo-for-stable-props)
- **`useMutation`** — TanStack Query Hook for writes: `mutate`, `isPending`, `onMutate`/`onError`/`onSettled`. [§9.10](../09-routing-and-data-fetching/README.md#910-mutations-invalidation-and-optimistic-updates)
- **`useNavigate`** — React Router Hook returning a function to navigate in code (`navigate('/x')`, `navigate(-1)`). [§9.3](../09-routing-and-data-fetching/README.md#93-routes-links-and-url-parameters-declarative-mode)
- **`useNavigation`** — React Router data-mode Hook: `state` is `'idle'`, `'loading'` or `'submitting'`. [§9.5](../09-routing-and-data-fetching/README.md#95-loaders--data-before-render-data-mode)
- **`useOptimistic`** — Hook showing an optimistic value while an Action runs; falls back to the real value when it ends. [§4.7](../04-forms-and-actions/README.md#47-useoptimistic--instant-feedback-automatic-rollback)
- **`useParams`** — React Router Hook returning the URL's dynamic segments as strings. [§9.3](../09-routing-and-data-fetching/README.md#93-routes-links-and-url-parameters-declarative-mode)
- **`useQuery`** — TanStack Query Hook: `{ data, status, isPending, isFetching, error }` for a key + function. [§9.8](../09-routing-and-data-fetching/README.md#98-tanstack-query-basics), [§6.9](../06-state-management/README.md#69-tanstack-query--server-state)
- **user-event** — `@testing-library/user-event`: simulates full user interactions (focus, keys, pointer) — async, `await` each call. [§12.4](../12-testing-react/README.md#124-user-event--interacting-like-a-user)
- **`useReducer`** — Hook returning `[state, dispatch]` for a pure reducer; for complex or related state updates. [§6.2](../06-state-management/README.md#62-usereducer--consolidate-update-logic)
- **`useRef`** — Hook returning a persistent `{ current }` object; for DOM nodes and values that don't affect rendering. [§5.8](../05-effects-and-refs/README.md#58-useref-for-values)
- **`useSearchParams`** — React Router Hook that reads and writes `?query` params like `useState`. [§9.3](../09-routing-and-data-fetching/README.md#93-routes-links-and-url-parameters-declarative-mode)
- **`useState`** — Hook returning `[value, setValue]`; the initial value is used on the first render only. [§3.2](../03-state-and-events/README.md#32-state-a-components-memory--usestate)
- **`useSuspenseQuery`** — TanStack Query Hook that suspends instead of returning a pending state; `data` is always defined. [§9.11](../09-routing-and-data-fetching/README.md#911-suspense-and-use)
- **`useSyncExternalStore`** — Subscribes to an external store: `(subscribe, getSnapshot, getServerSnapshot?)`; snapshot must be cached. [§7.9](../07-custom-hooks-and-hook-rules/README.md#79-usesyncexternalstore--subscribing-to-outside-data)
- **`useTransition`** — Returns `[isPending, startTransition]`; marks updates as non-urgent; async callbacks are Actions in React 19. [§7.10](../07-custom-hooks-and-hook-rules/README.md#710-usetransition--non-urgent-updates)
- **Utility-first CSS** — Styling by composing single-purpose classes in markup (Tailwind). [§8.6](../08-styling/README.md#86-tailwind-css)

**V**

- **Virtual DOM** — Informal name for React's in-memory element tree that is diffed against the previous one. [§1.3](../01-why-react-and-setup/README.md#13-rendering-the-virtual-dom-and-reconciliation-brief)
- **Virtualization (windowing)** — Rendering only the visible rows of a long list plus overscan inside a full-height spacer. [§10.9](../10-performance/README.md#109-list-virtualization)
- **Vite** — Build tool and dev server with native-ES-module dev serving and HMR; common for React SPAs. Vite 8 strips types/JSX with Oxc (earlier versions: esbuild). [§1.5](../01-why-react-and-setup/README.md#15-setting-up-a-react-project), [§11.1](../11-typescript-with-react/README.md#111-setup-and-how-type-checking-fits-in)

**W**

- **Waterfall** — Requests that start one after another because each waits for the previous render (fetch-in-Effect chains). [§9.5](../09-routing-and-data-fetching/README.md#95-loaders--data-before-render-data-mode), [§5.7](../05-effects-and-refs/README.md#57-fetching-data-in-effects-and-race-conditions)

**Z**

- **Zod** — Schema library: describe data once, `parse`/`safeParse` it at runtime and infer TypeScript types. [§4.9](../04-forms-and-actions/README.md#49-form-libraries--react-hook-form-and-zod-overview)
- **Zustand** — Small store library: `create((set, get) => …)` returns a Hook read with selectors; no provider. [§6.8](../06-state-management/README.md#68-zustand)

---

## Chapter 16 TL;DR

| Sheet | The one line to remember |
|---|---|
| JSX | One root, closed tags, camelCase attributes, `{expressions}`, `style` objects; `0 &&` renders 0; objects can't be children |
| Components | Capitalised, top-level, pure; props read-only; composition over configuration; stable keys |
| Hooks | Top level, same order; `use` is the exception; 19 added `use`, `useActionState`, `useFormStatus`, `useOptimistic`; 19.2 added `useEffectEvent` |
| State | Snapshot per render; updaters for queued changes; batched everywhere; never mutate; position + key decide identity |
| Lifecycle | Trigger → render (pure) → commit → layout effects → paint → Effects; children's Effects before parents' |
| Effects | Only for external systems; cleanup mirrors setup; StrictMode double run in dev; derive, key or handle instead where possible |
| Refs | Mutable box, no re-render, not during render; `ref` is a prop in 19 |
| Forms | Uncontrolled + form Action + `useActionState` by default; controlled when every keystroke matters; validate on the server |
| State homes | Local → lifted → context (rare changes) → Zustand/RTK (busy); server data → TanStack Query; shareable → URL |
| Data & routing | Loaders start fetching early; the query cache avoids refetching; transitions keep old UI |
| Performance | Measure, fix structure, then memoize (or the Compiler), split code, virtualize |
| Server | SSR/SSG/ISR/streaming/PPR per route; RSC ships no JS; `'use client'` marks the boundary, `'use server'` marks Server Functions |
| Testing | Roles and names, `user-event`, `findBy`, mock the network, fresh providers per test |
| Errors | Read the message, find it in §16.18, follow the link |

This is the end of the React Handbook. Next: build something (the plan in [§16.20](#1620-where-to-go-next)) — and when you forget a detail, come back to these sheets. Then continue with the [LLD Handbook's front-end machine-coding case](../../../lld/chapters/21-case-machine-coding/README.md), the [HTML & CSS Handbook](../../../html-css/chapters/01-how-the-web-works/README.md), or go back to the [book's front page](../../README.md) or [Chapter 1](../01-why-react-and-setup/README.md) for a second pass.
