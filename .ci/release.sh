#!/usr/bin/env bash

if [[ -z "${TRAVIS_TAG}" ]]; then
    echo "Not a tagged release"
else
    echo "Creating DataGate zip distribution"
    cp nitrite-datagate/build/libs/nitrite-datagate-$NITRITE_VERSION.jar nitrite-datagate/src/main/dist/lib/nitrite-datagate.jar
    cp nitrite-datagate/build/libs/nitrite-datagate-$NITRITE_VERSION.jar nitrite-datagate/src/main/docker/nitrite-datagate.jar
    rm nitrite-datagate/src/main/dist/lib/.gitkeep
    cd nitrite-datagate/src/main/dist/
    zip -r nitrite-datagate-$NITRITE_VERSION.zip bin conf lib
    cd -
    echo $(pwd)
    echo "Creating docker image"
    cd nitrite-datagate/src/main/docker
    docker build -t dizitart/nitrite-datagate:$NITRITE_VERSION .
    docker tag dizitart/nitrite-datagate:$NITRITE_VERSION dizitart/nitrite-datagate:latest
    docker login -u $DOCKER_USER -p $DOCKER_PASS
    echo "Publishing docker image"
    docker push dizitart/nitrite-datagate
fi
