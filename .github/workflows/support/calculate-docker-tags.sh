#!/bin/bash

OLD_IFS=$IFS
IFS=$'\n'

export DOCKER_TAGS
if [[ $IS_SNAPSHOT == 'true' ]]
then

read -r -d '' DOCKER_TAGS <<EOF
apiman/$IMAGE_NAME:$APIMAN_VERSION
apiman/$IMAGE_NAME:latest
EOF

else

read -r -d '' DOCKER_TAGS <<EOF
apiman/$IMAGE_NAME:$APIMAN_VERSION
apiman/$IMAGE_NAME:latest
apiman/$IMAGE_NAME:latest-release
apiman/$IMAGE_NAME:stable
ghcr.io/apiman/$IMAGE_NAME:$APIMAN_VERSION
ghcr.io/apiman/$IMAGE_NAME:latest
ghcr.io/apiman/$IMAGE_NAME:latest-release
ghcr.io/apiman/$IMAGE_NAME:stable
EOF

fi

IFS=OLD_IFS
echo "$DOCKER_TAGS"
