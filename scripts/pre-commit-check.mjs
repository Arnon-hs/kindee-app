import {execFileSync,spawnSync} from 'node:child_process';
import {existsSync,mkdtempSync,rmSync,symlinkSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {fileURLToPath} from 'node:url';
import {isLintInput,isQualityInput} from './quality-scope.mjs';

const root=fileURLToPath(new URL('../',import.meta.url));
const git=args=>execFileSync('git',args,{cwd:root,encoding:'utf8',maxBuffer:8*1024*1024});
const staged=git(['diff','--cached','--name-only','-z','--diff-filter=ACMRD']).split('\0').filter(Boolean);
if(!staged.some(isQualityInput)){console.log('No staged web/tooling inputs.');process.exit(0);}
const temporary=mkdtempSync(join(tmpdir(),'kindee-staged-'));
const started=performance.now();
try {
  // Validate index bytes, never let an unstaged fix mask an unsafe staged version.
  // Copy only the retained web/tooling surface; native imports have independent checks.
  // Knip's Capacitor plugin reads these real platform manifests to resolve dependencies.
  const platformMetadata=new Set(['android/capacitor.settings.gradle','ios/App/Podfile','ios/App/CapApp-SPM/Package.swift']);
  const files=git(['ls-files','--cached','-z']).split('\0').filter(path=>path&&(!/^(native|android|ios)\//.test(path)||platformMetadata.has(path)));
  execFileSync('git',['checkout-index','--prefix='+temporary+'/','-z','--stdin'],{cwd:root,input:files.join('\0')+'\0'});
  symlinkSync(join(root,'node_modules'),join(temporary,'node_modules'),'dir');
  const lint=staged.filter(path=>existsSync(join(temporary,path))&&isLintInput(path));
  const commands=[];
  if(lint.length)commands.push([process.execPath,[join(root,'node_modules/oxlint/bin/oxlint'),'--deny-warnings','--',...lint]]);
  commands.push([process.execPath,[join(root,'node_modules/vue-tsc/bin/vue-tsc.js'),'--noEmit']]);
  commands.push([process.execPath,['scripts/check-api-contract.mjs']]);
  commands.push([join(root,'node_modules/.bin/knip'),[]]);
  for(const config of ['.jscpd.json','.jscpd.markup.json'])commands.push([join(root,'node_modules/.bin/jscpd'),['--config',config,'--workers','1']]);
  for(const [command,args] of commands) {
    const result=spawnSync(command,args,{cwd:temporary,encoding:'utf8',maxBuffer:4*1024*1024});
    if(result.error||result.status!==0) {
      process.stderr.write(result.stdout??'');process.stderr.write(result.stderr??'');
      throw new Error('Staged source quality check failed. This checker did not rewrite staged content.');
    }
  }
  console.log(`Staged source checks passed in ${((performance.now()-started)/1000).toFixed(2)}s.`);
} finally {rmSync(temporary,{recursive:true,force:true});}
