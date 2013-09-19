API Management
==============

This repository currently contains a prototype API management capability.


Build
-----

Invoke the following in the root folder:

	mvn clean install


JBoss EAP
---------

The distribution is found in the _$apiman/release/war/target_ folder, called __apiman.war__. Simply copy the war into the _$EAP/standalone/deployment_ folder to deploy APIMan into the EAP server.

1) Manager REST service

This service provides capabilities for the service managers. The REST service can be found at the URL _host_/apiman/manager.

For example, http://localhost:8080/apiman/manager/service/names is used to retrieve the list of service names.

2) Account REST service

This service provides capabilities for account holders. The REST service can be found at the URL _host_/apiman/account.

For example, http://localhost:8080/apiman/account/user/register is used to register a new user's account.

3) HTTP Gateway

The HTTP gateway can be found at the URL _host_/apiman/gateway/_serviceName_/_serviceSpecificPart_

For example, http://localhost:8080/apiman/gateway/rtgov/activity/query?apikey=1234 posts a request to the RTGov activity server.


Apache Karaf
------------

Installation:

a) Download Karaf version 3.0.0.RC1 from http://karaf.apache.org/index/community/download.html

b) Unpack karaf and go to the bin folder and run ./karaf

c) From within the Karaf console, run the following commands:

	feature:repo-add cxf 2.7.5
	feature:install http cxf
	feature:repo-add file:/<full_path_to>/apiman/release/karaf/src/main/resources/features.xml
	feature:install apiman-inmemory
	feature:install apiman-gateway-http
	feature:install apiman-services-rest
	bundle:list

The final command should show:

	START LEVEL 100 , List Threshold: 50
	   ID   State         Level Name
	[  86] [  Resolved] [   80] XBean-Finder-Fragment (3.0.0.M3), Hosts: 87
	[ 176] [    Active] [   50] Apache CXF Compatibility Bundle Jar (2.7.5)
	[ 261] [    Active] [   80] Jackson JSON processor (1.9.9)
	[ 262] [    Active] [   80] Commons Codec (1.5)
	[ 263] [    Active] [   80] Overlord APIMan::Modules::Core (1.0.0.SNAPSHOT)
	[ 264] [    Active] [   80] Overlord APIMan::Modules::InMemory (1.0.0.SNAPSHOT)
	[ 265] [    Active] [   80] Overlord APIMan::Modules::Gateway (1.0.0.SNAPSHOT)
	[ 266] [    Active] [   80] wrap_mvn_org.apache.httpcomponents_httpcore_4.2.1 (0)
	[ 267] [    Active] [   80] wrap_mvn_org.apache.httpcomponents_httpclient_4.2.1 (0)
	[ 268] [    Active] [   80] Overlord APIMan::Modules::Gateway HTTP (1.0.0.SNAPSHOT)
	[ 269] [    Active] [   80] Overlord APIMan::Modules::Service Client HTTP (1.0.0.SNAPSHOT)
	[ 270] [    Active] [   80] Data mapper for Jackson JSON processor (1.9.9)
	[ 271] [    Active] [   80] Overlord APIMan::Modules::Services REST (1.0.0.SNAPSHOT)


1) Manager REST service

This service provides capabilities for the service managers. The REST service can be found at the URL _host_/apiman/manager.

For example, http://localhost:8181/cxf/apiman/manager/service/names is used to retrieve the list of service names.

2) Account REST service

This service provides capabilities for account holders. The REST service can be found at the URL _host_/apiman/account.

For example, http://localhost:8181/cxf/apiman/account/user/register is used to register a new user's account.

3) HTTP Gateway

The HTTP gateway can be found at the URL _host_/apiman/gateway/_serviceName_/_serviceSpecificPart_

For example, http://localhost:8181/apiman/gateway/rtgov/activity/query?apikey=1234 posts a request to the RTGov activity server.


