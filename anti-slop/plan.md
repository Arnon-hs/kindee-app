# Anti-slop plan: Kindee Apps

Audited 2026-11-10 using anti-slop-audit, based on [10 anti-AI slop moves](https://evilmartians.com/chronicles/ten-anti-ai-slop-moves-for-frontend-projects-going-faster-than-humans-can-review). This is an audit and rollout plan, not a claim that new gates are installed. The owner authorized publishing the current project and applying these skills; these measured plan files accompany that release. Implementation steps remain separately reviewable.

## Fit

Kindee is moving from a food-planning prototype to a product with real menus and a reviewed calorie diary. Incorrect account or nutrition state can affect users outside development. Treat the retained Vue client as maintained code, with native Android/iOS as the active mobile pilot. Native Kotlin/Swift are outside this TypeScript probe; their actual platform builds and tests are recorded separately. No framework migration or broad rewrite is proposed.

## Stack

- npm, Vue 3, Vite, TypeScript ~5.7, strict already enabled.
- At audit: no project linter, formatter, Git hooks or agent hooks. Step 1 adds manual Oxlint/Knip/jscpd commands; hooks remain absent.
- Unit runner is node:test through tsx, 19 tests in six files; the probe failed to recognize this runner.
- No browser E2E suite and no React dependency. React-only rules and react-doctor do not apply.
- Handwritten HTTP API clients, no public OpenAPI/GraphQL schema.
- GitHub Actions client-release.yml runs web build/tests/session checks plus the older Capacitor Android build. It does not certify the new native snapshot.
- Native pilot: Kotlin/Compose and Swift/SwiftUI; retain their platform checks instead of applying TS rules to them.

## Baseline and measurement corrections

Fresh dependency installation succeeded. The actual Vue build passed in 2.95 seconds; session-state check passed in 0.77 seconds. Running npm test before build failed only because its PWA test reads dist/manifest.webmanifest. After the documented build-first sequence, all 19 tests passed (runner reported 0.12 seconds). No lint command exists, so no lint baseline was invented.

The generic probe reported three baseline tsc errors because tsc cannot resolve Vue SFCs. These are not product failures: vue-tsc passed. Framework-aware flag runs found **10** noUncheckedIndexedAccess diagnostics and **3** exactOptionalPropertyTypes diagnostics; the raw generic probe undercounted Vue template sites. Two optional-property findings come from the Workbox plugin/type relationship. Do not solve them with casts or ignores.

| Move | Status | Measured and triaged | Decision |
| --- | --- | --- | --- |
| 1 API contract | missing | Two shared fetch call sites; handwritten shape assertions, no available public spec | Adopt after backend schema is published; first document/check existing transport contracts |
| 2 Strict TS | partial | strict is on; Vue checker: 10 indexed-access, 3 optional-property diagnostics | Adopt flags in a small focused PR after non-null shortcut protection |
| 3 Behavior lint | partial, step 1 | 73 raw diagnostics, not 73 bugs; see triage below | Add only useful warning rules, with changed-file enforcement after baseline review |
| 4 Boundaries | missing | Existing UI -> stores/API separation; no measured import cycles | Defer gate until native/web ownership is documented; keep 3-5 rules at most |
| 5 Scar rules | partial | Actual native calendar mismatch and swallowed Keychain result reproduced during this release | Keep regression tests; do not invent a TS lint scar from a Swift defect |
| 6 Agent rules/hooks | partial | Release adds concise AGENTS.md and skill usage; no automatic hook | Add checks as commands first, then hook after clean baseline |
| 7 Mutation | missing | node:test runner has no dedicated Stryker coverage runner | Defer until a focused pure-logic module/command-runner cost is measured |
| 8 Dead code | configured, step 1 | Knip: 3 raw files; 1 unused source skeleton candidate, 2 maintenance/dynamic entries | Configure entries first, then remove only verified dead code |
| 9 Duplicates | configured, step 1 | jscpd: 0 textual clones in logic, components and styles | Cheap zero-clone command is suitable; no semantic/model download |
| 10 CI gate | partial | Existing build/tests/session checks run in CI; branch-protection state not verified | Preserve existing jobs, wire new checks after useful baselines |
| React doctor | n/a | Vue, no React | Skip; revisit only if React is introduced |

## Lint triage

The probe used oxlint 1.93.0; none is installed in the project. There are no existing warn-level project rules to promote.

- 11 non-null assertions: five sites reviewed in MealPlanner.vue, HealthConnection.vue and planning store. They rely on mounted/account/demo-state assumptions; no crash was established. Adopt no-non-null-assertion gradually, remove assertions through explicit state handling before indexed-access flags.
- Five nested-condition diagnostics reviewed, including auth error selection and locale labels. They are maintenance findings, not demonstrated wrong results. One cognitive-complexity finding is authenticateAccount; these justify a small auth mapping refactor later.
- Forty no-unenclosed-multiline-block findings: five reviewed, all reflect existing compact formatting. Skip this rule for this rollout; a formatting rewrite does not belong to the release.
- Four super-linear-regex findings: all reviewed, in tests scanning bounded in-repository translation strings. Do not report a production denial-of-service finding. Exclude these test-only matches with documented scope if the rule is adopted.
- One hardcoded-password diagnostic is in mvp-messages.ts, a localization dictionary, not a provisioned credential. Confirmed false positive; do not label it a leaked secret.
- One regex-complexity diagnostic describes the explicit private-host allowlist used by the local-development API boundary. Do not simplify away its host restrictions to improve a score.
- Ten explicit-module-boundary-types diagnostics: five inspected, exported helpers generally have useful inferred types. Skip as a blanket rule; concrete API contracts have higher value.

## Dead code and clones

Knip's tests/session-store-check.ts is used through scripts/check-session-stores.mjs and its generated SSR entry; configure this entry. scripts/export-public-recipes.mjs is a manual snapshot-maintenance utility; retain it as an explicit entry until maintainership decides otherwise. src/platform.ts is an unreferenced future bridge skeleton and is a deletion candidate. nativeApiConfigured has no callers and is one unused-export candidate. Five file-local types may be de-exported later, without treating their implementations as dead. Six unlisted Workbox packages are imported by the service worker but currently reached transitively; declare direct build dependencies in the detector/config PR. No textual clone was found; no speculative deduplication is proposed.

## Variants

| Choice | Selected | Cost of alternative |
| --- | --- | --- |
| Lint backlog | Gradual | Cleanup would touch existing UI/store files for 11 assertions and 6 nested/complexity diagnostics before delivery; compact-format 40 hits intentionally excluded |
| Optional-property flag | Always on after focused fixes | Count-gated alternative postpones 3 diagnosed sites/type relationships and leaves ambiguity in request body construction |
| Mutation | Deferred; required job without score threshold if later adopted | Report-only is cheaper to adopt but does not ensure the command ran; no timing estimate invented |
| react-doctor | n/a | No React to analyze |

## When checks run

| Moment | Budget | Proposed checks |
| --- | --- | --- |
| Agent turn, Codex | about 30 s | Vue typecheck/build 2.95 s, node tests after build, state check 0.77 s; changed-file lint once configured |
| Pre-commit | about 10 s | File lint and zero-clone jscpd (probe 0.4 s), after noise removal |
| PR/push CI | Existing jobs | Build, 19 tests and state checks already present; add detector jobs only after configuration and meaningful plants |
| Scheduled | none yet | No named report owner or expensive adopted check, so no unsolicited schedule |
| Re-audit | After last agreed step or major architecture change | Recheck Vue counts, native/web boundary and ownership |

Probe times: generic typecheck 0.5 s, lint 1 s, knip 1.7 s, jscpd 0.4 s. They are local measurements, not GitHub CI duration claims. The checkout was shallow; the one-commit history is incomplete and cannot justify saying the project has no earlier incidents or PRs.

## Scar candidates and skipped work

The iOS pilot demonstrated two real defects: an unsigned simulator build could not persist a Keychain session, and Buddhist local dates did not match Gregorian API plan dates. The release adds explicit session save/readback checks, ad-hoc simulator signing and a failing-before/passing-after calendar regression. These are native behaviors; do not translate them into unrelated TS lint rules.

No hooks, branch protection, scheduled jobs, mutation package, React tool or code cleanup was silently enabled. No old project workflows or services were changed. Raw findings stay leads, not a bug total.

## Log

2026-11-10: baseline measured, framework false positives corrected, lint/dead-code samples inspected. Rollout implementation has not begun. See todo.md.


2026-11-10, step 1, branch `chore/anti-slop-detectors`:

- Refreshed read-only probe before changes: Oxlint 1.93.0 still 73 diagnostics; Knip 3 unused files, 4 exports, 5 types, 6 unlisted Workbox imports; generic jscpd 0 clones. No finding was called a defect without review.
- Installed exact Knip 6.45.0, jscpd 5.4.3, Oxlint 1.93.0; Workbox's six directly imported modules are pinned to existing 7.4.1. Only dependencies/configuration/docs change. Native source hashes still match SOURCE.json.
- Configured Knip: unused files 3 -> 1 via explicit maintenance/dynamic/test entries; exports 4 -> 4, types 5 -> 5; unlisted Workbox 6 -> 0. No live module was deleted or ignored. src/platform.ts, nativeApiConfigured, locale exports and five exported types remain for step 5 review.
- Mechanical deviation from recipe JSONC: knip.config.ts conditionally allows its exact generated SSR import only while the cache is absent. Cold and warm runs both have 0 unresolved issues and 0 config hints. The Vite plugin already discovers src/sw.ts; no redundant entry or bespoke reachability script was added.
- Native Oxlint baseline: 0 diagnostics, no JS plugins or changed-file enforcement. Broad-probe 73 remains a scope comparison, not 73 fixes. Non-null/complexity/nested-condition work is still step 2. SonarJS 4.2.2 was inspected then removed because npm LGPL metadata disagreed with its packaged Sonar Source-Available LICENSE. Step 2 must resolve provenance or use proven native equivalents before adding that dependency.
- Corrected clone coverage: the audit's format list omitted Vue. Both installed configs now select Vue SFC blocks; 0 clones. Strict JSON jscpd rejects comments and unknown config fields, so per-pattern exclusion reasons live in docs/QUALITY_CHECKS.md; workers are a supported CLI flag. No semantic model download.
- Plants: Knip reported an unused file and reachable unused export; native Oxlint reported unreachable Vue script code and did not misreport template-only use; jscpd exited 1 on a 17-line copied TS module and a 46-line copied Vue component in both configs. A three-line copy correctly remained below threshold. All temporary changes were removed and source restored exactly; final manual commands pass while Knip prints its backlog.
- npm-startup-inclusive timings: lint 0.20 s, Knip 0.47 s, both clone scans 0.14 s. These are manual tools in step 1, within future hook budgets; no hook, CI gate, schedule or branch-protection mutation was made. No TS source lint warning remains under the selected baseline. Other broad-probe warnings are deferred, not suppressed with inline disables.
- Local acceptance: clean npm ci, web build, 19/19 tests, session-store check and all three detector commands passed. Native Android built in 32 s from this repository, 1,000/1,000 tests passed; iPhone Simulator arm64 SDK 26.5 build and signature verification passed, plus pure Swift contracts. Native source did not change. No hardware/runtime scenario was rerun. Existing CI is verified on the PR; this step adds no workflow/filter/job and needs no branch-protection setting.


2026-11-10, step 2, branch `chore/anti-slop-changed-lint`:

- Merged PR #1 only after its web/Android CI succeeded; exact reviewed head was 1bc926a, squash merge 0857ddcc554d8dc3d3f32a82c22f543b36a66157. Default-branch CI 42857069108 also passed. No release or backend mutation.
- Remeasured using the installed native rules, without installing the conflicting SonarJS dependency: non-null assertions 11, nested ternaries 5. Auth cyclomatic complexity is 9; a documented budget of 8 yields one auth warning. The chosen native metric differs from Sonar cognitive complexity and is scoped to auth only. Existing Oxlint stays 1.93.0; no manifest dependency or lockfile version changed.
- Full lint: 0 selected warnings -> 17 after enabling the three warning rules. No application source cleanup is disguised as a count reduction. The unchanged 11/5/1 backlog remains in HealthConnection.vue, MealPlanner.vue, health-types.ts, planning store/test, api/auth.ts, recipe-labels.ts and DashboardView.vue. A later edit to any such file must clear its active warnings.
- Added manual `lint:changed` using the branch merge base plus staged/dirty/untracked changes. It denies warnings on selected web files only, handles NUL-delimited names/renames/deletions and fails on missing base refs. Current working-tree content is checked; staged-only snapshot verification is not claimed. No Git/agent hook or CI gate was introduced ahead of step 6.
- Ten disposable-Git integration tests verify actual rule output and nonzero exits for unsafe assertions, Vue nested ternaries and auth complexity, with positive controls for narrowing and untouched backlog. They cover committed/staged/untracked/dirty/renamed/deleted paths and names with spaces, hash/percent and newline characters. No plants remain in application source.
- Local acceptance: full warning report, strict changed-file check, PWA build, 29/29 tests, state check, unchanged Knip backlog and zero clones in both configs. Application/native/dependency/workflow source remains unchanged. The previous Android/iPhone builds were not repeated for identical native inputs. Existing PR CI must verify this branch; no new required check or protection change is needed. Step 3 remains next.


2026-11-11, step 3a, branch `chore/anti-slop-indexed-access`:

- Owner approved merging the reviewed PR #2. Squash merge: 37ea1ed96e8b0c4694b3682a76c264cb67a96cc5; default-branch web/Capacitor CI 42874324241 passed. No release or server change.
- Recipe requires **one flag per PR**. Split original step 3 into 3a (noUncheckedIndexedAccess, this PR) and 3b (exactOptionalPropertyTypes, next). This preserves original scope/ordering while making the two compiler contracts independently reviewable.
- Remeasured with installed vue-tsc: indexed-access diagnostics **10 -> 0**, across six source files. The generic tsc probe misses Vue template sites; actual framework checking is authoritative. Both a temporary TypeScript and a Vue array-access plant passed with the flag disabled and failed with it enabled, then were removed. No new dependency or tool version.
- Navigation, profile/macro fields and onboarding options now carry values/labels together instead of coordinating parallel arrays by an unchecked index. Demo recipe slugs are guarded before decoding. Weight endpoints/latest record are explicitly present before reading. Literal as-const tables preserve field-key types; no new unsafe assertion, any cast or ignore was used.
- Changed-file enforcement required one small related cleanup: the Dashboard error-label nested ternary became equivalent early returns. With the weight helper's old non-null assertion also removed, full lint warnings **17 -> 15** (10 assertion / 4 ternary / 1 auth complexity); none in changed files. Other backlog files remain untouched.
- One-off baseline/current comparison: **30 rendered HTML cases** in ru/en/th (navigation, dashboard empty/populated/errors, progress, profile, prep and all three onboarding stages), **18 demo recipe API outcomes**, **57 weight inputs** matched exactly. A planted wrong route in the isolated candidate source was detected. SSR fixture uses a controlled onboarding start step, inert PWA helper and forbidden network; this is not a browser/pixel, live-server, provider or hardware acceptance. The harness remains local, not a permanent test suite.
- Local acceptance: Vue typecheck, PWA build, **29/29 tests**, state check, changed-file lint, unchanged Knip backlog and zero clones in both configs passed. CSS/tokens/handlers/API transport/cache/native source/dependencies/workflows were not changed. Native source inputs remain identical to earlier successful builds, so no redundant native build was run.
- The optional-property probe still reports its original **3** findings in api/dashboard.ts and sw.ts. Flag remains off until 3b; no Workbox implementation or HTTP-body semantics were changed in 3a. Existing CI must verify this PR; no new job, hook or protection setting is requested.


2026-11-11, step 3b, chore/anti-slop-exact-optional: exactOptionalPropertyTypes enabled, 3 diagnostics -> 0. Absent HTTP bodies are omitted instead of explicitly undefined. Workbox 7.4.1 initialized arrow hooks are guarded and exposed by their original function references; constructors, quota registration, cache name, limits and route scope are unchanged. Actual callback identity/status-200/status-404/missing-cache controls passed. A temporary RequestInit body:undefined plant passed with the flag disabled and failed when enabled, then was removed. Vue typecheck, changed lint, web build, 29 tests and state checks passed. No dependency/native/workflow change. User explicitly authorized continuing the complete plan; later bounded steps will remain separate PRs.

### 2026-11-11: step 4, API contract

Backend source was recovered, reviewed and published by kindee-platform PRs #1-2. Its canonical schema is pinned by commit and SHA-256 in contracts/source.json. Hey API 0.99.0 and Zod 4.6.7 generate types/validators; existing Fetch/Capacitor transport is preserved. Account responses now validate at runtime. Other consumer migrations are deferred per feature, as the recipe requires.

Source/output drift plants failed and were removed. Account malformed-payload tests and 48 old/new error outcomes passed, including a negative control. Build, 32 tests, session isolation and zero clones passed; lint 15 -> 11 warnings. Remaining warnings: planning.test.ts, HealthConnection.vue, stores/planning.ts, MealPlanner.vue non-null assertions and recipe-labels.ts nested ternary. CI source/regeneration plants and the final green run are recorded in this PR. Packaged MIT licenses checked; no dependency audit remediation claimed. Generated code excluded from detectors because the backend operation set exceeds current client usage, but it remains compiler checked.

Real PR #6 plant: workflow run 42898028758 rejected the deliberately modified generated file at npm run api:check. The remaining old Android job was canceled to avoid wasting runner time. The plant was removed by regeneration in a new commit; no force push or history rewrite.

### 2026-11-11: step 5, verified dead code

Fresh Knip and rg confirmed the unused platform skeleton, nativeApiConfigured, ru/en/th exports and five exported types had no consumers. Removed one file/one function and de-exported three constants/five types: one file, four exports, five types -> zero. Dynamic recipe maintenance, state checks and tests retained. Touched ingredientLabel now uses equivalent if branches; 55 outcomes matched the base and a deliberately wrong Thai locale was caught. Build, 32 tests, session isolation, deterministic API output and zero clones passed. Lint 11 -> 10 warnings, all existing non-null assertions in the prior planning/health files. No native/runtime behavior claim from this cleanup.
