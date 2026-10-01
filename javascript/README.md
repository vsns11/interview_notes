# JavaScript Handbook

One place to learn JavaScript from your first line of code to confident intermediate work in the browser and in Node.js, and to prepare for JavaScript interviews, without switching between platforms. Each topic combines, in one explanation:
- **the DesignGurus course** you own, *Grokking JavaScript Fundamentals*,
- **classic books**: *You Don't Know JS Yet* (Kyle Simpson) and *Eloquent JavaScript* (Marijn Haverbeke), plus javascript.info,
- **official documentation**: MDN Web Docs, the ECMAScript specification and TC39 proposals (tc39.es), the WHATWG DOM/HTML/Fetch standards, the Node.js docs, used to fill gaps and check facts,
- **real-world examples** from browsers, Node tooling and the products you use.

Every chapter is written for a first-time learner and defines each term the first time it appears. Every code example is a runnable program (Node 24; browser examples run under jsdom or are screenshotted in headless Chrome) whose **real output** appears under it. Every chapter was independently reviewed for accuracy.

> Personal study notes. Course-derived material is paraphrased in my own words, not copied. Not for redistribution.

## Who it's for
| Reader | What you get |
|---|---|
| Complete beginners | A first language taught from zero (Ch 1–8), every term defined |
| Developers coming from Java, Python, C#… | The JavaScript way of doing familiar things, and the traps that differ: coercion, `this`, prototypes, the event loop |
| Front-end developers | The language under React and every framework, then the DOM, events, browser APIs and web security (Ch 13–14) |
| Node / back-end developers | Modules and npm, async I/O, core modules, streams, an HTTP server, testing and TypeScript (Ch 10–12, 15–16) |
| Interview candidates | DSA patterns in JS, implement-from-scratch classics, 53 tricky questions (Ch 17), and cheat sheets plus a 353-term glossary (Ch 18) |

## How to use it
Every topic follows the same rhythm: *what it is → why it matters → how it works → code with real output → trade-offs → common mistakes → real-world use → in the interview → self-check → TL;DR*. To review, skim the TL;DRs. To check you really learned a topic, answer the self-check questions without looking back.

**Beginner path (read in order):** 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8, then 9 → 10 → 11 → 12, then pick the front-end or Node path below. Type the examples yourself and run them. Keep Chapter 18 open as a quick reference.

**Front-end developer path:**
1. Core language: Ch 2, 4, 5, 7 (coercion, closures, objects and copying, immutable array updates).
2. `this` and classes (Ch 9), modules and bundlers (Ch 10), errors and DevTools (Ch 11).
3. **Ch 12** (event loop, promises, `async`/`await`, `AbortController`), then **Ch 13** (DOM, events, delegation, forms, layout thrashing) and **Ch 14** (`fetch`, CORS, storage, workers, observers, XSS/CSRF/CSP).
4. Continue with the [HTML & CSS Handbook](../html-css/chapters/01-how-the-web-works/README.md) and the [React Handbook](../react/chapters/01-why-react-and-setup/README.md).

