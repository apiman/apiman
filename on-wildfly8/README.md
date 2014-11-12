apiman on wildfly 8
===================

## Usage

To start up apiman

    docker run -it apiman/on-wildfly8

You may want to map the port(s) so you can access the app

    docker run -it -p 8080:8080 apiman/on-wildfly8

## Building the image

    docker build -t="apiman/on-wildfly8" --rm .
