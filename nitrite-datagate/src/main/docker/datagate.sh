#!/usr/bin/env sh

java $JAVA_OPTS -Xshareclasses -Xquickstart -jar nitrite-datagate.jar --spring.config.name=datagate
