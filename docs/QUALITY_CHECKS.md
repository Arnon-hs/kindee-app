# Local quality checks

Step 1 of [the anti-slop plan](../anti-slop/todo.md) installs reproducible **manual** commands for the maintained Vue/TypeScript client. Native Kotlin/Swift use their own build/test commands. No Git/agent hook, CI job, schedule or branch-protection setting is added by this step.

Requires Node.js 22.12+ and npm. Install the locked tools:

```sh
npm ci
npm run lint
npm run lint:changed
npm run deadcode
npm run dup
```

| Command | What it does | Exit behavior |
| --- | --- | --- |
| `npm run lint` | Oxlint 1.93.0 correctness rules, explicit-any, unused assignments, eval, unreachable code and constant conditions; includes Vue scripts with template-aware native variable checks | Existing warnings print; syntax/config failures fail. Step 2 adds manual changed-file enforcement |
| `npm run deadcode` | Knip 6.45.0 finds unused files/exports/types and dependency-resolution issues | Intentionally report-only with `--no-exit-code`; inspect findings. Tool/config failures are not intended success |
| `npm run dup` | jscpd 5.4.3 scans logic and larger component/style blocks, including Vue SFCs | Fails on any qualifying clone (`threshold: 0`) or an empty scan. This is a local command, not a new CI gate |

The lockfile pins resolved packages; these scripts call installed binaries, not a floating npx download. Six Workbox modules imported by src/sw.ts are now exact direct build dependencies at their existing 7.4.1 version. Their versions and application source did not change.

## Scope and exclusions

Knip uses `knip.config.ts` rather than JSONC because its one generated-import exception depends on whether the state-check cache exists. Both a clean checkout without that file and a warm checkout were verified without unresolved issues or configuration hints. Vite's plugin discovers index.html, application entry, Vite config and injectManifest service worker. Explicit entries cover node:test files, the dynamically built SSR state check and the manual recipe export. Entries do not execute that export or access its local API.

Native source/vendored libraries (`native/`, root `android/` and `ios/`) have independent platform checks. Installed dependencies, dist output and reports are not analysis source. These exclusions are documented inside the Knip/Oxlint configs. No live web application module is ignored to hide an existing finding.

jscpd 5.4.3 accepts strict JSON, so its exclusion comments live here rather than using unsupported JSONC or an ignored `$comment` field:

| Config pattern | Reason |
| --- | --- |
| `path: src` | Analyze maintained web application code, not native/vendor/build folders |
| `**/*.test.*`, `**/*.spec.*`, `**/fixtures/**` | Repeated test cases and fixture inputs are outside the application clone scan |
| `**/*.d.ts` | Ambient/generated declarations are contracts, not executable logic |
| `**/generated/**`, `**/__generated__/**` | Generator-owned output must be fixed at its generator |

Logic uses 6 lines / 40 tokens; larger component/style blocks use 15 lines / 100 tokens. Vue files are explicitly selected: jscpd tokenizes their embedded script/template/style blocks. Blocks smaller than these limits are intentionally not detected. The earlier generic audit did not select Vue; its zero count alone did not certify Vue coverage. No semantic model or embedding API is used.

The initial Oxlint baseline uses native rules only. No SonarJS formatting, inferred-boundary-type, password-dictionary, bounded-test-regex or template-unused-variable diagnostics were adopted from the broad probe. Non-null assertions, nested conditions and complexity are reserved for step 2. The temporarily inspected SonarJS 4.2.2 package declared LGPL in npm metadata but supplied a different Sonar Source-Available LICENSE; it was removed and is absent from the manifest/lockfile. Resolve that discrepancy or choose verified native equivalents before a later step adds those rules. Developer tools are not bundled into either mobile app.

## Step 1 acceptance on 2026-11-10

| Measure | Before configuration | After configuration |
| --- | --- | --- |
| Knip unused files | 3 | 1: src/platform.ts |
| Knip unused exports / exported types | 4 / 5 | 4 / 5, retained for reviewed cleanup |
| Knip unlisted Workbox imports | 6 | 0 |
| Knip unresolved generated import | Depends on whether cache existed | 0, verified cold and warm |
| Config hints | No project config | 0, verified cold and warm |
| Broad probe lint diagnostics | 73 on Oxlint 1.93.0 | Probe unchanged; selected native baseline has 0 diagnostics, not 73 fixes |
| Text clones | 0 in generic probe without Vue | 0 in both installed configs, now with Vue selected |

