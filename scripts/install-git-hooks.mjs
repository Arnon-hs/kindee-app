import {execFileSync,spawnSync} from 'node:child_process';
import {existsSync,readFileSync,realpathSync} from 'node:fs';
import {join,resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const root=fileURLToPath(new URL('../',import.meta.url));
if(process.env.SKIP_INSTALL_SIMPLE_GIT_HOOKS==='1')process.exit(0);
const top=spawnSync('git',['rev-parse','--show-toplevel'],{cwd:root,encoding:'utf8'});
if(top.status!==0||realpathSync(top.stdout.trim())!==realpathSync(root))process.exit(0);
const configured=spawnSync('git',['config','--get','core.hooksPath'],{cwd:root,encoding:'utf8'});
if(configured.stdout.trim()) {
  console.log('Preserved the existing core.hooksPath; add npm run precommit:check to its pre-commit hook.');
  process.exit(0);
}
const hook=resolve(root,execFileSync('git',['rev-parse','--git-path','hooks/pre-commit'],{cwd:root,encoding:'utf8'}).trim());
if(existsSync(hook)&&!readFileSync(hook,'utf8').includes('npm run precommit:check')) {
  console.log('Preserved the existing pre-commit hook; add npm run precommit:check to it.');
  process.exit(0);
}
execFileSync(join(root,'node_modules/.bin/simple-git-hooks'),[],{cwd:root,stdio:'inherit'});
