# KinDee 0.3.0-preview.1

Adds self-service account registration/login, independent 30-day sessions, saved meal plans and a shopping list derived from immutable ingredient snapshots. The private platform provides persistence; the public PWA demo keeps plans and diary entries in tab memory until reload. No server is bundled in these client artifacts.

Android phone/Wear OS debug APKs and a PWA demo build are included. The Android/native API URL must point to your own running platform. Existing CLI-only local profiles can keep using token login; create a new password account to use self-service login. Email verification and password reset are not implemented.

Client source includes iPhone/Apple Watch projects; no signed Apple binaries or physical-device synchronization pass is claimed. GrabFood/LINE MAN live data is not connected. Recipe nutrition/THB values remain labelled illustrative estimates.

Local checks: 25 Laravel tests / 252 assertions, 19 client tests, PSR-12 for 73 PHP files, OpenAPI validation and a real local HTTP account/planning/diary flow. GitHub Actions builds/tests this client and compiles Android artifacts before publishing. See README.md and docs/MVP.md.
