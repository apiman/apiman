FROM eclipse-temurin:11-jre-alpine

MAINTAINER florian.volk@scheer-group.com

ARG APIMAN_VERSION

RUN addgroup -S apiman && adduser -S apiman -G apiman && \
	mkdir -p /opt/apiman && \
	chown -R apiman:apiman /home/apiman /opt/apiman && \
    apk update && apk add curl unzip && rm -rf /var/cache/apk/*

RUN curl -s -L https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/migration-assistant-cli.jar -o /opt/apiman/migration-assistant-cli.jar

USER apiman
WORKDIR /opt/apiman
ENTRYPOINT [ "java", "-jar", "migration-assistant-cli.jar" ]
