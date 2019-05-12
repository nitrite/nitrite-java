FROM adoptopenjdk/openjdk8-openj9:alpine
VOLUME /tmp
VOLUME /logs
COPY ./nitrite-datagate.jar /
COPY ./datagate.properties /
COPY ./datagate.sh /

ENV JAVA_OPTS "$JAVA_OPTS -Drun.mode=docker -Djava.security.egd=file:/dev/./urandom"
# log settings
ENV DATAGATE_LOG_FILE "/logs/datagate.log"

# default 30 days cleanup policy
ENV DATAGATE_SYNC_LOG_CLEANUP_DELAY "30"

## Configure below environment variables
#ENV DATAGATE_HOST ""
#ENV DATAGATE_HTTP_PORT ""
#ENV DATAGATE_HTTPS_PORT ""
#ENV DATAGATE_MONITOR_PORT ""
#ENV DATAGATE_KEY_STORE ""
#ENV DATAGATE_KEY_PASSWORD ""

## mongo connection details
#ENV DATAGATE_MONGO_HOST ""
#ENV DATAGATE_MONGO_PORT ""
#ENV DATAGATE_MONGO_USER ""
#ENV DATAGATE_MONGO_PASSWORD ""
#ENV DATAGATE_MONGO_DATABASE ""

#RUN ["chmod", "+x", "./datagate.sh"]
#ENTRYPOINT [ "./datagate.sh" ]