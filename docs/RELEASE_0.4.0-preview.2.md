# Kindee 0.4.0 native pilot, preview 2

Google Maps directory search is connected through Apify on the existing Kindee cloud API. In Plan > Nearby, choose an address/location and request nearby restaurants and stores. The native clients show names, categories, distance, allowed listing photos and Google Maps links. Existing real menu observations and the calorie-aware mixed plan remain separate.

## Verified

- Live Homa Phuket Town search, radius 2 km: 16 listings, eight restaurant/eight store results, 13 allowed photo links. Repeated POST reused the 24-hour cache. Existing nearby-menu endpoint still returned 50 observations.
- Two actual Apify runs cost USD 0.06405 each in existing credits. No subscription purchased. Server caps: three starts/day, eight/month, USD 0.50/run. A new area requires an explicit action; no background GPS billing.
- Backend: 61 tests/601 assertions, actual published schema, real registration through generated client validation and cleanup of the temporary account. Final cloud release marker: `3f1922815b12d2074ad2bd0e68f29b229c4fffb461f501ca24a511652c39b49c`.
- Native Android: 1,000 unit tests passed and Debug APK built. APK signature verified. iPhone: pure Swift contracts and signed arm64 Simulator build passed; bundle signature verified.
- Web: strict indexed/optional checks, generated API/runtime account validation, 36 tests, state isolation, zero Knip findings/clones. Real PR controls proved API drift and all three new quality gates reject changes, then passed after controls were removed. Ten old non-null warnings remain a report.
- Focused cache mutation assessment found and closed the missing nested-path regression assertion. Production cache logic was unchanged; no score gate or permanent Stryker dependency.

## Downloads and installation

- `Kindee-Android-Pilot.apk`: current Android phone pilot, package `com.kindee.native.debug`, internal version `0.1.0-kindee-debug`/1. This is a Debug-signed test app.
- `Kindee-iPhone-Simulator.zip`: Apple Silicon Simulator bundle, **not an IPA for a physical iPhone**. Extract, boot a compatible simulator, then run `xcrun simctl install booted /path/to/calorietracker.app`.
- `Kindee-PWA-Demo.zip`: illustrative public web demo. It does not expose private cloud menus or provision pilot access.
- `Kindee-Source.zip`, `BUILD_INFO.json`, `SHA256SUMS`: source and exact artifact provenance/integrity. Native binaries were built from the Maps source at `9e14b723f95273d7ec8066175e03c23550c174c3`; the complete native tree remains identical in the release source. Later changes concern web quality and documentation.

For a real iPhone, open `native/ios/KindeePhonePilot.xcodeproj`, select your Apple Team and supported capabilities, connect/unlock/trust the device, enable Developer Mode if required and Run. Device signing/TestFlight remains unverified. See native/README.md. No signed device IPA is supplied.

## Limits

Directory listings do not provide a structured menu, calories, verified dietary suitability, current opening, stock or delivery eligibility. Their menu/calorie fields stay null and dietary safety stays unknown. Images are third-party links. Existing menu nutrition remains explicitly estimated/reviewed. Grab ingestion and real watch synchronization are not claimed.

Seeker was not connected during this release check. A new Simulator installation eventually succeeded after resource cleanup, but launching failed with a SpringBoard error under low disk space; the new Nearby UI was not accepted in runtime. No new UI screenshot is claimed. Existing screenshots in the repository are dated earlier checks. Other projects were preserved; only Kindee's API container was updated.

Git pre-commit and CI checks are active. The concrete Codex Stop hook still needs the owner's trust review through `/hooks` in this checkout before automatic execution can be observed. The one-month warning review is due 2026-12-11 in issue #9; final anti-slop plan deletion waits for that review and hook verification.
