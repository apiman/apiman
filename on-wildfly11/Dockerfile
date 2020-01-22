# Use WildFly 11 image as the base
FROM jboss/wildfly:11.0.0.Final

MAINTAINER Marc Savy <marc.savy@redhat.com>

ENV APIMAN_VERSION 1.5.5.Final

RUN cd $HOME/wildfly \
 && curl https://downloads.jboss.org/apiman/$APIMAN_VERSION/apiman-distro-wildfly11-$APIMAN_VERSION-overlay.zip | bsdtar -xvf-

USER root

RUN chown -R jboss:0 ${JBOSS_HOME} \
 && chmod -R g+rw ${JBOSS_HOME}

USER jboss

# Set the default command to run on boot
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]