**Node / back-end path:**
1. Core language: Ch 2, 4, 5, 7, 8 (Maps, Sets, iterators and generators).
2. **Ch 10** (ES modules vs CommonJS, npm, semver, lockfiles), **Ch 11** (custom errors, `cause`, `using`, the Node inspector).
3. **Ch 12** (with §12.4: `nextTick`, `setImmediate` and Node's loop phases), then **Ch 15** (files, paths, `process`, `Buffer`, `EventEmitter`, streams, `node:http`, child processes).
4. **Ch 16**: `node:test`, mocking and fake timers, TypeScript and Node's type stripping. Then the [SQL Handbook](../sql/chapters/01-databases-and-the-relational-model/README.md).

**Interview-prep path (you already write some JavaScript):**
1. Skim the TL;DRs of Ch 2, 4, 5, 9 and 12 to fill gaps (coercion, closures, copying, `this` and prototypes, the event loop).
2. Work through the ordering puzzles in **§12.3** until you can predict every line.
3. Work through **Ch 17**: the DSA toolkit and twenty-one patterns, then re-type each classic (debounce, throttle, curry, memoize, deep clone, LRU cache, `Promise.all` polyfill) from memory and run it.
4. Read the 53 questions in **§17.4** and practise saying the 30-second answers out loud.
5. The night before, review with **Ch 18** (cheat sheets, "which API for which job", the interview-question index).

```mermaid
flowchart LR
    A["Part I - Language basics: Ch 1-4"] --> B["Part II - Working with data: Ch 5-8"]
    B --> C["Part III - Objects, modules, errors: Ch 9-11"]
    C --> D["Part IV - Async and the platforms: Ch 12-15"]
    D --> E["Part V - Quality, interviews, revision: Ch 16-18"]
```

## What's in here
| Path | Purpose |
|---|---|
| `JavaScript-Handbook.pdf` | The whole book as one PDF, with a cover and a table of contents. |
| `interview/JavaScript-Interview-Edition.pdf` + `interview/INTERVIEW.md` | **Interview Edition** — a small (~40-page) fast-revision book: every chapter as must-know terms, key tables, tiny code snippets, likely questions and gotchas, plus a Top-25 question list, the event-loop ordering rules with three output puzzles, the from-scratch classics and a last-minute checklist. |
| `capstones/CAPSTONES.md` + `capstones/JavaScript-Capstone-Projects.pdf` | **Capstone Projects**: 40 build-it-yourself design briefs, beginner to advanced — vanilla JavaScript and Node 24, from a calculator and a localStorage to-do list to a WebSocket chat, an offline-first PWA, a Promises/A+ library, a bundler, a CRDT editor and an HTTP server on raw TCP — each with requirements, architecture and data-model diagrams, milestones, hard parts, testing, trade-offs and interview questions. |
| `chapters/NN-name/README.md` + `ChapterNN-Notes.pdf` | One chapter (Markdown source and its PDF). |
| `chapters/NN-name/code/` | Runnable examples (`.js`, `.mjs`, `.ts`, test files) and, for browser chapters, small self-contained `.html` pages. |
| `chapters/NN-name/img/` | Screenshots of those `.html` pages rendered in headless Chrome (Ch 1, 12, 13, 14). |
| `tools/` | `regen_pdfs.sh` (rebuild all PDFs) and `md2pdf.mjs` (Markdown → PDF). See [tools/README.md](tools/README.md). |

## Chapters
| # | Chapter | In one line | PDF |
|---|---|---|---|
| **Part I — Language basics** | | | |
| 01 | [Introduction and Setup](chapters/01-introduction-and-setup/README.md) | What JavaScript is, ECMAScript and TC39, engines and JIT, hosts (browser, Node, Deno, Bun), running code, basic rules, comments, strict mode | `Chapter01-Notes.pdf` |
| 02 | [Variables, Types and Operators](chapters/02-variables-types-and-operators/README.md) | `let`/`const`/`var`, hoisting and the TDZ, the eight types, primitives vs objects, operators, `==` vs `===`, `??` and `?.`, numbers, truthiness, conversions | `Chapter02-Notes.pdf` |
| 03 | [Control Flow and Loops](chapters/03-control-flow-and-loops/README.md) | `if`/`else`, the ternary, `switch`, `for`/`while`/`do…while`, `for…in` vs `for…of`, `break`/`continue`/labels, choosing a loop | `Chapter03-Notes.pdf` |
| 04 | [Functions, Scope and Closures](chapters/04-functions-scope-and-closures/README.md) | Declarations vs expressions, arrows, defaults/rest/spread, scope and hoisting, closures, higher-order functions, IIFEs, recursion, pure functions | `Chapter04-Notes.pdf` |
| **Part II — Working with data** | | | |
| 05 | [Objects, Math, Date and JSON](chapters/05-objects-math-date-and-json/README.md) | Properties, methods, spread and copying, destructuring, descriptors and freezing, getters/setters, built-ins, `Math`, `Date` and Temporal, JSON | `Chapter05-Notes.pdf` |
| 06 | [Strings and Regular Expressions](chapters/06-strings-and-regular-expressions/README.md) | Template literals, immutability, Unicode (code units, code points, graphemes), string methods, the regex language, regex methods and recipes | `Chapter06-Notes.pdf` |
| 07 | [Arrays](chapters/07-arrays/README.md) | Creating and changing arrays, mutating vs non-mutating methods, `map`/`filter`/`reduce`, sorting correctly, destructuring, arrays vs objects vs Map/Set, typed arrays | `Chapter07-Notes.pdf` |
| 08 | [Maps, Sets, Iterators and Generators](chapters/08-maps-sets-iterators-and-generators/README.md) | `Map`, `Set` and set algebra, weak collections, the iteration protocol, generators, iterator helpers, `Object.groupBy` | `Chapter08-Notes.pdf` |
| **Part III — Objects, modules and errors** | | | |
| 09 | [`this`, Prototypes and Classes](chapters/09-this-prototypes-and-classes/README.md) | How `this` is decided, `call`/`apply`/`bind`, the prototype chain, `new`, classes and `#private`, `extends`/`super`, type checks, mixins, composition | `Chapter09-Notes.pdf` |
| 10 | [Modules and Tooling](chapters/10-modules-and-tooling/README.md) | `import`/`export`, how ES modules load, dynamic `import()`, top-level `await`, CommonJS vs ESM, npm and semver, bundlers, transpilers, linters | `Chapter10-Notes.pdf` |
| 11 | [Errors and Debugging](chapters/11-errors-and-debugging/README.md) | Built-in error types, `try`/`catch`/`finally`, custom errors, `cause`, async errors, `using`, the console, stack traces and source maps, DevTools | `Chapter11-Notes.pdf` |
| **Part IV — Async and the platforms** | | | |
| 12 | [Asynchronous JavaScript](chapters/12-asynchronous-javascript/README.md) | The event loop, ordering puzzles, Node's loop, timers, callbacks, promises and combinators, `async`/`await`, cancellation, async iteration, real-world patterns | `Chapter12-Notes.pdf` |
| 13 | [The DOM and Events](chapters/13-dom-and-events/README.md) | The DOM tree, selecting and changing nodes, attributes vs properties, events and propagation, delegation, forms, `MutationObserver`, reflow, script timing | `Chapter13-Notes.pdf` |
| 14 | [Browser APIs and Security](chapters/14-browser-apis-and-security/README.md) | `fetch`, same-origin policy and CORS, storage, `requestAnimationFrame`, `URL`, History API, Web Workers, observers, XSS, CSRF, CSP | `Chapter14-Notes.pdf` |
| 15 | [Node.js Basics](chapters/15-nodejs-basics/README.md) | V8 + libuv, the thread pool, files and paths, `process`, `Buffer`, `EventEmitter`, streams and backpressure, an HTTP server, child processes, Node vs Deno vs Bun | `Chapter15-Notes.pdf` |
| **Part V — Quality, interviews and revision** | | | |
| 16 | [Testing and TypeScript](chapters/16-testing-and-typescript/README.md) | `node:assert/strict`, `node:test`, async tests, mocks and fake timers, snapshots and coverage; TypeScript types, narrowing, generics, `satisfies`, type stripping, JSDoc | `Chapter16-Notes.pdf` |
| 17 | [JavaScript for Interviews](chapters/17-javascript-for-interviews/README.md) | The DSA toolkit and its complexities, twenty-one coding patterns, implement-from-scratch classics, 53 tricky questions answered with real output | `Chapter17-Notes.pdf` |
| 18 | [Cheat Sheets and Glossary](chapters/18-cheatsheet-and-glossary/README.md) | One sheet per area (values machine-checked), "which API for which job", ES2015→ES2026 version table, study plan, question index, 353-term glossary | `Chapter18-Notes.pdf` |

Each chapter's PDF sits next to its README: `chapters/NN-name/ChapterNN-Notes.pdf`.

## Colour code (in the PDFs)
Section headings are coloured by their role, so you can scan a page for the part you need:

| Colour | Headings |
|---|---|
| Green | **What it is**, analogies, overviews |
| Red / coral | **The problem it solves** |
| Blue | **How it works**, walkthroughs |
| Amber | **Trade-offs**, comparisons, **When not to use it** |
| Bright red | **Common mistakes**, pitfalls |
| Violet | **Real-world examples** |
| Pink | **In the interview**, 30-second answers (boxed), follow-ups |
| Teal | **Self-check** |
| Yellow | **TL;DR** (boxed) |

Code blocks are syntax-highlighted, and Mermaid diagrams are rendered as images.

## Running the code
The examples need Node 24 (written and checked on v24.11). Shared helpers live one level up, in the notes root: `tools_run_js.sh`, `tools_dom.mjs`, `tools_web_shot.mjs`, and the npm packages in `.web-books/` (jsdom, esbuild, typescript and the React/testing libraries). No example needs network access or API keys.

```bash
# from javascript/ — run every example in the book (prints OK / FAIL / SKIP per file)
bash ../tools_run_js.sh javascript

# run one example (from its code/ folder, since some examples read or write files next to themselves)
cd chapters/12-asynchronous-javascript/code
node 02_ordering_puzzles.mjs
```
- **What the runner does:** it runs every `code/*.{js,mjs,ts,tsx,jsx}` file; `.ts`/`.tsx`/`.jsx` are bundled with esbuild first. Files marked `// tools_run: skip` are not standalone programs (modules imported by other examples, browser-only scripts, TypeScript files with deliberate type errors) and are reported as SKIP. At the last full run: 147 OK, 21 SKIP, 0 FAIL.
- **DOM examples under Node:** files whose first line is `import '../../../../tools_dom.mjs';` get a browser-like global `document`, `window`, events, `localStorage` and `MutationObserver` from jsdom, and `console.log` what a user would see.
- **Screenshots:** browser demos that need a real engine (layout, workers, CSP, IndexedDB, observers) are small self-contained `code/*.html` pages, rendered to `img/*.png` with headless Chrome. Re-shoot one from `javascript/` with, for example:
  ```bash
  node ../tools_web_shot.mjs chapters/13-dom-and-events/code/propagation.html chapters/13-dom-and-events/img/propagation.png 760
  ```
  The third argument is the viewport width in CSS pixels (the PNG is twice that, at 2× scale). Pages are shot in the light colour scheme unless you add `--dark`; `--wait=ms` waits for timers before the shot.

Timings in the outputs (sequential vs parallel `await`, worker demos) depend on the machine; your numbers will differ, but the pattern should not. The Chapter 18 cheat-sheet script feature-detects ES2026/ES2027 APIs, so it reports which ones your Node version lacks.

## Rebuilding the PDFs
Requires Node 18+ and Google Chrome (the path is set in `tools/md2pdf.mjs`).

```bash
bash tools/regen_pdfs.sh      # every ChapterNN-Notes.pdf + JavaScript-Handbook.pdf (cover title "JavaScript Handbook")

# one chapter only
node tools/md2pdf.mjs chapters/12-asynchronous-javascript/Chapter12-Notes.pdf chapters/12-asynchronous-javascript/README.md
```
`md2pdf.mjs` reports any Mermaid errors or broken images.

## Sources
- **Course:** DesignGurus, *Grokking JavaScript Fundamentals*. Its concepts, examples and exercises are covered in my own words; no course text is reproduced.
- **Books:** Kyle Simpson, *You Don't Know JS Yet* (scope and closures, `this`, types); Marijn Haverbeke, *Eloquent JavaScript*; javascript.info as a cross-check.
- **Documentation:** MDN Web Docs; the ECMAScript Language Specification (ECMA-262, tc39.es/ecma262) and TC39's proposals and finished-proposals lists (for every "ES20xx" label); the WHATWG DOM, HTML and Fetch standards; the Node.js documentation and release notes (nodejs.org); the TypeScript documentation; OWASP guidance for the security sections.
- **Web:** used to check facts and version details (which feature landed in which edition, what Node 24 ships, browser support).

## Companion books
| Book | Read it for |
|---|---|
| [React Handbook](../react/chapters/01-why-react-and-setup/README.md) | Components, state, effects, routing, data fetching and testing — built on Ch 4, 5, 7, 12 and 13 of this book |
| [HTML & CSS Handbook](../html-css/chapters/01-how-the-web-works/README.md) | Semantic HTML, accessibility, forms, layout (flexbox, grid) and responsive design — the page your JavaScript manipulates |
| [Python Handbook](../python/README.md) | A second language with a different model (strong typing, the GIL, generators) — useful for comparison |
| [LLD Handbook](../lld/README.md) | Object-oriented design, SOLID, design patterns and machine-coding case studies (in Java 21) for design interviews |
