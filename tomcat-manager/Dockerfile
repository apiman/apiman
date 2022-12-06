ARG BASE_IMAGE=tomcat:9.0.70-jdk11-temurin
FROM $BASE_IMAGE

ARG APIMAN_VERSION
ARG APIMAN_DISTRO=https://github.com/apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip
ARG POSTGRES_VERSION=42.3.0

LABEL org.opencontainers.image.authors="florian.volk@scheer-group.com"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.url="https://www.apiman.io"
LABEL org.opencontainers.image.source="https://www.github.com/apiman/apiman"
LABEL org.opencontainers.image.title="Apiman Manager (Tomcat distro)"
LABEL org.opencontainers.image.description="Apiman Manager API and UI (Tomcat distro)"
LABEL org.opencontainers.image.version=$APIMAN_VERSION

EXPOSE 8080 8443

USER root

ADD $APIMAN_DISTRO /tmp/apiman-overlay.zip
ADD https://jdbc.postgresql.org/download/postgresql-$POSTGRES_VERSION.jar $CATALINA_HOME/lib

RUN groupadd -r tomcat && \
    useradd --no-log-init --create-home -r -g tomcat tomcat && \
    chown -R tomcat:tomcat /home/tomcat && \
    apt-get update && \
    apt-get install -y unzip && \
    apt-get clean

WORKDIR $CATALINA_HOME
RUN unzip -o /tmp/apiman-overlay.zip && \
    rm /tmp/apiman-overlay.zip && \
    unzip -o webapps/apimanui.war -d webapps/apimanui && \
    unzip -o webapps/apiman.war -d webapps/apiman && \
    rm webapps/apiman-gateway.war && rm webapps/apiman-gateway-api.war && \
    chgrp -R tomcat $CATALINA_HOME && chmod -R g+rw $CATALINA_HOME

USER tomcat
ENTRYPOINT ["catalina.sh"]
CMD [ "run" ]
