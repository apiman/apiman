#!/bin/sh
mvn clean test -Papidocs
cp -rf target/miredot/* ../../../../apiman.github.io/latest/apidoc/manager/.
echo "******************"
echo "*** Published! ***"
echo "******************"
