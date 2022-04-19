FROM tomcat:9.0.62-jdk11-temurin

MAINTAINER florian.volk@scheer-group.com
EXPOSE 8080 8443

ARG APIMAN_VERSION

USER root
ADD https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip $CATALINA_HOME
RUN groupadd -r tomcat && \
    useradd --no-log-init --create-home -r -g tomcat tomcat && \
    chown -R tomcat:tomcat /home/tomcat && \
    apt-get update && \
    apt-get install -y unzip && \
    apt-get clean

WORKDIR $CATALINA_HOME
RUN unzip -o apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip && \
    # speed up tomcat
    unzip -o webapps/apimanui.war -d webapps/apimanui && \
    unzip -o webapps/apiman.war -d webapps/apiman && \
    rm apiman-distro-tomcat-$APIMAN_VERSION-overlay.zip && \
    rm webapps/apiman-gateway.war && rm webapps/apiman-gateway-api.war && \
    chgrp -R tomcat $CATALINA_HOME && chmod -R g+rw $CATALINA_HOME

USER tomcat
ENTRYPOINT ["catalina.sh"]
CMD [ "run" ]
