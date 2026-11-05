#!/usr/bin/env bash
set -euo pipefail
if [[ "$(uname -s)" != Darwin ]]; then echo "Requires macOS, Xcode, XcodeGen and CocoaPods. No IPA was built." >&2; exit 1; fi
command -v xcodegen >/dev/null
command -v pod >/dev/null
cd "$(dirname "$0")/.."
KINDEE_NATIVE=true npm run build
npx cap copy ios
cd ios/App
xcodegen generate
pod install
# Compiles unsigned simulator targets. Device installs need your Apple team/provisioning.
xcodebuild -workspace App.xcworkspace -scheme App -sdk iphonesimulator -configuration Debug -derivedDataPath build CODE_SIGNING_ALLOWED=NO build
xcodebuild -project App.xcodeproj -scheme KinDeeWatch -sdk watchsimulator -configuration Debug -derivedDataPath build-watch CODE_SIGNING_ALLOWED=NO build
