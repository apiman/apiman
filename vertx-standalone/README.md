apiman vertx standalone
=======================

This Docker image has an embedded apiman CLI, as well as the Vert.x standalone distribution, enabling you to pass it an API declaration to use. See [apiman CLI](https://github.com/apiman/apiman-cli) documentation for syntax.

## Usage

To start up apiman

    docker run -it apiman/vertx-standalone

You may want to map the port(s) so you can access the app

    docker run -it -p 8082:8082 apiman/vertx-standalone

You will want to provide a declaration and a headless configuration

	docker run -it -p 8082:8082 -v $PWD/examples/apis:/opt/apiman/apis -v $PWD/examples/configs:/opt/apiman/configs apiman/vertx-standalone sample.yml

## Building the image

    docker build -t="apiman/vertx-standalone" --rm .

## Image accessible on Docker hub

This image is automatically built and published into [Docker Hub](https://registry.hub.docker.com/u/apiman/vertx-standalone/).
