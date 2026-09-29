// switch: compare one value against many cases using ===
function describeType(value) {
  switch (typeof value) {
    case "number":
      return "a number";
    case "string":
      return "a string";
    case "boolean":
      return "a boolean";
    default:
      return "something else";
  }
}
console.log(describeType(42), "|", describeType("hi"), "|", describeType(null));

// default = the catch-all
function menu(option) {
  let result;
  switch (option) {
    case 1:
      result = "Home";
      break;
    case 2:
      result = "Profile";
      break;
    case 3:
      result = "Settings";
      break;
    default:
      result = "Invalid selection";
  }
  return result;
}
console.log([1, 2, 3, 4].map(menu).join(", "));

// Grouped cases share one block
function openStatus(day) {
  switch (day) {
    case "Mon": case "Tue": case "Wed": case "Thu": case "Fri":
      return "open 9-17";
    case "Sat":
      return "open 10-14";
    case "Sun":
      return "closed";
    default:
      return "unknown day";
  }
}
console.log(["Tue", "Sat", "Sun", "Funday"].map(openStatus).join(" | "));

// Strict comparison: "18" (string) does not match 18 (number)
function ageCase(age) {
  switch (age) {
    case 18: return "matched the NUMBER 18";
    case "18": return "matched the STRING '18'";
    default: return "no match";
  }
}
console.log(ageCase("18"), "|", ageCase(18), "|", ageCase(18.0));

// Fall-through: forgetting break runs the following cases too
function withoutBreak(level) {
  const granted = [];
  switch (level) {
    case "admin":
      granted.push("delete");   // no break -> falls through (intentional here)
    case "editor":
      granted.push("edit");
    case "viewer":
      granted.push("read");
  }
  return granted;
}
console.log("admin:", withoutBreak("admin"), "viewer:", withoutBreak("viewer"));

// switch (true) pattern for ranges
function bmiCategory(bmi) {
  switch (true) {
    case bmi < 18.5: return "underweight";
    case bmi < 25:   return "normal";
    case bmi < 30:   return "overweight";
    default:         return "obese";
  }
}
console.log([17, 22, 27, 33].map(bmiCategory).join(", "));

// A lookup object is often cleaner than a switch that only maps values
const statusText = { 200: "OK", 404: "Not Found", 500: "Server Error" };
console.log(statusText[404], "|", statusText[418] ?? "Unknown status");

// Block scope inside cases: wrap in { } to declare let/const per case
function area(shape) {
  switch (shape.kind) {
    case "circle": {
      const r = shape.r;
      return +(Math.PI * r * r).toFixed(2);
    }
    case "rect": {
      const { w, h } = shape;
      return w * h;
    }
  }
}
console.log("areas:", area({ kind: "circle", r: 1 }), area({ kind: "rect", w: 2, h: 3 }));
