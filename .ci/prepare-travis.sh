#!/usr/bin/env sh

## Zip Unzip
sudo apt-get -qqy update
sudo apt-get install -y zip

## Android
export ANDROID_COMPILE_SDK="25"
export ANDROID_SDK_TOOLS_REV="3859397"                                              # "26.0.1"

mkdir $HOME/.android                                                                # For sdkmanager configs
echo 'count=0' > $HOME/.android/repositories.cfg                                    # Avoid warning
wget --quiet --output-document=android-sdk.zip https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS_REV}.zip
unzip -qq android-sdk.zip -d android-sdk-linux
sudo mv android-sdk-linux /opt/android-sdk

export ANDROID_HOME="/opt/android-sdk"
export PATH=$PATH:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools/
sudo apt-get update
sudo dpkg --add-architecture i386
sudo apt-get -qqy update
sudo apt-get -qqy install libncurses5:i386 libstdc++6:i386 zlib1g:i386

echo y | sdkmanager --update
echo y | sdkmanager --licenses
echo y | sdkmanager 'platforms;android-'${ANDROID_COMPILE_SDK}

## Mongo
sudo service mongod start
sudo service mongod status
sudo mongo admin --eval "db.getSiblingDB('benchmark').createUser({user: 'bench', pwd: 'bench', roles: [{role: 'readWrite', db: 'benchmark'}, {role: 'dbAdmin', db: 'benchmark'}]})"
