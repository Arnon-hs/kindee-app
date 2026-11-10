# Notices

The MIT license covers original KinDee client source. It does not relicense third-party images, the Gradle wrapper, generated framework code or dependencies. Their own licenses apply.

Food photography retains the CC licenses and attribution recorded in public/food/CREDITS.md and src/data/food-photos.json. Do not remove required notices from redistributed copies. Package dependencies and Gradle wrapper retain their upstream licenses.

The retained web client's generated response validators use Zod 4.6.7, copyright 2025 Colin McDonnell, under MIT. Its canonical license is distributed at public/licenses/zod-MIT.txt and copied into the web build. Hey API is a development generator; generated definitions do not replace upstream dependency notices.

There are no server secrets or production user records in this repository. Health connections and workout measurements require supported native platforms, user consent and device testing. PWA alone cannot read HealthKit or Health Connect. A proprietary watch is not supported merely because it resembles Wear OS or Apple Watch.

## Current native pilot

The `native/` subtree derives from [Fud AI](https://github.com/apoorvdarshan/fud-ai), upstream commit `01a2f05c56aa61d36345fc1ad41e05f6bc8da415`. Preserve its MIT license and copyright notice (2026 Apoorv Darshan). Kindee modifications add its server client, safe AI/session integration, menu/plan views, onboarding path, light palette and Today composition. They do not replace upstream authorship.

Direct native runtime notices include RevenueCat iOS 5.80.2 and Android 11.0.0 (MIT), Argmax/WhisperKit 1.1.0 (MIT plus its NOTICES), Swift Argument Parser 1.8.2 (Apache-2.0), LiteRT-LM's vendor notice bundle, and Whisper conversion/runtime notices. Canonical texts are retained in native resources and native/local-models/legal. FreeExerciseDB's Unlicense remains beside its data. Provider logo provenance remains in native/THIRD_PARTY_NOTICES.md.

The 96 Debug workout sample frames are the upstream authored sample pack; the full corpus and optional model weights are not redistributed here. Downloaded restaurant image URLs and UI screenshots do not grant redistribution rights to a restaurant photo dataset. No backend menu export, customer record, account credential or model-weight file is included in this source snapshot.
