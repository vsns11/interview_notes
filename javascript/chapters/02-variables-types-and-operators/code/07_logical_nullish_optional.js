// Logical operators return one of their OPERANDS, not necessarily a boolean.
const t = true, f = false;
console.log("t && f:", t && f, "| t || f:", t || f, "| !f:", !f);
console.log('"cat" && "dog":', "cat" && "dog");    // both truthy -> last
console.log('"" && "dog":', JSON.stringify("" && "dog"));  // first falsy
console.log('0 || "default":', 0 || "default");     // first truthy
console.log('"" || null || 0:', "" || null || 0);   // none truthy -> last
console.log("!!'text':", !!"text", "| !!0:", !!0);

// Short-circuit: the right side runs only if needed.
let calls = 0;
const touch = () => { calls++; return true; };
false && touch();
true || touch();
console.log("right side evaluated", calls, "times");

// ?? (nullish coalescing): falls back only on null/undefined.
const settings = { volume: 0, title: "", theme: null };
console.log("volume || 50:", settings.volume || 50, "| volume ?? 50:", settings.volume ?? 50);
console.log('title || "Untitled":', settings.title || "Untitled", '| title ?? "Untitled":', JSON.stringify(settings.title ?? "Untitled"));
console.log('theme ?? "light":', settings.theme ?? "light");

// ?. (optional chaining): stop and return undefined if the left side is null/undefined.
const user = { name: "Ana", address: null, greet() { return "hi"; } };
console.log("user.address?.city:", user.address?.city);
console.log("user.profile?.photo?.url:", user.profile?.photo?.url);
console.log("user.greet?.():", user.greet?.(), "| user.wave?.():", user.wave?.());
console.log("user.tags?.[0]:", user.tags?.[0]);
try { user.address.city; } catch (e) { console.log("without ?. ->", e.constructor.name); }

// Logical assignment (ES2021)
const cfg = { retries: 0, name: "", port: undefined };
cfg.retries ||= 3;    // 0 is falsy -> replaced
cfg.name ??= "app";   // "" is not nullish -> kept
cfg.port ??= 8080;    // undefined -> replaced
let token = "abc"; token &&= token.toUpperCase();
console.log(cfg, token);

// Precedence: && binds tighter than ||; mixing ?? with || needs parentheses.
console.log("true || false && false:", true || false && false);
try { eval("null || undefined ?? 'x'"); } catch (e) { console.log("mixing ?? and || without () ->", e.name); }
console.log("(null || undefined) ?? 'x':", (null || undefined) ?? "x");
