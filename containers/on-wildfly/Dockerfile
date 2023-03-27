# Use WildFly image as the base
ARG WILDFLY_VERSION=23.0.2.Final
FROM quay.io/wildfly/wildfly:${WILDFLY_VERSION}

ARG APIMAN_VERSION

LABEL org.opencontainers.image.authors="marc@blackparrotlabs.io"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.url="https://www.apiman.io"
LABEL org.opencontainers.image.source="https://www.github.com/apiman/apiman"
LABEL org.opencontainers.image.title="Apiman WildFly all-in-one image (legacy/deprecated)"
LABEL org.opencontainers.image.documentation="https://github.com/apiman/apiman-docker/blob/$APIMAN_VERSION/on-wildfly/README.md"
LABEL org.opencontainers.image.version=$APIMAN_VERSION

EXPOSE 8080 8443

RUN cd $HOME/wildfly \
 && curl -L https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-wildfly-$APIMAN_VERSION-overlay.zip | bsdtar -xvf-

USER root

RUN chown -R jboss:0 ${JBOSS_HOME} \
 && chmod -R g+rw ${JBOSS_HOME}

USER jboss

# Set the default command to run on boot
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]
