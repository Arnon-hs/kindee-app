# Kindee 0.4.0 native pilot, preview 3

Includes the [Maps pilot and quality rollout from preview 2](RELEASE_0.4.0-preview.2.md), plus a defensive web account fix: HTTP 401/429 responses with JSON `null` preserve the intended authentication/rate-limit guidance rather than throwing a TypeError. A regression test failed before the fix and passed afterward. The web suite now has 37 tests.

Android APK and iPhone Simulator binaries are identical to preview 2: native source did not change. Updated PWA/source archives and BUILD_INFO.json pin this release's exact source. SHA256SUMS covers all artifacts. Signatures, archive integrity, provider-key pattern scans and uploaded asset digests were verified.

The cloud Maps search remains live on the same server release, with 16 Homa listings, 13 photo links and 50 separate menu observations. Directory menu/calorie fields remain unknown. No extra paid provider run or subscription was needed for this fix.

Runtime limits remain: Seeker is unavailable; the new Simulator UI was not accepted after its SpringBoard launch error. The iPhone archive is for Apple Silicon Simulator, not a signed device IPA. Device installation instructions remain in native/README.md and the preview 2 notes.

Git/CI checks are active. The project Codex Stop hook still needs the owner's trust review through `/hooks`, and the real one-month review remains due 2026-12-11 in issue #9. Production discovery-provider comparison is tracked in issue #12. Earlier releases are preserved.
