# Kindee 0.4.0 native pilot

This prerelease adds the current Fud AI-based Android and iPhone clients alongside the retained web/Capacitor source. The iPhone pilot connects to Kindee's existing cloud API for nearby menus, a saved mixed plan and reviewed food-photo calorie estimates.

Verified in iPhone 16 Simulator (iOS 26.5): 50 Homa menu candidates, 21 saved meals (14 cooking / 4 cafe / 3 delivery), server photo analysis and a 720 / 2,200 kcal local diary after explicit save. Secure session persistence and Thai Buddhist-calendar plan selection were corrected. Android native unit tests: 1,000 passed; fresh Debug APK built. See [the verification report](https://github.com/Arnon-hs/kindee-app/blob/v0.4.0-preview.1/docs/IOS_CLOUD_TEST.md).

## Downloads

- `Kindee-Android-Pilot.apk`: current native Android Debug build; install on a test phone. This final APK was not reinstalled on Seeker in this check.
- `Kindee-iPhone-Simulator.zip`: Apple Silicon Simulator app. It is not a signed IPA for a physical iPhone.
- `Kindee-native-source.zip`: repository source including the native clients, retained web client, shared sample assets, build/check scripts and notices. No provider keys, account sessions or optional model weights.
- `SHA256SUMS`: checksums for the three assets above.

Kindee cloud access requires an approved test account. OpenAI credentials stay server-side. Menu nutrition and photo calories are estimates; source freshness, delivery coverage and final provider prices must be reviewed. Grab is not connected. Physical iPhone/Watch/Health and TestFlight are unverified. Other projects were left untouched.

Global anti-slop, good-readme and design-system skills were installed and applied to project documentation, native component reuse and a measured Vue audit. The staged audit plan does not enable new hooks or CI gates. Existing GitHub CI checks the retained web/Capacitor clients, not the new native subtree. Dependency audit findings require separate triage.

Preserved Kindee and Fud AI MIT notices and added canonical direct-SDK notices to distributed native resources. No project license was changed.
