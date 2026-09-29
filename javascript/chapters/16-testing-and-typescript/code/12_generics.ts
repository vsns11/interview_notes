// Generics: write code once, keep types precise; plus the built-in utility types

// 1) Generic function: T is inferred from the argument
function first<T>(items: readonly T[]): T | undefined {
  return items[0];
}
const n = first([10, 20]);          // number | undefined
const s = first(['a', 'b']);        // string | undefined
console.log('1)', n, s);

// 2) Constraints: T must have a length
function longest<T extends { length: number }>(a: T, b: T): T {
  return a.length >= b.length ? a : b;
}
console.log('2)', longest('apple', 'fig'), longest([1, 2], [1, 2, 3]));

// 3) keyof + indexed access: a type-safe property getter
function pluck<T, K extends keyof T>(rows: T[], key: K): T[K][] {
  return rows.map((r) => r[key]);
}
const users = [{ id: 1, name: 'Ana', admin: true }, { id: 2, name: 'Bo', admin: false }];
console.log('3)', pluck(users, 'name'), pluck(users, 'admin'));

// 4) Generic types and classes
interface ApiResponse<T> { data: T; status: number }
class TypedCache<K, V> {
  #map = new Map<K, V>();
  set(k: K, v: V): this { this.#map.set(k, v); return this; }
  get(k: K): V | undefined { return this.#map.get(k); }
}
const res: ApiResponse<string[]> = { data: ['x', 'y'], status: 200 };
const cache = new TypedCache<string, number>().set('hits', 3);
console.log('4)', res.data.length, cache.get('hits'));

// 5) Utility types
interface Todo { id: number; title: string; done: boolean; tags: string[] }
type TodoPatch = Partial<Omit<Todo, 'id'>>;           // every field optional except no id
type TodoPreview = Pick<Todo, 'id' | 'title'>;
type ById = Record<number, TodoPreview>;
const todo: Readonly<Todo> = { id: 1, title: 'write ch16', done: false, tags: [] };
function update(t: Todo, patch: TodoPatch): Todo { return { ...t, ...patch }; }
const byId: ById = { [todo.id]: { id: todo.id, title: todo.title } };
console.log('5)', update(todo, { done: true }).done, byId[1]?.title);

type Fn = (a: number, b: string) => boolean;
type P = Parameters<Fn>;                               // [a: number, b: string]
type R = ReturnType<Fn>;                               // boolean
const args: P = [1, 'x'];
const ret: R = true;
console.log('5)', args, ret);

// 6) Mapped and conditional types (a peek)
type Flags<T> = { [K in keyof T]: boolean };            // same keys, boolean values
type ElementOf<T> = T extends readonly (infer E)[] ? E : never;
const dirty: Flags<Pick<Todo, 'title' | 'done'>> = { title: true, done: false };
const tag: ElementOf<string[]> = 'urgent';
console.log('6)', dirty, tag);
