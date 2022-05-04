#!/usr/bin/env bash
set -x -e

export HOSTNAME
export APIMAN_IMAGE_ID

#HOSTNAME=$(hostname)
HOSTNAME=localhost
APIMAN_IMAGE_ID="apiman/on-wildfly:$(git rev-parse HEAD)"
# Latest 12.x version that we have in the POM is not working properly with all of our docker settings. Use this when it does...
#$(mvn -q -Dexec.executable="echo" -Dexec.args='${version.org.keycloak}' --non-recursive exec:exec -f ../../../pom.xml)

cleanup () {
   echo "Cleaning up Docker compose..."
   docker-compose down -v --remove-orphans
}

trap cleanup ERR EXIT

# Start docker container from latest local docker to give us a backend. Daemon mode, so will need to clean up later.
docker-compose up -d

# We need to figure out when the backend is actually available, so we use this handy 'wait-on' status utility.
# Check out `waitOnConfig.js` for username and password injection.
yarn run wait-on -v -d 15000 -t 300000 -c waitOnConfig.js "http://$HOSTNAME:8877/apiman/system/status"

# If CI env var set, use record mode and export to Cypress dashboard.
if [[ -n $CI ]]
then
    yarn cy:run --record --config baseUrl="http://$HOSTNAME:8877/,retries=3,defaultCommandTimeout=10000"
else
    yarn cy:run --config baseUrl="http://$HOSTNAME:8877/,retries=3,defaultCommandTimeout=10000"
fi