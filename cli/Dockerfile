FROM openjdk:8 AS build

ARG CLI_VERSION="0.4.0"
ARG RELEASE_TYPE="tag"

COPY files /tmp
RUN chmod +x /tmp/fetch-source.sh && \
    /tmp/fetch-source.sh ${CLI_VERSION} ${RELEASE_TYPE}

WORKDIR /tmp/apiman-cli
RUN chmod +x /tmp/apiman-cli/gradlew && \
    ./gradlew shadowJar

FROM openjdk:8-jre-alpine

RUN mkdir -p /opt/apiman-cli/lib
COPY --from=build /tmp/apiman-cli/build/libs/* /opt/apiman-cli/lib/

ENTRYPOINT [ "java", "-jar", "/opt/apiman-cli/lib/apiman-cli.jar" ]
