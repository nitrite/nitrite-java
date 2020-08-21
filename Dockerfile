FROM openjdk:8

COPY . /

ENV PGP_KEY_PASSWORD=$PGP_KEY_PASSWORD
ENV GITHUB_WORKSPACE=$GITHUB_WORKSPACE
ENV PGP_KEY_ID=$PGP_KEY_ID
ENV MAVEN_USERNAME=$MAVEN_USERNAME
ENV MAVEN_PASSWORD=$MAVEN_PASSWORD
ENV GITHUB_TOKEN=$GITHUB_TOKEN

ENV ANDROID_COMPILE_SDK="25"
ENV ANDROID_SDK_TOOLS_REV="3859397"
ENV ANDROID_HOME="/opt/android-sdk"
ENV ANDROID_SDK_ROOT="/opt/android-sdk"
ENV PATH=$PATH:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools/

RUN apt-get -y update && apt-get install -y --no-install-recommends \
    zip \
    apt-utils \
    unzip \
    && apt-get purge -y --auto-remove \
    && apt-get clean && apt-get autoremove && rm -rf /var/lib/apt/lists/*
RUN mkdir -p $HOME/.android
RUN echo 'count=0' > $HOME/.android/repositories.cfg
RUN wget --quiet --output-document=android-sdk.zip https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS_REV}.zip
RUN unzip -qq android-sdk.zip -d android-sdk-linux
RUN mv android-sdk-linux /opt/android-sdk
RUN apt-get update
RUN dpkg --add-architecture i386
RUN apt-get -qqy update
RUN apt-get -qqy install libncurses5:i386 libstdc++6:i386 zlib1g:i386
RUN echo y | sdkmanager --update
RUN echo y | sdkmanager --licenses
RUN echo y | sdkmanager 'platforms;android-'${ANDROID_COMPILE_SDK}

RUN chmod +x gradlew
RUN ./gradlew clean build
RUN openssl aes-256-cbc -pass pass:$PGP_KEY_PASSWORD -in ./.ci/secring.gpg.enc -out $GITHUB_WORKSPACE/secring.gpg -d -md md5
RUN ./gradlew publish -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$GITHUB_WORKSPACE/secring.gpg