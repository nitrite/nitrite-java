#!/usr/bin/env sh

## Zip Unzip
sudo apt-get -qqy update
sudo apt-get install -y zip

## Android
wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
tar zxvf android-sdk_r24.4.1-linux.tgz
sudo mv android-sdk-linux /opt/android-sdk
sudo apt-get update
export ANDROID_HOME="/opt/android-sdk"
sudo dpkg --add-architecture i386
sudo apt-get -qqy update
sudo apt-get -qqy install libncurses5:i386 libstdc++6:i386 zlib1g:i386
echo y | /opt/android-sdk/tools/android --silent update sdk --no-ui --all --filter platform-tools,tools,build-tools-24.0.0,android-24,extra-android-m2repository

## Mongo
sudo service mongod start
sudo service mongod status
sudo mongo admin --eval "db.getSiblingDB('benchmark').createUser({user: 'bench', pwd: 'bench', roles: [{role: 'readWrite', db: 'benchmark'}, {role: 'dbAdmin', db: 'benchmark'}]})"
