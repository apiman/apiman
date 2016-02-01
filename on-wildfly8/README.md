apiman on wildfly 8
===================

## Usage

To start up apiman

    docker run -it apiman/on-wildfly8

You may want to map the port(s) so you can access the app

    docker run -it -p 8080:8080 -p 8443:8443 apiman/on-wildfly8

## Building the image

    docker build -t="apiman/on-wildfly8" --rm .

## Image accessible on Docker hub

This image is automatically built and published into [Docker Hub](https://registry.hub.docker.com/u/apiman/on-wildfly8/).


## How to extend the image

You might probably want to extend the image. Usually creating/enabling admin user for wildfly is a good practice, and also, if you want to debug the apiman, you can enable debugging, and expose the debug port like so:

    FROM apiman/on-wildfly8
    RUN $JBOSS_HOME/bin/add-user.sh admin admin123! --silent
    EXPOSE 8787
    CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-apiman.xml", "--debug"]

You can build your own extended image with:

    docker build --rm -t "myname/apiman-on-wildfly8:latest" .

And then run it like:

    docker run -it --rm -p 8080:8080 -p 9990:9990 -p 8787:8787 myname/apiman-on-wildfly8
    
