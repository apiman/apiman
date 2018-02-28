apiman cli
==========

## Usage

To use apiman CLI

    docker run -it apiman/cli [args]

For valid arguments, please see the documentation for [apiman CLI](https://github.com/apiman/apiman-cli)

## Building the image

    docker build -t="apiman/cli" --rm .

### Building from a branch

If you're a developer working on this project, or you want the latest edge version, you can build from a branch of the apiman-cli repository.

To do this, specify the RELEASE_TYPE as 'branch' and the CLI_VERSION as the branch name:

	docker build -t="apiman/cli:beta" --build-arg RELEASE_TYPE=branch --build-arg CLI_VERSION="develop" --rm .

## Image accessible on Docker hub

This image is automatically built and published into [Docker Hub](https://registry.hub.docker.com/u/apiman/cli/).
