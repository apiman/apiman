FROM jboss/wildfly

RUN $JBOSS_HOME/bin/add-user.sh admin admin123! --silent

EXPOSE 8787

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml", "--debug"]

# These 2 steps are latest as to invalidate as fewer layers as possible with each build
ADD apiman-distro-wildfly8-overlay.zip /tmp/
RUN unzip -o /tmp/apiman-distro-wildfly8-overlay.zip -d $JBOSS_HOME
