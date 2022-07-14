# Use WildFly image as the base
ARG WILDFLY_VERSION=23.0.2.Final
FROM quay.io/wildfly/wildfly:${WILDFLY_VERSION}

MAINTAINER Marc Savy <marc@blackparrotlabs.io>
EXPOSE 8080 8443

ARG APIMAN_VERSION

RUN cd $HOME/wildfly \
 && curl -L https://github.com/Apiman/apiman/releases/download/$APIMAN_VERSION/apiman-distro-wildfly-$APIMAN_VERSION-overlay.zip | bsdtar -xvf-

USER root

RUN chown -R jboss:0 ${JBOSS_HOME} \
 && chmod -R g+rw ${JBOSS_HOME}

USER jboss

# Set the default command to run on boot
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]
