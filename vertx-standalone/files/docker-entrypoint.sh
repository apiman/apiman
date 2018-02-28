#!/usr/bin/env bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
API_SRC_DIR="${SCRIPT_DIR}/apis"
CLI_ARGS= # Nothing, by default

# The combined output file
export CONFIG_FILE_PATH="/opt/apiman/apis/all.json"

# All declarations
DECLARATIONS=""
for DECLARATION_FILE in "$@"; do
    DECLARATIONS="${DECLARATIONS} --declarationFile ${API_SRC_DIR}/${DECLARATION_FILE}"
done

# Perform conversion
java -jar ${SCRIPT_DIR}/lib/apiman-cli.jar \
      gateway generate headless ${DECLARATIONS} \
      --outputFile ${CONFIG_FILE_PATH} ${CLI_ARGS}

./apiman-gateway.sh -conf=/opt/apiman/configs/conf-standalone.json
