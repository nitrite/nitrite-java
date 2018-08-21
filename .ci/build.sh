#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    if [[ "${TRAVIS_BRANCH}" = "master" ]]; then
        ./gradlew clean build asciidoc -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE uploadArchives --stacktrace
    elif [[ "${TRAVIS_BRANCH}" = "develop" ]]; then
        ./gradlew clean build asciidoc -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE --stacktrace
    else
        echo "${TRAVIS_BRANCH} is not configured for build."
    fi
else
    ./gradlew clean build asciidoc -Prelease -PnitriteVersion=$NITRITE_VERSION -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$PGP_KEY_FILE uploadArchives closeAndReleaseRepository :nitrite-explorer:shadowJar --stacktrace
fi