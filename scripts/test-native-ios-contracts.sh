#!/bin/sh
set -eu
project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
kindee_build_root=${KINDEE_BUILD_ROOT:-"$project_root/.native-build/ios"}
mkdir -p "$kindee_build_root/ModuleCache"
xcrun swiftc -module-cache-path "$kindee_build_root/ModuleCache" \
  "$project_root/native/ios/calorietracker/Services/KindeeMeal.swift" \
  "$project_root/native/ios/calorietracker/Services/KindeeCoachPayload.swift" \
  "$project_root/native/tests/KindeeContracts.swift" \
  -o "$kindee_build_root/KindeeContracts"
"$kindee_build_root/KindeeContracts"
