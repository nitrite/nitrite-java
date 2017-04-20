#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    ./gradlew build run asciidoc -Dscan -PnitriteVersion=$NITRITE_VERSION
else
    openssl aes-256-cbc -pass pass:$PGP_KEY_PASSWORD -in .ci/secring.gpg.enc -out ~/secring.gpg -d
    ./gradlew build run asciidoc -Prelease -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE uploadArchives closeAndReleaseRepository :nitrite-explorer:shadowJar -Dscan
fi