import assert from 'node:assert/strict';
import test from 'node:test';
import {execFileSync, spawnSync} from 'node:child_process';
import {copyFileSync, mkdirSync, mkdtempSync, rmSync, symlinkSync, writeFileSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {dirname, join} from 'node:path';
import {fileURLToPath} from 'node:url';

const project = fileURLToPath(new URL('../', import.meta.url));
const unsafeAssertion = 'export function unsafe(value?: string) { return value!.length; }\n';

function fixture(check: (root: string) => void) {
  const root = mkdtempSync(join(tmpdir(), 'kindee-lint-changed-'));
  try {
    mkdirSync(join(root, 'scripts'));
    copyFileSync(join(project, 'scripts/lint-changed.mjs'), join(root, 'scripts/lint-changed.mjs'));
    copyFileSync(join(project, 'scripts/quality-scope.mjs'), join(root, 'scripts/quality-scope.mjs'));
    copyFileSync(join(project, '.oxlintrc.json'), join(root, '.oxlintrc.json'));
    // Reuse the locked checker; the disposable repo contains no copied dependencies.
    symlinkSync(join(project, 'node_modules'), join(root, 'node_modules'), 'dir');
    writeFileSync(join(root, '.gitignore'), 'node_modules/\n');
    git(root, ['init', '-q', '-b', 'master']);
    write(root, 'src/legacy.ts', unsafeAssertion);
    commit(root);
    git(root, ['update-ref', 'refs/remotes/origin/master', 'HEAD']);
    check(root);
  } finally {
    rmSync(root, {recursive: true, force: true});
  }
}

function git(root: string, args: string[]) {
  return execFileSync('git', ['-c', 'core.hooksPath=/dev/null', ...args], {cwd: root, encoding: 'utf8'});
}

function write(root: string, path: string, text: string) {
  mkdirSync(dirname(join(root, path)), {recursive: true});
  writeFileSync(join(root, path), text);
}

function commit(root: string) {
  git(root, ['add', '.']);
  git(root, ['-c', 'user.name=Kindee Fixture', '-c', 'user.email=fixture@example.test',
    '-c', 'commit.gpgsign=false', 'commit', '-qm', 'fixture']);
}

function run(root: string, args: string[] = []) {
  const result = spawnSync(process.execPath, ['scripts/lint-changed.mjs', ...args], {cwd: root, encoding: 'utf8'});
  assert.ifError(result.error);
  return {status: result.status, output: result.stdout + result.stderr};
}

test('changed lint leaves untouched backlog and native/docs changes out of scope', () => fixture(root => {
  write(root, 'docs/note.md', 'A documentation edit.\n');
  write(root, 'native/vendor.ts', unsafeAssertion);
  const result = run(root);
  assert.equal(result.status, 0, result.output);
  assert.match(result.output, /Changed web source files: 0/);
}));

for (const path of ['src/new file.ts', 'src/hash#percent%.ts', 'src/line\nbreak.ts']) {
  test(`changed lint rejects an untracked assertion in ${JSON.stringify(path)}`, () => fixture(root => {
    write(root, path, unsafeAssertion);
    const result = run(root);
    assert.equal(result.status, 1, result.output);
    assert.match(result.output, /no-non-null-assertion/);
  }));
}

for (const path of ['vite.config.ts','capacitor.config.ts','knip.config.ts','openapi-ts.config.ts']) {
  test(`changed lint checks the root configuration ${path}`,()=>fixture(root=>{
    write(root,path,'export const unsafe = value => value ? value > 1 ? 1 : 2 : 3;\n');
    const result=run(root);
    assert.equal(result.status,1,result.output);
    assert.match(result.output,/no-nested-ternary/);
  }));
}

test('changed lint rejects staged and committed source changes against the base', () => fixture(root => {
  write(root, 'src/new.ts', unsafeAssertion);
  git(root, ['add', 'src/new.ts']);
  assert.equal(run(root).status, 1);
  commit(root);
  const result = run(root);
  assert.equal(result.status, 1, result.output);
  assert.match(result.output, /no-non-null-assertion/);
}));

test('changed lint checks a dirty tracked file and accepts its narrowed replacement', () => fixture(root => {
  write(root, 'src/legacy.ts', unsafeAssertion + '// touched\n');
  assert.equal(run(root).status, 1);
  write(root, 'src/legacy.ts', 'export function safe(value?: string) { return value?.length; }\n');
  const result = run(root);
  assert.equal(result.status, 0, result.output);
  assert.match(result.output, /Changed web source files: 1/);
}));

test('changed lint handles renames and skips deleted files', () => fixture(root => {
  git(root, ['mv', 'src/legacy.ts', 'src/renamed file.ts']);
  assert.equal(run(root).status, 1);
  commit(root);
  git(root, ['rm', 'src/renamed file.ts']);
  const result = run(root);
  assert.equal(result.status, 0, result.output);
}));

test('changed lint rejects nested ternaries inside a Vue script', () => fixture(root => {
  write(root, 'src/Choice.vue', `<script setup lang="ts">
const choose = (first: boolean, second: boolean) => first ? 'a' : second ? 'b' : 'c';
</script>
<template>{{choose(true, false)}}</template>\n`);
  const result = run(root);
  assert.equal(result.status, 1, result.output);
  assert.match(result.output, /no-nested-ternary/);
}));

test('changed lint rejects authentication path complexity above its explicit budget', () => fixture(root => {
  const branches = Array.from({length: 9}, (_, index) => `if (value === ${index}) return ${index};`).join('\n');
  write(root, 'src/api/auth.ts', `export function auth(value: number) {\n${branches}\nreturn -1;\n}\n`);
  const result = run(root);
  assert.equal(result.status, 1, result.output);
  assert.match(result.output, /complexity/);
}));

test('changed lint fails closed for a missing base and accepts an explicit local base', () => fixture(root => {
  const missing = run(root, ['--base', 'missing-base']);
  assert.equal(missing.status, 1, missing.output);
  assert.equal(run(root, ['--base', 'HEAD']).status, 0);
}));
