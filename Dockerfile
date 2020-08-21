FROM openjdk:8-jdk-buster

COPY . /

RUN chmod +x gradlew

ENV PGP_KEY_PASSWORD=$PGP_KEY_PASSWORD
ENV GITHUB_WORKSPACE=$GITHUB_WORKSPACE
ENV PGP_KEY_ID=$PGP_KEY_ID
ENV MAVEN_USERNAME=$MAVEN_USERNAME
ENV MAVEN_PASSWORD=$MAVEN_PASSWORD
ENV GITHUB_TOKEN=$GITHUB_TOKEN

RUN ./gradlew clean build
RUN openssl aes-256-cbc -pass pass:$PGP_KEY_PASSWORD -in ./.ci/secring.gpg.enc -out $GITHUB_WORKSPACE/secring.gpg -d -md md5
RUN ./gradlew publish -Psigning.keyId=$PGP_KEY_ID -Psigning.password=$PGP_KEY_PASSWORD -Psigning.secretKeyRingFile=$GITHUB_WORKSPACE/secring.gpg