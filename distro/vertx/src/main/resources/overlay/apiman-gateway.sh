#!/bin/sh

# Convenience script to launch apiman's Vert.x gateway with sensible
# default behaviour.
#
# To pass additional Java options with $APIMAN_GATEWAY_OPTS.
# e.g. export APIMAN_GATEWAY_OPTS="-Xmx2048m".

# Some elements cribbed from WF's `standalone.sh`; thanks.
DIRNAME=$(dirname "$0")
# PROGNAME=$(basename "$0")
DEBUG_MODE="${DEBUG:-false}"
DEBUG_PORT="${DEBUG_PORT:-8787}"

# Some simple arg parsing (non-destructive to $@).
parseArgs() {
    while [ "$#" -gt 0 ]
    do
        case "$1" in
            --debug)
                DEBUG_MODE=true
                if [ -n "$2" ] && [ "$2" = "$(echo "$2" | sed 's/-//')" ]; then
                    DEBUG_PORT=$2
                    shift
                fi
                ;;
        esac
        shift
    done
}

parseArgs "$@"

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# Set debug settings if not already set
if [ "$DEBUG_MODE" = "true" ]; then
    DEBUG_OPT=$(echo "$JAVA_OPTS" "$APIMAN_GATEWAY_OPTS" | grep "\-agentlib:jdwp")
    if [ "x$DEBUG_OPT" = "x" ]; then
        APIMAN_GATEWAY_OPTS="$APIMAN_GATEWAY_OPTS -agentlib:jdwp=transport=dt_socket,address=0.0.0.0:$DEBUG_PORT,server=y,suspend=n"
        echo "Debugging mode enabled: $APIMAN_GATEWAY_OPTS"
    else
        echo "Debug already enabled in JAVA_OPTS or APIMAN_GATEWAY_OPTS, ignoring --debug argument"
    fi
fi

# Launch
$JAVA "$JAVA_OPTS" \
    "$APIMAN_GATEWAY_OPTS" \
    `# Explicitly tell Apiman to use log4j2` \
    -Dapiman.logger-delegate=log4j2 \
    `# Use Log4j2 by default.` \
    -Dlog4j.configurationFile="$DIRNAME/log4j2.xml" \
    `# Set Vert.x's logger to use Log4j2 (NB: this is separate from apiman's policy/internal logging).` \
    -Dvertx.logger-delegate-factory-class-name=io.apiman.gateway.platforms.vertx3.logging.ApimanLog4j2LogDelegateFactory \
    `# Async Log4j2 logging using LMAX Disruptor.` \
    -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
    `# $@ to pass args through.` \
    -jar "$DIRNAME/apiman-gateway.jar" "$@"
