import {execFileSync, spawnSync} from 'node:child_process';
import {existsSync} from 'node:fs';
import {fileURLToPath} from 'node:url';
import {join} from 'node:path';

const root = fileURLToPath(new URL('../', import.meta.url));
const oxlint = fileURLToPath(new URL('../node_modules/oxlint/bin/oxlint', import.meta.url));
const rootConfigs = new Set(['vite.config.ts', 'capacitor.config.ts', 'knip.config.ts, openapi-ts.config.ts']);

function git(args) {
  return execFileSync('git', args, {cwd: root, encoding: 'utf8', maxBuffer: 8 * 1024 * 1024});
}

function readBase(args) {
  if (args.length === 0) return 'origin/master';
  if (args.length !== 2 || args[0] !== '--base' || !args[1] || args[1].startsWith('-')) {
    throw new Error('Usage: npm run lint:changed -- [--base <git-ref>]');
  }
  return args[1];
}

function isWebSource(path) {
  if (!existsSync(join(root, path))) return false;
  if (rootConfigs.has(path)) return true;
  return /^(src|tests|scripts)\//.test(path) && /\.(?:[cm]?[jt]sx?|vue)$/.test(path);
}

function changedFiles(base) {
  // Resolve first: a missing/unrelated base must fail, never turn into an empty pass.
  const ancestor = git(['merge-base', 'HEAD', base]).trim();
  const common = ['--name-only', '-z', '--diff-filter=ACMRTUXB'];
  const lists = [
    git(['diff', ...common, ancestor, 'HEAD', '--']),
    git(['diff', ...common, '--cached', '--']),
    git(['diff', ...common, '--']),
    git(['ls-files', '--others', '--exclude-standard', '-z']),
  ];
  return [...new Set(lists.flatMap(list => list.split('\0')).filter(Boolean))]
    .filter(isWebSource).sort();
}

try {
  const base = readBase(process.argv.slice(2));
  const files = changedFiles(base);
  console.log(`Changed web source files: ${files.length} (base ${base}).`);
  if (files.length > 0) {
    // Argument arrays and NUL-delimited Git output preserve spaces/newlines. This
    // checks working-tree content; it is not a staged-snapshot pre-commit hook.
    const result = spawnSync(process.execPath, [oxlint, '--deny-warnings', '--', ...files], {
      cwd: root, stdio: 'inherit',
    });
    if (result.error) throw result.error;
    process.exitCode = result.status ?? 1;
  }
} catch (error) {
  console.error(error instanceof Error ? error.message : String(error));
  process.exitCode = 1;
}
