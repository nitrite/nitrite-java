#!/usr/bin/env bash
# THREAT-MODEL §7 criterion 2, on the Android release variant.
#
# PLAN.md §6 M3 names this as the check that would have caught the guard gap, and it is the half
# nitrite-bridge's own tests cannot reach: on Android the guard is packaging, not a system
# property, so the only evidence is the built APK.
#
#   ./tool/verify_release_apk.sh
#
# Two builds, because one is not evidence. The release APK must contain none of the strings; the
# debug APK must contain all of them. Without that negative control the first check passes just as
# well when the app has stopped referencing the bridge for some unrelated reason — a renamed
# method, a deleted source set, a build that failed quietly.
set -euo pipefail

cd "$(dirname "$0")/.."

# Strings only the bridge has. `getSchema` is not among them on the JVM: Nitrite's own
# getSchemaVersion carries the substring, so it is in both APKs whatever the bridge does.
# `_dbinspect` is not either — the JVM core has no mDNS advertiser, and this app's NsdManager
# registration is off by default, so it would fail the control half.
NEEDLES=(listStores queryPage dbinspect.bridge.enabled)

GRADLE=${GRADLE:-./gradlew}
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

# The classes are inside classes*.dex, compressed in the APK — `strings` on the .apk itself finds
# nothing and would report a pass for the wrong reason. Unpack, then search.
grep_apk() {
  local apk="$1" out="$2" found=()
  rm -rf "$out" && mkdir -p "$out"
  unzip -q -o "$apk" -d "$out"
  for needle in "${NEEDLES[@]}"; do
    if grep -r -l -a -F -- "$needle" "$out" >/dev/null 2>&1; then
      found+=("$needle")
    fi
  done
  echo "${found[*]-}"
}

echo "==> release variant (nitrite-bridge is debugImplementation, so it is not in it)"
"$GRADLE" -q assembleRelease
release=app/build/outputs/apk/release/app-release-unsigned.apk
present="$(grep_apk "$release" "$work/release")"
if [[ -n "$present" ]]; then
  echo "FAIL: these survived into the release APK: $present" >&2
  exit 1
fi
echo "    none of: ${NEEDLES[*]}"
echo "    $(du -h "$release" | cut -f1) $release"

echo "==> debug variant (control)"
"$GRADLE" -q assembleDebug
debug=app/build/outputs/apk/debug/app-debug.apk
present="$(grep_apk "$debug" "$work/debug")"
for needle in "${NEEDLES[@]}"; do
  if [[ " $present " != *" $needle "* ]]; then
    echo "FAIL: $needle is missing from the debug APK, so the check above" >&2
    echo "      was not proving anything." >&2
    exit 1
  fi
done
echo "    back, as expected: $present"

echo
echo "OK: criterion 2 holds on the Android release variant."
