#!/usr/bin/env bash
set -x -e

export HOSTNAME
export KEYCLOAK_VERSION

HOSTNAME=$(hostname)
KEYCLOAK_VERSION=13.0.0
# Latest 12.x version that we have in the POM is not working properly with all of our docker settings. Use this when it does...
#$(mvn -q -Dexec.executable="echo" -Dexec.args='${version.org.keycloak}' --non-recursive exec:exec -f ../../../pom.xml)

GULP_PS=
cleanup () {
   echo "Cleaning up Gulp and Docker..."
   pkill -TERM -P "$GULP_PS"
   docker-compose down -v --remove-orphans
}

# Just in case we have any weird stuff left hanging around
killall java || true

trap cleanup ERR EXIT

# The CI gulpfile is run from directory above because it extends the original file and we want to avoid complications with
# path handling. This adds a few proxy routes so that Cypress does not complain about mixed origins.
# It uses the UI from the docker container rather than the local version, so just be aware of that.
yarn run gulp --gulpfile gulpfileCI.js &

## Save gulp PID so we can kill it later during cleanup
GULP_PS=$!
echo "Gulp process ID is: $GULP_PS"

export JAVA_OPTS=""

# Start docker container from latest local docker to give us a backend. Daemon mode, so will need to clean up later.
docker-compose up -d

# We need to figure out when the backend is actually available, so we use this handy 'wait-on' status utility.
# Check out `waitOnConfig.js` for username and password injection.
yarn run wait-on -v -d 15000 -t 300000 -c waitOnConfig.js "http://$HOSTNAME:2772/apiman/system/status" || true

docker-compose logs

# If CI env var set, use record mode and export to Cypress dashboard.
if [[ -n $CI ]]
then
    yarn cy:run --record --config baseUrl="http://$HOSTNAME:2772/apimanui/,retries=3,defaultCommandTimeout=10000"
else
    yarn cy:run --config baseUrl="http://$HOSTNAME:2772/apimanui/,retries=3,defaultCommandTimeout=10000"
fi