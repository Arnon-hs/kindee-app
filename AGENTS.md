# Kindee project instructions

Read README.md, DESIGN.md and docs/IOS_CLOUD_TEST.md before changing the mobile pilot. Current native source is under native/; root android/ios belong to the retained Capacitor client. Keep the private backend and credentials out of this repository.

Use design-system for relevant UI composition, starting from current tokens and controls. Use good-readme for public onboarding docs, grounded in commands and measured outcomes. Use anti-slop-audit for the Vue/TypeScript surface and anti-slop-rollout only for an agreed step of anti-slop/todo.md; React tools do not apply to this Vue client, and TS rules do not certify Swift/Kotlin.

Run public web checks in order: KINDEE_PUBLIC_DEMO=true npm run build, KINDEE_PUBLIC_DEMO=true npm test, npm run test:state. Native Android: cd native/android, then ./gradlew :app:testDebugUnitTest :app:assembleDebug. iOS: scripts/test-native-ios-contracts.sh, then scripts/build-native-ios-simulator.sh, followed by actual Simulator acceptance. Preserve simulator signing for Keychain. Serialize heavy local builds and leave other projects/services untouched.

Keep real menus/source times, AI estimates, recipe estimates, plans and consumed diary entries distinguishable. Use Gregorian wire dates with the user's local timezone, even when the device displays a Buddhist calendar. Do not claim hardware, Watch, HealthKit, delivery eligibility or production acceptance from source/unit/simulator results alone.

Never add provider keys, session tokens, signing keys or private local settings. Preserve dirty work and all upstream attribution. Build artifacts belong in versioned GitHub release assets rather than the source tree. Do not change repository visibility, license, branch protection or unrelated workflows as a side effect of this task.
