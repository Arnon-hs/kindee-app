import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync,existsSync} from 'node:fs';
import {resolve} from 'node:path';
test('PWA build includes standalone manifest, icons, offline shell and worker',()=>{
 const base=process.env.KINDEE_PUBLIC_DEMO==='true'?'/app/':'/';
 const manifest=JSON.parse(readFileSync('dist/manifest.webmanifest','utf8'));
 assert.equal(manifest.display,'standalone');assert.equal(manifest.scope,base);assert.equal(manifest.start_url,base);
 for(const size of ['192x192','512x512'])assert.ok(manifest.icons.some((i:{sizes:string})=>i.sizes===size));
 for(const icon of manifest.icons)assert.ok(existsSync(resolve('dist',icon.src.slice(base.length))));
 assert.ok(manifest.icons.some((i:{purpose:string})=>i.purpose==='maskable'));
 assert.ok(existsSync('dist/sw.js'));assert.ok(existsSync('dist/offline.html'));
 const sw=readFileSync('dist/sw.js','utf8');assert.ok(!sw.includes('__WB_MANIFEST'));assert.ok(sw.includes('kindee-public-recipes-v1'));
 const index=readFileSync('dist/index.html','utf8');assert.ok(index.includes('manifest.webmanifest'));assert.ok(index.includes('apple-touch-icon'));
});
