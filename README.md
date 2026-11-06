# KinDee Apps — MVP 0.3 preview

New: self-service account registration/login against the local platform; independent device sessions; weekly saved meal plans and an ingredient shopping list. Plan snapshots never alter consumed calories automatically. The public Site remains an explicitly labelled memory-only demo; full accounts and persistent data need a running platform API.

Clone the private platform as the sibling `../kindee-platform`, then run `python3 scripts/start-local.py` from that directory. Open http://localhost:8080 and register an account. Email verification/password reset and a public production API host are not included. [Current scope and checks](docs/MVP.md).

Open client applications for KinDee: choose food around your goals, time and budget.

- [Presentation](https://kindee-eat-well.arnon-hs.chatgpt.site/)
- [Try the public app](https://kindee-eat-well.arnon-hs.chatgpt.site/app/)
- [Releases](https://github.com/Arnon-hs/kindee-app/releases)

## Included

Vue PWA with recipes, portions, food diary, profile and progress; Android phone project with Health Connect bridge; Wear OS companion; iPhone and Apple Watch source projects with HealthKit/WatchConnectivity. Backend implementation is maintained in the private `kindee-platform` repository. No backend credentials or live GrabFood/LINE MAN adapter is included here.

## Run and verify

Requires Node.js 22+ and npm. From this repository root:

```sh
npm ci
KINDEE_PUBLIC_DEMO=true npm run build
KINDEE_PUBLIC_DEMO=true npm test
npm run dev
```

The public demo uses an explicitly illustrative recipe snapshot. Personal demo entries stay in tab memory and disappear on reload. The normal build uses a separately deployed Laravel API; for local development Vite proxies `/api` to `http://localhost:8080`. A clone of the private platform can run alongside this repository for the full account flow.

Install the PWA on iPhone with Safari → Share → Add to Home Screen; on Android with Chrome → Install app. HTTPS is required outside localhost. See [PWA instructions](docs/PWA.md).

## Native builds

Android: JDK 21, Android SDK platform 36 and build-tools 35.0.0/36.0.0. Set JAVA_HOME and ANDROID_HOME, then run `npm run build:android`. This builds both phone and Wear OS debug APKs. Debug permits local HTTP for development; production requires HTTPS. Phone and watch must use matching signing certificates. The app does not bypass health permissions.

Apple: on a Mac with Xcode, XcodeGen and CocoaPods, run `npm run build:apple` for unsigned simulator builds. Real-device installs require your Apple team and provisioning. No iPhone IPA or watchOS binary was built in the Linux workspace. Proprietary/Amazfit watch support remains unconfirmed.

See [health/onboarding details](docs/HEALTH_AND_ONBOARDING.md). Actual physical watch/phone synchronization has not been validated. Activity is not treated as a measured weight-loss result, and sources must not be summed twice.

## CI and releases

GitHub Actions builds/tests the PWA and compiles Android phone and Wear OS APKs. A `release:` commit or manual workflow run publishes a prerelease after those jobs pass. Apple builds remain a documented Mac workflow; no signed Apple release is claimed. Release artifacts are for testing, not a production medical or dietary service.

Original client source: MIT. Photography and dependencies retain their own licenses; see [NOTICE.md](NOTICE.md).
