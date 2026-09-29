// CommonJS requiring an ES module (require(esm): on by default since Node 22.12 / 20.19).
const math = require('./math.mjs');
module.exports = { sum: math.add(20, 22), keys: Object.keys(math) };
