#!/usr/bin/env bash

set -e

if [[ $# -lt 2 ]]; then
	echo "Usage: $( basename $0 ) <CLI_VERSION> <RELEASE_TYPE>"
	exit 1
fi

CLI_VERSION="$1"
RELEASE_TYPE="$2"

case "${RELEASE_TYPE}" in
    tag)
        SOURCE_URL="https://codeload.github.com/apiman/apiman-cli/zip/v${CLI_VERSION}"
        ;;
    branch)
        SOURCE_URL="https://codeload.github.com/apiman/apiman-cli/zip/${CLI_VERSION}"
        ;;
    *)
        echo "Release type must be branch or tag"
		exit 1
		;;
esac

echo -e "Downloading source from: ${SOURCE_URL}"
curl -s ${SOURCE_URL} -o /tmp/apiman-cli.zip

unzip /tmp/apiman-cli.zip -d /tmp
mv /tmp/apiman-cli-* /tmp/apiman-cli
