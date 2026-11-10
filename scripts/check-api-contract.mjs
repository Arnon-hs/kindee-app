import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {readFileSync,readdirSync,mkdirSync,mkdtempSync,rmSync} from 'node:fs';
import {join} from 'node:path';
import {fileURLToPath} from 'node:url';

const root=fileURLToPath(new URL('../',import.meta.url));
const source=JSON.parse(readFileSync(join(root,'contracts/source.json'),'utf8'));
const hash=value=>createHash('sha256').update(value).digest('hex');
const schema=readFileSync(join(root,'contracts/openapi.yaml'));
if(hash(schema)!==source.sha256)throw new Error('API source hash changed. Update the verified backend pin before regenerating.');

if(process.argv.length===3&&process.argv[2]==='--live') {
 const url=new URL(source.published_url);
 if(url.protocol!=='https:'||url.host!=='kindee-api.158.69.190.195.sslip.io'||url.pathname!=='/api/v1/openapi.yaml')throw new Error('Unexpected contract destination');
 const response=await fetch(url,{redirect:'error',signal:AbortSignal.timeout(15000),cache:'no-store'});
 if(!response.ok)throw new Error(`API contract HTTP ${response.status}`);
 const live=await response.arrayBuffer();
 if(live.byteLength>1024*1024||hash(Buffer.from(live))!==source.sha256)throw new Error('Published backend contract drifted from the verified pin.');
 console.log('Published backend contract matches the verified pin.');
} else if(process.argv.length===2) {
 mkdirSync(join(root,'reports'),{recursive:true});
 const temporary=mkdtempSync(join(root,'reports/api-check-'));
 function files(path,prefix='') {
  return readdirSync(path,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?files(join(path,entry.name),prefix+entry.name+'/'):[prefix+entry.name]).sort();
 }
 try {
  execFileSync(join(root,'node_modules/.bin/openapi-ts'),['-f','openapi-ts.config.ts','-o',temporary,'--no-log-file','--silent'],{cwd:root,stdio:'inherit'});
  const generated=join(root,'src/api/generated');
  const expected=files(temporary),actual=files(generated);
  if(JSON.stringify(expected)!==JSON.stringify(actual))throw new Error('Generated API file set drifted. Run npm run api:generate.');
  for(const file of expected)if(!readFileSync(join(temporary,file)).equals(readFileSync(join(generated,file))))throw new Error(`Generated API drift: ${file}. Run npm run api:generate.`);
  console.log(`Generated API matches the pinned source (${expected.length} files).`);
 } finally { rmSync(temporary,{recursive:true,force:true}); }
} else { throw new Error('Usage: npm run api:check [-- --live]'); }
