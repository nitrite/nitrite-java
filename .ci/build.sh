#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    ./gradlew clean build run asciidoc -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE snapshotUpload --stacktrace
else
    ./gradlew clean build run asciidoc -Prelease -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE releaseUpload closeAndReleaseRepository :nitrite-explorer:shadowJar --stacktrace
fi