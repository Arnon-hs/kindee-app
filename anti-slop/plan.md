# Anti-slop plan: Kindee Apps

Audited 2026-11-10 using anti-slop-audit, based on [10 anti-AI slop moves](https://evilmartians.com/chronicles/ten-anti-ai-slop-moves-for-frontend-projects-going-faster-than-humans-can-review). This is an audit and rollout plan, not a claim that new gates are installed. The owner authorized publishing the current project and applying these skills; these measured plan files accompany that release. Implementation steps remain separately reviewable.

## Fit

Kindee is moving from a food-planning prototype to a product with real menus and a reviewed calorie diary. Incorrect account or nutrition state can affect users outside development. Treat the retained Vue client as maintained code, with native Android/iOS as the active mobile pilot. Native Kotlin/Swift are outside this TypeScript probe; their actual platform builds and tests are recorded separately. No framework migration or broad rewrite is proposed.

## Stack

- npm, Vue 3, Vite, TypeScript ~5.7, strict already enabled.
- No project linter, formatter, Git hooks or agent hooks before this release.
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
| 3 Behavior lint | missing | 73 raw diagnostics, not 73 bugs; see triage below | Add only useful warning rules, with changed-file enforcement after baseline review |
| 4 Boundaries | missing | Existing UI -> stores/API separation; no measured import cycles | Defer gate until native/web ownership is documented; keep 3-5 rules at most |
| 5 Scar rules | partial | Actual native calendar mismatch and swallowed Keychain result reproduced during this release | Keep regression tests; do not invent a TS lint scar from a Swift defect |
| 6 Agent rules/hooks | partial | Release adds concise AGENTS.md and skill usage; no automatic hook | Add checks as commands first, then hook after clean baseline |
| 7 Mutation | missing | node:test runner has no dedicated Stryker coverage runner | Defer until a focused pure-logic module/command-runner cost is measured |
| 8 Dead code | missing | Knip: 3 raw files; 1 unused source skeleton candidate, 2 maintenance/dynamic entries | Configure entries first, then remove only verified dead code |
| 9 Duplicates | missing | jscpd: 0 textual clones in logic, components and styles | Cheap zero-clone command is suitable; no semantic/model download |
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
