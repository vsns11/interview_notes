// The conditional (ternary) operator:  condition ? valueIfTrue : valueIfFalse
const age = 20;
const label = age >= 18 ? "adult" : "minor";
console.log("label:", label);

const count = 1;
console.log(`You have ${count} new ${count === 1 ? "message" : "messages"}`);

// Chained ternaries read like an else-if ladder (fine when short and formatted like this):
const temp = 12;
const feel =
  temp < 0 ? "freezing" :
  temp < 15 ? "cold" :
  temp < 25 ? "pleasant" :
  "hot";
console.log("feel:", feel);

// It is an EXPRESSION, so it can be used where a statement can't — e.g. in an argument or a const.
const price = 100, isMember = true;
console.log("to pay:", price * (isMember ? 0.9 : 1));