One-off plants demonstrated real tool output: Knip reported an unimported file and an unused export in a reachable module; Oxlint reported unreachable code inside a Vue script while accepting a variable used only by its template; jscpd exited 1 for copied TypeScript and copied Vue blocks in both configs. A three-line copy stayed below the documented threshold. All plants were removed and the touched source restored exactly. Final commands returned 0; Knip still prints its reviewed backlog.

Local command wall times including npm startup: lint 0.20 s, Knip report 0.47 s, both clone scans 0.14 s. These are local observations, not CI-duration promises. No custom rule or automated check is claimed without a demonstrated plant.

## Existing behavior and platform verification

Run the web checks in order because PWA tests read the built manifest:

```sh
KINDEE_PUBLIC_DEMO=true npm run build
KINDEE_PUBLIC_DEMO=true npm test
npm run test:state
```

Native Android: `cd native/android`, then `./gradlew :app:testDebugUnitTest :app:assembleDebug` with JDK 21 and the Android SDK described in native/README.md. iPhone: `scripts/test-native-ios-contracts.sh`, then `scripts/build-native-ios-simulator.sh` with a full Xcode and appropriate DEVELOPER_DIR/KINDEE_BUILD_ROOT.

This step changes tooling/configuration/docs and direct dependency declarations. It does not change native app source, server settings, account access or menus, and does not publish replacement app binaries. The existing v0.4.0-preview.1 remains the app download. Physical iPhone, Watch, Health and a new full menu/photo-AI runtime acceptance are not inferred from a build; prior simulator acceptance is documented separately in IOS_CLOUD_TEST.md.

