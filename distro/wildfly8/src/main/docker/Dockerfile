FROM jboss/wildfly

ADD apiman-distro-wildfly8-overlay.zip /tmp/

RUN unzip -o /tmp/apiman-distro-wildfly8-overlay.zip -d /opt/jboss/wildfly

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml"]