# Standalone WARs

This module, and its children, build a set of WAR files that can be deployed in a simple web app container such as Jetty or Tomcat.

## Building the WARs

Presuming you have completed the normal _apiman_ build process (`mvn clean install`) then you should have the following WAR files already built:

    standalone
    |--api
       |--target/api.war
    |--gateway
       |--target/gateway.war

## Deployment

These instructions been tested on Tomcat 8 (apache-tomcat-8.0.30 to be precise).

  1. Copy the WAR files to your Tomcat `webapps` directory
  2. Add a role and users to `conf/tomcat-users.xml`

Example user and role config:
  
    <!-- required apiman role name -->
    <role rolename="apipublisher"/>
    
    <!-- user can be anything you want -->
    <user username="admin" password="admin123!" roles="apipublisher"/>

Once you have deployed the WAR files and configured security, start Tomcat. You should see both the `api.war` and `gateway.war` get deployed successfully (see the Tomcat logs).

You can access them at:

  http://localhost:8080/api

and:

  http://localhost:8080/gateway

## Dependencies

The standalone WARs depend on the _apiman micro war_ JAR. Since Tomcat (<=8) doesn't provide a CDI implementation, we use JBoss Weld.

## TODO

  * Refactor common dependencies out of _micro war_ into a separate module.
  * Remove dependency on Jetty and other unneeded artifacts from the _micro war_ module.
  * Externalise configuration used in bootstrap process to allow, for example, use of the Infinispan component implementations instead of the ES ones.

