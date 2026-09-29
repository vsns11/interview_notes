// Date: creating, reading, changing, comparing and formatting dates.
// Pin the local time zone so the output is the same on every machine.
process.env.TZ = 'America/New_York';

// 1) The four ways to construct a Date
const now = new Date();                                   // current moment
console.log('now is a Date:', now instanceof Date, typeof now.getTime());
const epoch = new Date(0);                                // ms since 1970-01-01T00:00:00Z
const fromMs = new Date(1_700_000_000_000);
const fromIso = new Date('2024-03-10T14:30:00Z');         // ISO 8601 string (Z = UTC)
const fromParts = new Date(2024, 0, 31, 9, 15);           // local time; month is 0-based (0 = Jan)
console.log(epoch.toISOString(), fromMs.toISOString());
console.log(fromIso.toISOString(), fromParts.toString());

// 2) Getters (local time) vs UTC getters
const d = new Date('2024-07-04T23:30:00Z');
console.log('local:', d.getFullYear(), d.getMonth(), d.getDate(), d.getDay(), d.getHours(), d.getMinutes());
console.log('UTC  :', d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate(), d.getUTCDay(), d.getUTCHours());
console.log('offset minutes (UTC - local):', d.getTimezoneOffset());
console.log('getTime === valueOf:', d.getTime() === d.valueOf(), '| Date.now() is a number:', typeof Date.now());

// 3) Parsing pitfall: date-only ISO strings are UTC, date-time strings without Z are LOCAL
console.log(new Date('2023-01-01').toString());
console.log(new Date('2023-01-01T00:00').toString());
console.log(new Date('not a date').getTime(), String(new Date('nope')));

// 4) Setters mutate the Date and overflow ("roll over") into the next unit
const b = new Date(2024, 5, 15);
b.setFullYear(1990, 0, 1);                  // year, month, day in one call
console.log(b.toDateString());
const roll = new Date(2024, 0, 31);
roll.setMonth(1);                           // Feb 31 does not exist -> Mar 2 (2024 is a leap year)
console.log(roll.toDateString());
const addDays = (date, n) => { const c = new Date(date); c.setDate(c.getDate() + n); return c; };
console.log(addDays(new Date(2024, 11, 30), 5).toDateString());
const lastDayOfFeb = new Date(2024, 2, 0).getDate();   // day 0 of March = last day of Feb
console.log('days in Feb 2024:', lastDayOfFeb);

// 5) Differences: subtracting Dates gives milliseconds
const start = new Date('2023-01-01');
const end = new Date('2023-12-31');
const MS_PER_DAY = 1000 * 60 * 60 * 24;
console.log((end - start) / MS_PER_DAY + ' days');
// Across a daylight-saving change local midnights are 23 h apart, so round:
const beforeDst = new Date(2024, 2, 9), afterDst = new Date(2024, 2, 11);
console.log((afterDst - beforeDst) / MS_PER_DAY, Math.round((afterDst - beforeDst) / MS_PER_DAY));

// 6) Comparing
const x = new Date(2024, 0, 1), y = new Date(2024, 0, 1);
console.log(x === y, x.getTime() === y.getTime(), x < addDays(y, 1));

// 7) Formatting
const t = new Date('2024-03-10T14:30:00Z');
console.log(t.toISOString());               // always UTC, machine-friendly
console.log(t.toString());
console.log(t.toDateString(), '|', t.toTimeString());
console.log(t.toLocaleDateString('en-US'), '|', t.toLocaleDateString('en-GB'), '|', t.toLocaleDateString('de-DE'));
console.log(t.toLocaleString('en-US', { timeZone: 'Asia/Kolkata', dateStyle: 'full', timeStyle: 'short' }));
const fmt = new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric', year: 'numeric', timeZone: 'UTC' });
console.log(fmt.format(t));
const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
console.log(rtf.format(-1, 'day'), '|', rtf.format(3, 'week'));

// 8) Temporal (the modern replacement) - feature-detect it
console.log('Temporal available here:', typeof globalThis.Temporal !== 'undefined');
