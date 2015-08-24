#!/bin/sh
mvn clean install
cp -rf user-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/user-guide/.
cp -rf developer-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/developer-guide/.
cp -rf installation-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/installation-guide/.
cp -rf production-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/production-guide/.

