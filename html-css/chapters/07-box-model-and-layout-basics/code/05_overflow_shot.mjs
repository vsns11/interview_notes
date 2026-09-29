// tools_run: skip — makes img/05_overflow.png. Same as tools_web_shot.mjs, except it drops Puppeteer's
// default --hide-scrollbars flag so the scroll/auto scrollbars appear in the picture.
import puppeteer from 'puppeteer-core';
import path from 'node:path';
const b = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  headless: 'new', ignoreDefaultArgs: ['--hide-scrollbars'] });
const p = await b.newPage();
await p.setViewport({ width: 820, height: 250, deviceScaleFactor: 2 });
await p.goto('file://' + path.resolve('05_overflow.html'));
await p.screenshot({ path: '../img/05_overflow.png' });
await b.close();
