#!/bin/sh

# Convenience script to launch apiman's Vert.x gateway with sensible
# default behaviour.
# To pass additional Java options with $APIMAN_GATEWAY_OPTS.
# e.g. export APIMAN_GATEWAY_OPTS="-Xmx2048".

# Some elements cribbed from WF's `standalone.sh`; thanks.
DIRNAME=$(dirname "$0")
# PROGNAME=$(basename "$0")

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# Launch
$JAVA $APIMAN_GATEWAY_OPTS -Dlog4j.configurationFile="$DIRNAME/log4j2.xml" \
    `# Set Vert.x's logger to use Log4j2 (NB: this is separate from apiman's policy/internal logging).` \
    -Dvertx.logger-delegate-factory-class-name=io.apiman.gateway.platforms.vertx3.logging.ApimanLog4j2LogDelegateFactory \
    `# Async Log4j2 logging using LMAX Disruptor.` \
    -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
    `# $@ to pass args through.` \
    -jar "$DIRNAME/apiman-gateway.jar" "$@"
