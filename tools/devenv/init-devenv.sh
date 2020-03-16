#!/bin/sh

rm -rf wildfly-10.1.0.Final
cp -rf ../server-all/target/wildfly-10.1.0.Final .

rm -f wildfly-10.1.0.Final/standalone/deployments/apiman.war
rm -f wildfly-10.1.0.Final/standalone/deployments/apiman.war.deployed
rm -f wildfly-10.1.0.Final/standalone/deployments/apiman-gateway.war
rm -f wildfly-10.1.0.Final/standalone/deployments/apiman-gateway.war.deployed
rm -f wildfly-10.1.0.Final/standalone/deployments/apiman-gateway-api.war
rm -f wildfly-10.1.0.Final/standalone/deployments/apiman-gateway-api.war.deployed
rm -f wildfly-10.1.0.Final/standalone/deployments/apimanui.war
rm -f wildfly-10.1.0.Final/standalone/deployments/apimanui.war.deployed

mv wildfly-10.1.0.Final/standalone/configuration/standalone.xml wildfly-10.1.0.Final/standalone/configuration/standalone_original.xml
mv wildfly-10.1.0.Final/standalone/configuration/standalone-apiman.xml wildfly-10.1.0.Final/standalone/configuration/standalone.xml
