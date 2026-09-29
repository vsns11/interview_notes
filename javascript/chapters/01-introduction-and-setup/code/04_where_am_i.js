// Which host is running this code? The language is the same; the host adds different APIs.
console.log("typeof globalThis :", typeof globalThis);          // standard name for the global object everywhere
console.log("typeof window     :", typeof window);              // only in browsers
console.log("typeof document   :", typeof document);            // only in browsers (the DOM)
console.log("typeof process    :", typeof process);             // only in Node.js
console.log("Node version      :", process.version.split(".")[0] + ".x");
console.log("V8 is the engine  :", typeof process.versions.v8 === "string");
// Things every modern host provides (they are in the language or widely shared web APIs):
console.log("Math.max(3, 7)    :", Math.max(3, 7));             // ECMAScript built-in
console.log("typeof setTimeout :", typeof setTimeout);          // host API (browser and Node both have it)
console.log("typeof fetch      :", typeof fetch);               // web API, also built into Node 18+
