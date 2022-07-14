ARG WILDFLY_VERSION=23.0.2.Final
FROM jboss/wildfly:${WILDFLY_VERSION}

ARG APIMAN_VERSION
ARG APIMAN_DISTRO=https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-wildfly-$APIMAN_VERSION-overlay.zip
ARG POSTGRES_VERSION=42.3.0

LABEL org.opencontainers.image.authors="florian.volk@scheer-group.com,marc@blackparrotlabs.io"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.url="https://www.apiman.io"
LABEL org.opencontainers.image.source="https://www.github.com/apiman/apiman"
LABEL org.opencontainers.image.title="Apiman Manager (WildFly distro)"
LABEL org.opencontainers.image.description="Apiman Manager API and UI (WildFly distro)"
LABEL org.opencontainers.image.version=$APIMAN_VERSION

EXPOSE 8080 8443

ADD https://jdbc.postgresql.org/download/postgresql-$POSTGRES_VERSION.jar $JBOSS_HOME/standalone/deployments
ADD $APIMAN_DISTRO /tmp/apiman-overlay.zip

USER root
WORKDIR $JBOSS_HOME
RUN unzip -o /tmp/apiman-overlay.zip && \
    rm $JBOSS_HOME/standalone/deployments/apiman-gateway-ds.xml && \
    rm $JBOSS_HOME/standalone/deployments/apiman-gateway.war && \
    rm $JBOSS_HOME/standalone/deployments/apiman-gateway-api.war && \
    chown -R jboss:jboss $JBOSS_HOME && chmod -R g+rw $JBOSS_HOME

USER jboss
ENTRYPOINT ["./bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
CMD [ "-c", "standalone-apiman.xml"]
