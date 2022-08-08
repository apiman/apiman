#!/usr/bin/env bash
set -x -e

export HOSTNAME
export APIMAN_IMAGE_ID

#HOSTNAME=$(hostname)
HOSTNAME=localhost
APIMAN_IMAGE_ID="apiman/on-wildfly:$(git rev-parse HEAD)"
#$(mvn -q -Dexec.executable="echo" -Dexec.args='${version.org.keycloak}' --non-recursive exec:exec -f ../../../pom.xml)

cleanup () {
   echo "Cleaning up Docker compose..."
   docker-compose --project-name="apiman-ci-testing" down -v --remove-orphans
}

trap cleanup ERR EXIT

# Start docker container from latest local docker to give us a backend. Daemon mode, so will need to clean up later.

docker-compose --project-name="apiman-ci-testing" up  -d

# We need to figure out when the backend is actually available, so we use this handy 'wait-on' status utility.
# Check out `waitOnConfig.js` for username and password injection.
yarn run wait-on -v -d 15000 -t 300000 -c waitOnConfig.js "http://$HOSTNAME:8877/apiman/system/status"

yarn cy:open --config baseUrl="http://$HOSTNAME:8877/,retries=3,defaultCommandTimeout=10000"