Tool references: [Knip configuration](https://knip.dev/reference/configuration), [Oxlint configuration](https://oxc.rs/docs/guide/usage/linter/config), [jscpd engine documentation](https://github.com/kucherenko/jscpd/blob/master/docs/rust.md). Installed-tool help and local planted output were used to verify supported options; unsupported JSON fields were removed.

Step 1 platform verification: fresh Android Debug build from this repository, **1,000 tests passed, 0 failed/errors/skips**. iPhone-only arm64 Simulator build passed on **SDK 26.5**, its ad-hoc signature verified, and Swift meal/date/AI-boundary contracts passed. A clean locked npm install, PWA build, **19 tests** and session-store integration check passed. Application source/native manifest hashes remained unchanged. Simulator runtime and real hardware scenarios were not repeated for this tooling change.


## Step 2: reject warnings in changed web source

PR #1 was squash-merged at `0857ddcc554d8dc3d3f32a82c22f543b36a66157`; its default-branch web/Capacitor CI passed. This next step uses the existing locked Oxlint 1.93.0 with no dependency change:

- `typescript/no-non-null-assertion`: warnings for TypeScript's postfix `!` shortcut.
- `no-nested-ternary`: warnings for nested conditional expressions, including Vue scripts.
- `complexity`: an explicit cyclomatic-path budget of **8** for `src/api/auth.ts`. The existing authenticateAccount measures **9** and remains a reviewed warning. This is a different metric from Sonar cognitive complexity, not a claim of identical coverage. Authentication has a small, focused branch budget; the rule is not imposed on unrelated planning code in this step.

Full `npm run lint` reports **17 warnings**: 11 non-null assertions, 5 nested ternaries and 1 auth-complexity warning. These are maintenance findings, not 17 established bugs. Their source was not rewritten or silently suppressed. Warning locations:

| Rule | Files retained for later edits |
| --- | --- |
| Non-null assertions | src/components/HealthConnection.vue, src/components/MealPlanner.vue, src/health-types.ts, src/stores/planning.ts, tests/planning.test.ts |
| Nested ternaries | src/api/auth.ts, src/recipe-labels.ts, src/views/DashboardView.vue |
| Auth complexity | src/api/auth.ts |

Use the changed-file command before submitting changed web source:

```sh
git fetch origin
npm run lint:changed
# For only local staged/dirty/untracked edits, excluding earlier branch commits:
npm run lint:changed -- --base HEAD
```

The default compares with the merge base of HEAD and origin/master and also collects staged, unstaged and untracked files. Renames are checked at their new path; deleted paths are skipped. Git's NUL-delimited output and argument arrays preserve spaces, percent/hash characters and newlines. An absent/unrelated base fails instead of returning an empty success. Fetching the base remains the caller's responsibility; this command does not access the network.

Warnings fail for each selected file with `--deny-warnings`. An unchanged legacy file does not block the command. A touched legacy file must clear its warnings. The command checks current **working-tree content**; it is not a pre-commit index snapshot verifier. Config-only changes still require the full `npm run lint` command. Its scope matches the web lint inputs: script extensions under src/tests/scripts plus Vite, Capacitor and Knip root configs. Native, docs, generated output and assets keep their existing independent checks.

Ten integration tests use disposable Git fixtures and the actual pinned Oxlint. They demonstrate: untouched backlog excluded; untracked files with spaces/hash/percent/newline names rejected for assertions; staged and committed changes rejected; a dirty tracked file rejected and its narrowed replacement accepted; renamed files checked and deletions skipped; nested Vue ternaries rejected; auth complexity above the explicit budget rejected; missing base rejected and explicit HEAD accepted. Fixtures are removed after each test.

Local acceptance: PWA build, **29/29 tests** (the prior 19 plus these 10), session-state check, changed-file lint, Knip report and both zero-clone scans passed. Application/native source, dependencies and workflows remain unchanged. Previous native builds are still tied to identical native source; they were not rerun for this web-tooling step. No hook, new CI gate, deployment, server setting or replacement binary is added. The next plan step is the framework-aware TypeScript flags.

References for the chosen native rules: [non-null assertions](https://oxc.rs/docs/guide/usage/linter/rules/typescript/no-non-null-assertion), [cyclomatic complexity](https://oxc.rs/docs/guide/usage/linter/rules/eslint/complexity). Existing compact-format, bounded-test-regex and SonarJS license exclusions remain documented above.


## Step 3a: framework-aware indexed access

`tsconfig.json` now enables `noUncheckedIndexedAccess`. The existing build runs `vue-tsc --noEmit`, so it checks both TypeScript modules and Vue templates. The **10 measured diagnostics are now 0**. The [TypeScript option](https://www.typescriptlang.org/tsconfig/noUncheckedIndexedAccess.html) makes uncertain indexed reads include undefined; ordinary strict mode alone did not check these sites.

- App navigation and profile/macro/onboarding fields now keep each value with its label/icon/route in one record rather than parallel arrays. Order, text, routes, option values, defaults, classes and style source remain the same.
- Demo recipe slug extraction guards absence before decoding. Missing recipes still use recipeNotFound; valid filter/detail/portion behavior was compared with the base revision.
- Weight-change math and the latest-weight display only read observed endpoints after presence checks. A weight-change result stays unknown for fewer than two readings; the latest measurement is displayed when present. Existing valid-input rounding/sign is preserved.
- Dashboard error text now uses equivalent early returns to satisfy the warning rule already active on changed files. Full warnings are now **15** (10 assertions, 4 nested ternaries, 1 auth complexity); changed files have none.

One-off checks against the merged PR #2 source compared **30 exact SSR HTML outputs** across ru/en/th, **18 demo API outcomes**, and **57 weight-history cases**. A wrong-route plant was detected by the same comparison. SSR uses controlled synthetic state/onboarding stages, an inert PWA helper and no network; it does not certify browser interactivity, pixels, permission flows, actual menu data or a physical phone. The scoped design-system check reuses source-inspected existing controls and CSS; no component contract, visual world or shared token was added. No new screenshot/live-UI claim is made.

Temporary .ts and .vue unsafe-index plants passed with the flag disabled and failed when enabled; both were removed, and the real project typecheck passed. Local PWA build, **29 tests**, session-state check, strict changed-file lint, Knip report and both clone scans passed. Application transport/cache/native code and dependency versions did not change; native builds were not repeated for identical inputs.

The recipe requires **one flag per PR**, so original step 3 is now 3a and 3b. exactOptionalPropertyTypes remains off; its **3 known diagnostics** (request-body omission and Workbox callback types) are the next independent compiler change. No cast/ignore workaround or cache behavior change is introduced here. No Git/agent hook, new workflow or branch-protection setting is added.
