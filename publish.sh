#!/bin/sh
mvn clean install
cp -rf user-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/user-guide/.
cp -rf developer-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/developer-guide/.
cp -rf installation-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/installation-guide/.
cp -rf production-guide/target/docbook/publish/en-US/html/* ../apiman.github.io/latest/production-guide/.
cp -rf production-guide/en-US/images/* ../apiman.github.io/latest/images/images

cp developer-guide/target/docbook/publish/en-US/pdf/apiman-developer-guide.pdf ../apiman.github.io/latest/developer-guide/.
cp installation-guide/target/docbook/publish/en-US/pdf/apiman-installation-guide.pdf ../apiman.github.io/latest/installation-guide/.
cp production-guide/target/docbook/publish/en-US/pdf/apiman-production-guide.pdf ../apiman.github.io/latest/production-guide/.
cp user-guide/target/docbook/publish/en-US/pdf/apiman-user-guide.pdf ../apiman.github.io/latest/user-guide/.

