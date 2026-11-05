#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${JAVA_HOME:?Set JAVA_HOME to JDK 21}"
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK}"
# This debug build permits HTTP to private LAN API addresses. Release builds must use HTTPS.
KINDEE_NATIVE=true KINDEE_LOCAL_API=true npm run build
npx cap sync android
cd android
./gradlew --no-daemon :app:assembleDebug :wear:assembleDebug
printf '\nPhone: android/app/build/outputs/apk/debug/app-debug.apk\nWatch: android/wear/build/outputs/apk/debug/wear-debug.apk\n'
