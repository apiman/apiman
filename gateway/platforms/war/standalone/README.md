# Standalone WARs

This module, and its children, build a set of WAR files that can be deployed in a simple web app container such as Jetty or Tomcat.

The following WARs are built:

  * Gateway
  * Gateway API
  * Elasticsearch embedded

## Building the WARs

You must disable the `java8` profile to avoid a compilation failure in building the _vert.x_ modules:

    mvn clean install -Dmaven.test.skip=true -P \!java8

You might also wish to build skipping the regular _api-man_ project tests:

    mvn clean install -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -P \!java8

Presuming you have completed the normal _apiman_ build process (`mvn clean install`) then you should have the following WAR files already built:

    standalone
    |--api
       |--target/apiman-gateway-api.war
    |--gateway
       |--target/apiman-gateway.war
    |--es
       |--target/apiman-es.war
       
If you want to build them again, install from the _standalone_ directory:

    mvn clean install

## Deployment

These instructions been tested on Tomcat 8 (apache-tomcat-8.0.30 to be precise).

  1. Copy the WAR files to your Tomcat `webapps` directory
  2. Add a role and users to `conf/tomcat-users.xml`

Example user and role config:
  
    <!-- required apiman role name -->
    <role rolename="apipublisher"/>
    
    <!-- user can be anything you want -->
    <user username="admin" password="admin123!" roles="apipublisher"/>

Once you have deployed the WAR files and configured security, start Tomcat. You should see the WARs get deployed successfully (see the Tomcat logs).

You can access them at:

  * http://localhost:8080/apiman-gateway-api
  * http://localhost:8080/apiman-gateway
  * http://localhost:8080/apiman-es

## Dependencies

  * Since Tomcat (<=8) doesn't provide a CDI implementation, we use JBoss Weld.
  * Currently expects an Elasticsearch instance running on port `localhost:9200` with a cluster named _apiman_. In order to get up and running quickly, you can use the ES embedded WAR, but a proper Elasticsearch cluster is strongly recommended.

## Implementation

The standalone WARs are based on the _apiman_ micro WAR implementation.

## TODO

  * Allow use of non-ES component implementations, such as the Infinispan ones.
