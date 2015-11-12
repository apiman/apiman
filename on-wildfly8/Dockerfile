# Use latest jboss/base-jdk:7 image as the base
FROM jboss/wildfly

MAINTAINER Eric Wittmann <eric.wittmann@redhat.com>

ENV APIMAN_VERSION 1.2.0.Beta2

RUN cd $HOME/wildfly \
 && curl http://downloads.jboss.org/apiman/$APIMAN_VERSION/apiman-distro-wildfly8-$APIMAN_VERSION-overlay.zip | bsdtar -xvf-

# Set the default command to run on boot
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]
