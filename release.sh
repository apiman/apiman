#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing APIMan"
echo "######################################"
echo ""
read -p "Release Version: " RELEASE_VERSION
read -p "New Development Version: " DEV_VERSION

mvn versions:set -DnewVersion=$RELEASE_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin master

mvn clean install

git tag -a -m "Tagging release $RELEASE_VERSION" dtgov-$RELEASE_VERSION
git push origin dtgov-$RELEASE_VERSION

mvn deploy

mvn versions:set -DnewVersion=$DEV_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin master
