FROM eclipse-temurin:11.0.13_8-jre-alpine

MAINTAINER florian.volk@scheer-group.com
EXPOSE 8080 8081

ARG APIMAN_VERSION

RUN addgroup -S apiman && adduser -S apiman -G apiman && \
	mkdir -p /opt/apiman/logs && \
	chown -R apiman:apiman /home/apiman /opt/apiman/logs && \
    apk update && apk add curl unzip && rm -rf /var/cache/apk/*

RUN curl -s -L https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-vertx-$APIMAN_VERSION.zip -o /tmp/apiman-distro-vertx-${APIMAN_VERSION}.zip && \
	unzip /tmp/apiman-distro-vertx-${APIMAN_VERSION}.zip -d /tmp && \
	mv /tmp/apiman-distro-vertx-${APIMAN_VERSION}/* /opt/apiman/ && rm -rf /tmp/* && \
    chmod +x /opt/apiman/apiman-gateway.sh

USER apiman
WORKDIR /opt/apiman
ENTRYPOINT [ "./apiman-gateway.sh" ]
CMD [ "--conf", "configs/conf-es.json"]
