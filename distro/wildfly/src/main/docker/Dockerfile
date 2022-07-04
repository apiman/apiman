FROM ${docker.base-image}

LABEL org.opencontainers.image.authors="Marc Savy <marc@blackparrotlabs.io>"

ENV APIMAN_VERSION ${project.version}

USER root

COPY maven/target ${JBOSS_HOME}

RUN chown -R jboss:0 ${JBOSS_HOME} \
 && chmod -R g+rw ${JBOSS_HOME}

USER jboss

RUN cd $HOME/wildfly && bsdtar -xvf apiman-distro-${docker.wildfly.version}-$APIMAN_VERSION-overlay.zip

# Set the default command to run on boot
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]
