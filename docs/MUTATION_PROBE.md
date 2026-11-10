# Focused mutation assessment

Step 8 was measured on 2026-11-11 using Node 22.23.1, tsx 4.23.15 and Stryker core 10.1.1. The target was the three-line public-recipe cache boundary in `src/cache-policy.ts`, with only `tests/cache.test.ts`. Source SHA-256: `21da2bc538a980823c1b80eb59149f801c97a795c5b8b2ee502dc129b44b60ad`.

The probe ran in a separate small scratch directory, containing the actual module/test and package metadata. No native source, provider request, user database or other project was involved. It used the built-in command runner, coverageAnalysis off, one worker and no score failure threshold. The installed core's Apache-2.0 license was checked; no Stryker dependency was added to the app.

| Probe | Mutants | Killed | Survived | Timeout/errors | Wall time |
| --- | --- | --- | --- | --- | --- |
| Existing assertions | 20 | 19 | 1 | 0 | About 2 seconds |
| Added nested-path regression assertion | 20 | 20 | 0 | 0 | About 2 seconds |

The survivor removed the leading anchor from the public-recipe URL regex. Existing tests rejected private routes and other origins, but did not reject `/untrusted/api/v1/recipes`. That is a real coverage gap for this privacy boundary, not an equivalent mutant or an existing production bug. The added assertion requires that nested lookalike route to remain uncacheable. Production code was unchanged.

Stryker's command runner reports one command result, not individual node:test coverage. These counts do not establish whole-app coverage or native safety. No assertions or production logic were changed merely to raise a score.

Decision: retain focused mutation as an on-demand diagnostic rather than add a permanent runner, score gate or schedule from one small-module assessment. Revisit adoption when more pure logic changes justify it. Normal unit and cache boundary checks remain in CI.

To reproduce the probe, copy the module and its test to a fresh scratch directory with the same relative paths, install exact Stryker core 10.1.1 and tsx 4.23.15 there, and run `stryker run` with:

```js
export default {
  mutate: ['src/cache-policy.ts'],
  commandRunner: {command: 'node --import tsx --test tests/cache.test.ts'},
  coverageAnalysis: 'off',
  concurrency: 1,
  timeoutMS: 5000,
  thresholds: {break: null},
  reporters: ['clear-text', 'json'],
  jsonReporter: {fileName: 'reports/mutation.json'},
  tempDirName: 'reports/stryker-tmp',
  ignorePatterns: ['install.log', 'reports/**'],
};
```
