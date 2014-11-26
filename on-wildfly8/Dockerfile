# Use latest jboss/base-jdk:7 image as the base
FROM jboss/wildfly

MAINTAINER Eric Wittmann <eric.wittmann@redhat.com>

ENV APIMAN_VERSION 1.0.0.Beta1
ENV WILDFLY_VERSION 8.1.0.Final

RUN cd $HOME \
 && curl http://downloads.jboss.org/overlord/apiman/$APIMAN_VERSION/apiman-distro-wildfly8-$APIMAN_VERSION-overlay.zip | bsdtar -xvf- \
 && cp -Rf $HOME/wildfly-$WILDFLY_VERSION/* $HOME/wildfly \
 && rm -rf $HOME/wildfly-$WILDFLY_VERSION

RUN  /opt/jboss/wildfly/bin/add-user.sh admin admin123! --silent


# Set the default command to run on boot
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]
