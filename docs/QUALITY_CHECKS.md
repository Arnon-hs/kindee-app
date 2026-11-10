# Local quality checks

Step 1 of [the anti-slop plan](../anti-slop/todo.md) installs reproducible **manual** commands for the maintained Vue/TypeScript client. Native Kotlin/Swift use their own build/test commands. No Git/agent hook, CI job, schedule or branch-protection setting is added by this step.

Requires Node.js 22.12+ and npm. Install the locked tools:

```sh
npm ci
npm run lint
npm run deadcode
npm run dup
```

| Command | What it does | Exit behavior |
| --- | --- | --- |
| `npm run lint` | Oxlint 1.93.0 correctness rules, explicit-any, unused assignments, eval, unreachable code and constant conditions; includes Vue scripts with template-aware native variable checks | Existing warnings print; syntax/config failures fail. Warning enforcement on changed files is step 2 |
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

## Measured acceptance on 2026-11-10

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
