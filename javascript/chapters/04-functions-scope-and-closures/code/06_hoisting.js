// Hoisting summary: how each kind of declaration behaves before its line.
function demo() {
  const report = [];
  report.push(["function declaration", typeof hoistedFn === "function" ? hoistedFn() : "missing"]);
  report.push(["var", String(v)]);
  try { l; } catch (e) { report.push(["let", e.name]); }
  try { c; } catch (e) { report.push(["const", e.name]); }
  try { new K(); } catch (e) { report.push(["class", e.name]); }
  try { fe(); } catch (e) { report.push(["var function expression", e.name + " (" + typeof fe + ")"]); }

  function hoistedFn() { return "callable"; }
  var v = 1;
  let l = 2;
  const c = 3;
  class K {}
  var fe = function () {};
  return report;
}
for (const [kind, result] of demo()) console.log(kind.padEnd(24), "->", result);

// Shadowing + TDZ: the inner let hides the outer variable even before its line
const x = "outer";
function shadow() {
  try { return x; } catch (e) { return e.name + ": inner x is in its TDZ"; }
  let x = "inner";
}
console.log(shadow());

// var inside a function is hoisted to the top of THAT function
var mood = "happy";
function moodSwing() {
  const before = mood;     // undefined, not "happy": the local var mood is hoisted
  var mood = "grumpy";
  return [before, mood];
}
console.log("var shadow hoisting:", moodSwing());
