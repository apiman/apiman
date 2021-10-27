#!/usr/bin/bash
set -o nounset
set -o errtrace
set -e
set -x


SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

export MVN=

if ! command -v mvnd &> /dev/null
then
    echo "*** mvnd (maven daemon) could not be found. Using Maven Wrapper. https://github.com/mvndaemon/mvnd#how-to-install-mvnd ***"
    MVN="$SCRIPT_DIR/mvnw -T1C "
else
    MVN=mvnd
fi

echo "Fast build skips tests, Javadoc, and various other non-essentials for quickly rebuilding."

cd $SCRIPT_DIR
cd parent
$MVN clean install
cd $SCRIPT_DIR
$MVN clean install -DskipTests -Dmaven.javadoc.skip -nsu -Pinstall-all-wildfly

echo "Done"




