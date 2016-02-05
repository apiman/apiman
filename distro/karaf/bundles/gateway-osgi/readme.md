features:addurl mvn:io.apiman/apiman-karaf/1.2.2-SNAPSHOT/xml/features
features:install apiman-gateway
install -s mvn:io.apiman/gateway-osgi/1.2.2-SNAPSHOT
install -s mvn:io.apiman/gateway-osgi-api/1.2.2-SNAPSHOT

cp distro/karaf/bundles/gateway-osgi/io.apiman.gateway.cfg /Users/chmoulli/Fuse/Fuse-servers/jboss-fuse-6.2.1.redhat-084/etc
install -s mvn:io.apiman/gateway-osgi-api/1.2.2-SNAPSHOT