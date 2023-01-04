# Apiman on WildFly

## Note about Keycloak

> **Important**
> As of Apiman 3.0.0.Final, Keycloak server is no longer included in this quickstart image.

You will need to run Keycloak separately and point Apiman to Keycloak using an environment variable.

The following is just an example; you are free to use any ports or configuration.
You may need to tweak your Keycloak realm definition depending upon the particulars of your setup.

```shell
docker run -it -p 8085:8080 -e KEYCLOAK_FRONTEND_URL=http://localhost:8085 quay.io/keycloak/keycloak:18.0.2

docker run -it -p 8080:8080 -e APIMAN_AUTH_URL=http://localhost:8085 apiman/on-wildfly:latest-release
```
* Run Keycloak and map to 8085. Let Keycloak know externally the port is 8085 for redirects, etc.
* Run Apiman, telling Apiman where Keycloak is via `APIMAN_AUTH_URL`.
* Access Apiman at http://localhost:8080/apimanui
* Access Keycloak at http://localhost:8085/admin

## Usage

To start up Apiman

    docker run -it apiman/on-wildfly

You may want to map the port(s) so you can access the app

    docker run -it -p 8080:8080 -p 8443:8443 -e APIMAN_AUTH_URL=<your Keycloak server> apiman/on-wildfly:latest-release

## Building the image

```shell
docker build --build-arg APIMAN_VERSION=3.0.0.Final -t="apiman/on-wildfly" --rm .
```
Various useful build arguments are available, such as the Apiman version and WildFly version. 

Please refer to the Dockerfile to see everything that is available. 
Most of them have sensible default values.

## Image accessible on Docker hub

This image is automatically built and published into [Docker Hub](https://registry.hub.docker.com/r/apiman/on-wildfly).

## How to extend the image

You might want to extend the image. Usually creating/enabling admin user for WildFly is a good practice. Also, if you want to debug the Apiman, you can enable debugging, and expose the debug port like so:

```dockerfile
FROM apiman/on-wildfly
RUN $JBOSS_HOME/bin/add-user.sh admin admin123! --silent
EXPOSE 8787
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml", "--debug"]
```

You can build your own extended image with:

```shell
docker build --rm -t "myname/apiman-on-wildfly:latest" .
```

And then run it like:

```shell
docker run -it --rm -p 8080:8080 -p 9990:9990 -p 8787:8787 myname/apiman-on-wildfly
```    
