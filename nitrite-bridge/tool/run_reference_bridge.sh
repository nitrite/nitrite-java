#!/usr/bin/env bash
# Starts a Nitrite-backed reference bridge and prints `{"host":…,"port":…,"code":…}` on its first
# line, so the dbinspect conformance suite can be pointed at it without parsing the pairing banner.
#
#   tool/run_reference_bridge.sh [memory|mvstore|rocksdb] &
#   dart run <dbinspect>/conformance/bin/dbinspect_conformance.dart 127.0.0.1:<port> <code>
#
# Needs org.dizitart:dbinspect-bridge in the local repository until it is published; see the
# comment in ../pom.xml. The bridge and its fixtures are in test sources on purpose: the published
# artifact is the adapter and nothing else.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$here"

mvn -q -B -f pom.xml test-compile
mvn -q -B -f pom.xml dependency:build-classpath \
  -Dmdep.outputFile=target/classpath.txt -Dmdep.includeScope=test

exec java \
  -cp "target/classes:target/test-classes:$(cat target/classpath.txt)" \
  org.dizitart.no2.bridge.ReferenceBridge "$@"
