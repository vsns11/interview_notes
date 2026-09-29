// What a bundler does with a CSS Module. esbuild (like Vite, webpack, Parcel) treats *.module.css
// as "local CSS": class names are renamed to be unique and the import becomes an object.
import * as esbuild from 'esbuild';

const result = await esbuild.build({
  stdin: {
    contents: `import styles from './Card.module.css'; console.log(JSON.stringify(styles, null, 1));`,
    resolveDir: '.', loader: 'js',
  },
  bundle: true, write: false, outdir: 'out', format: 'esm', logLevel: 'error',
});
for (const file of result.outputFiles) {
  if (file.path.endsWith('.js')) {
    console.log('--- the JS object you get from `import styles from "./Card.module.css"` ---');
    const mapping = file.text.match(/\{[\s\S]*?\}/)[0];
    console.log(mapping.replace(/\n\s*/g, ' '));
  } else {
    console.log('--- the CSS the bundler emits (renamed, so it cannot clash) ---');
    console.log(file.text.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\n\s*\n/g, '\n').trim());
  }
}
