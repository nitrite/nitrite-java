#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    ./gradlew clean build run asciidoc -Dscan -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE uploadArchives --stacktrace --info
else
    ./gradlew clean build run asciidoc -Prelease -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE uploadArchives closeAndReleaseRepository :nitrite-explorer:shadowJar -Dscan --stacktrace --info
fi