# Kindee project instructions

Read README.md, DESIGN.md and docs/IOS_CLOUD_TEST.md before changing the mobile pilot. Current native source is under native/; root android/ios belong to the retained Capacitor client. Keep the private backend and credentials out of this repository.

Use design-system for relevant UI composition, starting from current tokens and controls. Use good-readme for public onboarding docs, grounded in commands and measured outcomes. Use anti-slop-audit for the Vue/TypeScript surface and anti-slop-rollout only for an agreed step of anti-slop/todo.md; React tools do not apply to this Vue client, and TS rules do not certify Swift/Kotlin.

Run public web checks in order: KINDEE_PUBLIC_DEMO=true npm run build, KINDEE_PUBLIC_DEMO=true npm test, npm run test:state. Native Android: cd native/android, then ./gradlew :app:testDebugUnitTest :app:assembleDebug. iOS: scripts/test-native-ios-contracts.sh, then scripts/build-native-ios-simulator.sh, followed by actual Simulator acceptance. Preserve simulator signing for Keychain. Serialize heavy local builds and leave other projects/services untouched.

Keep real menus/source times, AI estimates, recipe estimates, plans and consumed diary entries distinguishable. Use Gregorian wire dates with the user's local timezone, even when the device displays a Buddhist calendar. Do not claim hardware, Watch, HealthKit, delivery eligibility or production acceptance from source/unit/simulator results alone.

Never add provider keys, session tokens, signing keys or private local settings. Preserve dirty work and all upstream attribution. Build artifacts belong in versioned GitHub release assets rather than the source tree. Do not change repository visibility, license, branch protection or unrelated workflows as a side effect of this task.

## Anti-slop checks

Roll out one agreed step from anti-slop/todo.md; anti-slop/plan.md records the reasoning and measurements.

- Fix or explain findings. Do not add non-null assertions, any casts, TypeScript ignores or disable comments to make a check pass.
- Each exclusion needs its reason in the config, or in docs/QUALITY_CHECKS.md when the tool only accepts strict JSON. Review exceptions when they grow.
- After a real bug fix, identify the regression check that would have caught it. Add a custom rule only for a demonstrated incident or measured recurring cost.
- Warnings and Knip backlog remain manual reports in step 1. Hooks, changed-file enforcement, CI gates and mutation testing are deferred to their planned steps.

Commands, after npm ci (Node 22.12+):

- npm run lint: scoped native Oxlint warnings for web source, tests, scripts and root configs.
- npm run deadcode: Knip report; review the remaining file/exports/types, do not treat exit 0 as an empty backlog.
- npm run dup: zero qualifying text clones, including Vue scripts/templates/styles at the documented thresholds.

Run these commands when changing their inputs and keep reports under gitignored reports/. See docs/QUALITY_CHECKS.md for scope, exclusion reasons and planted checks. Keep native builds separate and serialized.
