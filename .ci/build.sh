#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    ./gradlew clean build publish -PnitriteVersion="$NITRITE_VERSION" -Psigning.keyId="$PGP_KEY_ID" -Psigning.password="$PGP_KEY_PASSWORD" -Psigning.secretKeyRingFile="$PGP_KEY_FILE" --stacktrace
else
    ./gradlew clean build publish -Prelease -PnitriteVersion="$NITRITE_VERSION" -Psigning.keyId="$PGP_KEY_ID" -Psigning.password="$PGP_KEY_PASSWORD" -Psigning.secretKeyRingFile="$PGP_KEY_FILE" --stacktrace
fi