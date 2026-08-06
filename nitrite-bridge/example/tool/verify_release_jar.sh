#!/usr/bin/env bash
# THREAT-MODEL §7 criterion 2, on a desktop application artifact.
#
# nitrite-bridge's own tests prove the in-process half — DbInspect.start opens no socket without
# the opt-in. The criterion is written about a shipped application, and this is that: the shaded
# jar a developer would actually distribute, greppped for the protocol strings.
#
#   ./tool/verify_release_jar.sh
#
# Two builds, because one is not evidence. The release build (dbinspect `provided`) must contain
# none of the strings; the plain development build must contain all of them. Without that negative
# control the first check passes just as well when the app has stopped referencing the bridge for
# some unrelated reason — a renamed method, a deleted import, a build that failed quietly.
set -euo pipefail

cd "$(dirname "$0")/.."

# Strings only the bridge has.
#
# Two of the Flutter example's four needles do not carry over, and neither omission is a weakening:
#
#   getSchema   collides. Nitrite's own NitriteConfig, NitriteDatabase, MigrationManager and
#               StoreMetaData all carry `getSchemaVersion`, so the substring is in the release jar
#               whatever the bridge does — a needle that is always present proves nothing.
#   _dbinspect  the mDNS service type, and the JVM core has no advertiser yet (NsdManager is still
#               an open M3 item). Grepping for a string nothing emits would fail the control half.
#
# `org/dizitart/dbinspect` is deliberately not a needle either: Inspector.class references those
# types by name, so the *application's* constant pool keeps one hit in both builds. What must be
# gone is the bridge's own code, and the three below are only ever in that.
NEEDLES=(listStores queryPage dbinspect.bridge.enabled)

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

# Class files are deflated inside the jar, so `strings` on the .jar itself finds nothing and would
# report a pass for the wrong reason. Unpack, then search.
grep_jar() {
  local jar="$1" out="$2" found=()
  rm -rf "$out" && mkdir -p "$out"
  unzip -q -o "$jar" -d "$out"
  for needle in "${NEEDLES[@]}"; do
    if grep -r -l -a -F -- "$needle" "$out" >/dev/null 2>&1; then
      found+=("$needle")
    fi
  done
  echo "${found[*]-}"
}

jar=target/nitrite-bridge-example.jar

echo "==> release build (-Prelease: dbinspect is provided)"
mvn -q -B -Prelease clean package
present="$(grep_jar "$jar" "$work/release")"
if [[ -n "$present" ]]; then
  echo "FAIL: these survived into the release jar: $present" >&2
  exit 1
fi
classes="$(find "$work/release" -path '*dbinspect*' -name '*.class' | wc -l | tr -d ' ')"
if [[ "$classes" != "0" ]]; then
  echo "FAIL: $classes bridge class files are in the release jar" >&2
  exit 1
fi
echo "    none of: ${NEEDLES[*]}, and no dbinspect class files"
echo "    $(du -h "$jar" | cut -f1) $jar"

echo "==> development build (control)"
mvn -q -B clean package
present="$(grep_jar "$jar" "$work/dev")"
for needle in "${NEEDLES[@]}"; do
  if [[ " $present " != *" $needle "* ]]; then
    echo "FAIL: $needle is missing from the development build, so the check above" >&2
    echo "      was not proving anything." >&2
    exit 1
  fi
done
echo "    back, as expected: $present"

echo
echo "OK: criterion 2 holds on the desktop jar."
