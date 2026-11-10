#!/bin/sh
set -eu
project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
kindee_build_root=${KINDEE_BUILD_ROOT:-"$project_root/.native-build/ios"}
mkdir -p "$kindee_build_root/tmp" "$kindee_build_root/ModuleCache"
export TMPDIR="$kindee_build_root/tmp/"
xcodebuild -project "$project_root/native/ios/KindeePhonePilot.xcodeproj" \
  -target calorietracker -sdk iphonesimulator -configuration Debug -jobs 1 \
  -clonedSourcePackagesDirPath "$kindee_build_root/SourcePackages" \
  -packageCachePath "$kindee_build_root/PackageCache" \
  CODE_SIGNING_ALLOWED=YES CODE_SIGN_IDENTITY=- DEVELOPMENT_TEAM= \
  TARGETED_DEVICE_FAMILY=1 ONLY_ACTIVE_ARCH=YES ARCHS=arm64 \
  COMPILER_INDEX_STORE_ENABLE=NO SWIFT_ENABLE_BATCH_MODE=NO \
  SYMROOT="$kindee_build_root/Products" OBJROOT="$kindee_build_root/Objects" \
  CLANG_MODULE_CACHE_PATH="$kindee_build_root/ModuleCache" build
