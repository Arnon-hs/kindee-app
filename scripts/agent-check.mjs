import {execFileSync,spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {isQualityInput} from './quality-scope.mjs';

const root=fileURLToPath(new URL('../',import.meta.url));
const npm=process.platform==='win32'?'npm.cmd':'npm';
const git=args=>execFileSync('git',args,{cwd:root,encoding:'utf8',maxBuffer:8*1024*1024});
if(process.argv.includes('--changed')) {
  const base=git(['merge-base','HEAD','origin/master']).trim();
  const paths=[git(['diff','--name-only','-z',base]),git(['ls-files','--others','--exclude-standard','-z'])].flatMap(value=>value.split('\0'));
  if(!paths.some(isQualityInput)){console.log('No changed web/tooling inputs.');process.exit(0);}
}
const started=performance.now(),failures=[];
const checks=['lint:changed','api:check','deadcode:check','dup','build','test','test:state'];
let built=false;
for(const check of checks) {
  if((check==='test'||check==='test:state')&&!built){console.error(`Skipped ${check}: build failed.`);continue;}
  const result=spawnSync(npm,['run',check],{cwd:root,env:{...process.env,KINDEE_PUBLIC_DEMO:'true'},stdio:'inherit'});
  if(check==='build')built=result.status===0;
  if(result.error||result.status!==0)failures.push(check);
}
console.log(`Agent checks: ${((performance.now()-started)/1000).toFixed(2)}s; failures: ${failures.join(', ')||'none'}.`);
process.exitCode=failures.length?1:0;
