#!/usr/bin/env bash

openssl aes-256-cbc -pass pass:"$PGP_KEY_PASSWORD" -in "$GITHUB_WORKSPACE"/.ci/secring.gpg.enc -out "$GITHUB_WORKSPACE"/secring.gpg -d -md md5
mkdir -p ~/.gnupg
cp "$GITHUB_WORKSPACE"/secring.gpg ~/.gnupg/secring.gpg
