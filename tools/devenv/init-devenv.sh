#!/bin/sh

rm -rf wildfly-dev-server
cp -rf ../server-all/target/wildfly-dev-server .

rm -f wildfly-dev-server/standalone/deployments/apiman.war
rm -f wildfly-dev-server/standalone/deployments/apiman.war.deployed
rm -f wildfly-dev-server/standalone/deployments/apiman-gateway.war
rm -f wildfly-dev-server/standalone/deployments/apiman-gateway.war.deployed
rm -f wildfly-dev-server/standalone/deployments/apiman-gateway-api.war
rm -f wildfly-dev-server/standalone/deployments/apiman-gateway-api.war.deployed
rm -f wildfly-dev-server/standalone/deployments/apimanui.war
rm -f wildfly-dev-server/standalone/deployments/apimanui.war.deployed

mv wildfly-dev-server/standalone/configuration/standalone.xml wildfly-dev-server/standalone/configuration/standalone_original.xml
mv wildfly-dev-server/standalone/configuration/standalone-apiman.xml wildfly-dev-server/standalone/configuration/standalone.xml
