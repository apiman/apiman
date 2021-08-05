FROM jboss/wildfly:20.0.1.Final

MAINTAINER florian.volk@scheer-group.com
EXPOSE 8080 8443

ARG APIMAN_VERSION

USER root
WORKDIR $JBOSS_HOME
RUN curl -L https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-wildfly-$APIMAN_VERSION-overlay.zip | bsdtar -xvf- && \
    rm -rf $JBOSS_HOME/standalone/deployments/apiman-ds.xml && \
    rm -rf $JBOSS_HOME/standalone/deployments/apiman-gateway-ds.xml && \
    rm -rf $JBOSS_HOME/standalone/deployments/apiman-gateway.war && \
    rm -rf $JBOSS_HOME/standalone/deployments/apiman-gateway-api.war && \
    chown -R jboss:jboss $JBOSS_HOME && chmod -R g+rw $JBOSS_HOME

USER jboss
ENTRYPOINT ["./bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
CMD [ "-c", "standalone-apiman.xml"]
