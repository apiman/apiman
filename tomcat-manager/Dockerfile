FROM tomcat:9.0.52-jdk11-adoptopenjdk-hotspot

MAINTAINER florian.volk@scheer-group.com
EXPOSE 8080 8443

ARG APIMAN_VERSION

USER root
ADD https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip $CATALINA_HOME
RUN groupadd -r tomcat && useradd --no-log-init -r -g tomcat tomcat

WORKDIR $CATALINA_HOME
RUN apt-get update && \
    apt-get install -y unzip && \
    apt-get clean && \
    unzip -o apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip && \
    rm apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip && \
    rm webapps/apiman-gateway.war && rm webapps/apiman-gateway-api.war && \
    chgrp -R tomcat $CATALINA_HOME && chmod -R g+rw $CATALINA_HOME

USER tomcat
ENTRYPOINT ["catalina.sh"]
CMD [ "run" ]
