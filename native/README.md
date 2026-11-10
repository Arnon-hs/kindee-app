# Kindee native pilot

Current mobile source based on Fud AI at upstream commit `01a2f05c56aa61d36345fc1ad41e05f6bc8da415`, modified for Kindee's server, mixed meal plans, reviewed diary and light interface. SOURCE.json records the imported files and hashes. The parent repository keeps the earlier PWA/Capacitor projects separate.

## iPhone Simulator

Verified with Xcode 26.6, iOS SDK/runtime 26.5 and Apple Silicon iPhone 16 simulator. Install Xcode, its iOS runtime and Metal Toolchain first. Set DEVELOPER_DIR if Xcode is installed outside /Applications or xcode-select points to CommandLineTools. Keep build caches on a disk with space.

From the repository root, first check the pure Swift contracts and then build:

```sh
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
KINDEE_BUILD_ROOT=/path/to/build-cache scripts/test-native-ios-contracts.sh
KINDEE_BUILD_ROOT=/path/to/build-cache scripts/build-native-ios-simulator.sh
xcrun simctl boot 'your-simulator-UDID'
xcrun simctl bootstatus 'your-simulator-UDID' -b
xcrun simctl install 'your-simulator-UDID' /path/to/build-cache/Products/Debug-iphonesimulator/calorietracker.app
xcrun simctl launch 'your-simulator-UDID' com.kindee.native.debug
```

The script keeps simulator code signing enabled with an ad-hoc identity. Xcode embeds the simulated app identifier needed by Keychain. Building with CODE_SIGNING_ALLOWED=NO may open the interface but fail secure session storage. A successful API login must also save and read back its session before the app reports connected.

KindeePhonePilot excludes the embedded Watch dependency for this iPhone-only check. The original full project, Watch source and extensions remain in calorietracker.xcodeproj; Watch and iPad are not certified by this pilot. SDK 26.5 does not contain the iOS 27 FoundationModels photo Attachment API. That code stays behind FOUNDATION_MODELS_IMAGE_INPUT, selected for SDK 27; an SDK 27 build has not been verified. Kindee's server photo analysis works independently of that Apple-local feature.

For the isolated local UI test profile, Debug Simulator also supports `--ui-testing --ui-testing-onboarded`. These flags disable Health/iCloud/notifications, bypass onboarding for the fixture and do not provision a server account. Never ship the fixture as normal account acceptance.

## Physical iPhone

Select your Apple Team and compatible provisioning for the app, Widget and Share extensions, connect/unlock/trust the iPhone, enable Developer Mode when requested, select it in Xcode and Run. Real device signing, camera, HealthKit, Watch and TestFlight remain unverified. The downloadable Simulator app cannot be installed as a device IPA.

## Android

Use JDK 21, Android SDK compile platform 37.2 and the build tools/NDK required by android/app/build.gradle.kts. targetSdk is 36 and minimum API is 26. From the repository root:

```sh
cd native/android
./gradlew :app:testDebugUnitTest :app:assembleDebug --no-daemon --max-workers=1
```

Set JAVA_HOME, ANDROID_HOME and optionally GRADLE_USER_HOME for your machine. Debug builds copy only the 96 sample workout frames supplied here; the full 1.2 GB corpus and local model weights are intentionally absent. Shared voice tools, exercise data and dependency notices are included. Release signing keys and local OAuth settings are not distributed.

The server API defaults to the Kindee HTTPS pilot. Account access is provisioned separately. The app stores its own session, not the server's OpenAI credential. Review photo estimates and portions before logging; watch permissions and diary sharing remain independent actions.

## Attribution

Keep LICENSE, THIRD_PARTY_NOTICES.md, local-models/legal and per-resource licenses with redistributed copies. Optional models are separate downloads with their own terms. Provider logos remain their owners' trademarks. Runtime restaurant images are not a new freely licensed image dataset.
