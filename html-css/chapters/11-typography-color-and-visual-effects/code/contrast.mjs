// tools_run: skip  (module imported by 04_contrast.mjs and inlined into 04_contrast.html)
// WCAG 2.x contrast ratio, step by step.
// 1. sRGB channel 0-255 -> 0-1 -> linear light (undo the gamma curve)
export const lin = c => { c /= 255; return c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4; };
// 2. relative luminance: green counts most, blue least (how human eyes weigh them)
export const luminance = ([r, g, b]) => 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b);
// 3. ratio of the lighter to the darker, each + 0.05 (for screen flare) -> 1:1 … 21:1
export const ratio = (a, b) => { const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x); return (hi + 0.05) / (lo + 0.05); };
export const hex = h => [1, 3, 5].map(i => parseInt(h.slice(i, i + 2), 16));
// WCAG 2.2 levels: AA 4.5 (normal text) / 3 (large text ≥ 24px, or ≥ 18.66px bold, and UI parts); AAA 7 / 4.5
export const grade = r => r >= 7 ? 'AAA' : r >= 4.5 ? 'AA' : r >= 3 ? 'large/UI only' : 'FAIL';
