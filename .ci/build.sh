#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    ./gradlew build run asciidoc -Dscan -PnitriteVersion=$NITRITE_VERSION
else
    ./gradlew build run asciidoc -Prelease -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE uploadArchives closeAndReleaseRepository :nitrite-explorer:shadowJar -Dscan
fi