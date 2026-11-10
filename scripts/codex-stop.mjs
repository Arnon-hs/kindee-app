import {readFileSync} from 'node:fs';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';

const root=fileURLToPath(new URL('../',import.meta.url));
try {
  const input=JSON.parse(readFileSync(0,'utf8'));
  // Do not persist or echo hook input: it can contain transcript/account data.
  if(input.stop_hook_active===true){console.log('{}');process.exit(0);}
  const result=spawnSync(process.execPath,['scripts/agent-check.mjs','--changed'],{cwd:root,encoding:'utf8',timeout:27000,maxBuffer:1024*1024});
  if(!result.error&&result.status===0)console.log('{}');
  else console.log(JSON.stringify({decision:'block',reason:'Kindee web checks need review. Run npm run agent:check and resolve or report the failures.\n'+(result.stdout??'').slice(-3500)+(result.stderr??'').slice(-1500)}));
} catch {
  console.log(JSON.stringify({decision:'block',reason:'Kindee Stop hook could not complete. Run npm run agent:check manually and report its result.'}));
}
