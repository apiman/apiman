#!/usr/bin/env bash

OPTS=`getopt -o pn: --long push,name:  -- $@`
eval set -- "$OPTS"

PUSH_TO_HUB=false
IMAGE_LOCAL=jcechace/apiman-vertx-openshift

while true; do
    case "$1" in
    -p | --push) PUSH_TO_HUB=true; shift ;;
    -n | --name) IMAGE_LOCAL=$2; shift; shift;;
    *) break ;;
    esac
done

echo "Bulding docker image with tag '${IMAGE_LOCAL}'."
docker build -t ${IMAGE_LOCAL} $( dirname "${BASH_SOURCE[0]}" )


if $PUSH_TO_HUB; then
    echo "Pushing to docker hub."
    docker push ${IMAGE_LOCAL}:latest
fi


