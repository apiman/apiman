#!/usr/bin/bash
set -o nounset
set -o errtrace
set -e
set -x

if ! command -v mvnd &> /dev/null
then
    echo "mvnd (maven daemon) could not be found. https://github.com/mvndaemon/mvnd#how-to-install-mvnd"
    exit
fi


SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo "Fast build skips tests, Javadoc, and various other non-essentials for quickly rebuilding."

cd $SCRIPT_DIR
cd parent
mvn clean install
cd $SCRIPT_DIR
mvnd clean install -DskipTests -Dmaven.javadoc.skip -nsu -Pinstall-all-wildfly

echo "Done"




