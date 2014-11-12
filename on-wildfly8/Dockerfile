# Use latest jboss/base-jdk:7 image as the base
FROM jboss/base-jdk:7

MAINTAINER Eric Wittmann <eric.wittmann@redhat.com>

ENV WILDFLY_VERSION 8.1.0.Final
ENV APIMAN_VERSION 1.0.0.Alpha4

RUN cd $HOME \
 && curl http://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.zip -o wildfly-$WILDFLY_VERSION.zip \
 && curl http://downloads.jboss.org/overlord/apiman/$APIMAN_VERSION/apiman-distro-wildfly8-$APIMAN_VERSION-overlay.zip -o apiman-distro-wildfly8-$APIMAN_VERSION-overlay.zip \
 && unzip wildfly-$WILDFLY_VERSION.zip \
 && unzip apiman-distro-wildfly8-$APIMAN_VERSION-overlay.zip \
 && ln -s wildfly-$WILDFLY_VERSION wildfly

# Set the JBOSS_HOME env variable
ENV JBOSS_HOME /opt/jboss/wildfly

# Set the default command to run on boot
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-c", "standalone-apiman.xml"]
