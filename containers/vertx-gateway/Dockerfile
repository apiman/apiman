ARG BASE_IMAGE=eclipse-temurin:11-jre-alpine
FROM $BASE_IMAGE

ARG APIMAN_VERSION
ARG APIMAN_DISTRO=https://github.com/apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-vertx-$APIMAN_VERSION.zip

LABEL org.opencontainers.image.authors="florian.volk@scheer-group.com"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.url="https://www.apiman.io"
LABEL org.opencontainers.image.source="https://www.github.com/apiman/apiman"
LABEL org.opencontainers.image.title="Apiman Gateway (Vert.x distro)"
LABEL org.opencontainers.image.description="Apiman Gateway (Vert.x distro). Unless you are operating headlessly, you need an Apiman Manager as well."
LABEL org.opencontainers.image.version=$APIMAN_VERSION

EXPOSE 8080 8081

RUN addgroup -S apiman && adduser -S apiman -G apiman && \
    mkdir -p /opt/apiman/logs && \
    chown -R apiman:apiman /home/apiman /opt/apiman/logs && \
    apk update && apk add libarchive-tools && rm -rf /var/cache/apk/*

ADD $APIMAN_DISTRO /tmp/apiman-distro-vertx.zip

RUN bsdtar xvf /tmp/apiman-distro-vertx.zip --strip-components=1 -C /opt/apiman && \
    rm /tmp/apiman-distro-vertx.zip && \
    chmod +x /opt/apiman/apiman-gateway.sh

USER apiman
WORKDIR /opt/apiman
ENTRYPOINT [ "./apiman-gateway.sh" ]
CMD [ "--conf", "configs/conf-es.json"]
